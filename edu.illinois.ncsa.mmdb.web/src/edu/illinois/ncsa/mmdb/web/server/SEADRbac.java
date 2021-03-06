package edu.illinois.ncsa.mmdb.web.server;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.util.ListTable;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.common.DefaultRole;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBAC;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;

public class SEADRbac extends RBAC {
    private static Log log = LogFactory.getLog(SEADRbac.class);

    public SEADRbac(Context c) {
        super(c);
    }

    public int getUserAccessLevel(Resource user) {
        int accesslevel = TupeloStore.getInstance().getConfiguration(ConfigurationKey.AccessLevelValues).split("[ ]*,[ ]*").length - 1;
        String accesspredicate = TupeloStore.getInstance().getConfiguration(ConfigurationKey.AccessLevelPredicate);
        if (accesspredicate == null) {
            return accesslevel;
        }

        // check role permission
        Unifier uf = new Unifier();
        uf.addPattern(user, HAS_ROLE, "role");
        uf.addPattern("role", Resource.uriRef(accesspredicate), "access");
        uf.addColumnName("access");
        try {
            getContext().perform(uf);
        } catch (OperatorException e) {
            log.warn("Could not get roles and access levels.", e);
        }
        for (Tuple<Resource> row : uf.getResult() ) {
            int x = Integer.parseInt(row.get(0).toString());
            if (x < accesslevel) {
                accesslevel = x;
            }
        }
        return accesslevel;
    }

    public boolean checkAccessLevel(Resource user, Resource item) {
        if (user == null) {
            user = PersonBeanUtil.getAnonymousURI();
        }
        int userLevel = getUserAccessLevel(user);
        int accesslevel = Integer.parseInt(TupeloStore.getInstance().getConfiguration(ConfigurationKey.AccessLevelDefault));
        String accesspredicate = TupeloStore.getInstance().getConfiguration(ConfigurationKey.AccessLevelPredicate);
        if (accesspredicate != null) {
            Unifier uf = new Unifier();
            uf.addPattern(item, Dc.CREATOR, "creator");
            uf.addPattern(item, Resource.uriRef(accesspredicate), "access", true);
            uf.setColumnNames("access", "creator");
            try {
                getContext().perform(uf);
            } catch (OperatorException e) {
                log.warn("Could not get roles and access levels.", e);
            }
            for (Tuple<Resource> row : uf.getResult() ) {
                if (row.get(1).equals(user)) {
                    return true;
                }
                if (row.get(0) != null) {
                    accesslevel = Integer.parseInt(row.get(0).toString());
                }
            }
        }
        return userLevel <= accesslevel;
    }

    public boolean isOwner(Resource user, Resource object) throws RBACException {
        try {
            return getContext().match(object, Dc.CREATOR, user).size() > 0;
        } catch (OperatorException e) {
            throw new RBACException("cannot determine ownership", e);
        }
    }

    /**
     * If the user is the dc:creator of the given object, also includes the
     * owner in the
     * DefaultRole.OWNER role.
     * 
     * @param person
     *            the person
     * @param object
     *            the object that they might be the dc:creator of
     * @throws
     * 
     */
    public Collection<Resource> getRoles(Resource personUri, Resource object) throws RBACException {
        Set<Resource> r = new HashSet<Resource>(getRoles(personUri));
        if (isOwner(personUri, object)) {
            r.add(Resource.uriRef(DefaultRole.OWNER.getUri()));
        }
        if (personUri.equals(PersonBeanUtil.getAnonymousURI())) {
            r.add(Resource.uriRef(DefaultRole.ANONYMOUS.getUri()));
        }
        return r;
    }

    /**
     * Check if a given user has the given permission
     * 
     * @param userURI
     * @param permission
     * @return if the user owns the permission or not
     * @throws RBACException
     */
    public boolean checkPermission(String userURI, Permission permission) throws RBACException {
        if ((userURI == null) || userURI.equals("") || (permission == null)) {
            throw (new RBACException("Need to specify user/permission."));
        }
        return checkPermission(Resource.uriRef(userURI), Resource.uriRef(permission.getUri()));
    }

    /**
     * Check if a given user has the given permission on the given object
     * 
     * @param user
     * @param permission
     * @return if the user owns the permission or not
     * @throws OperatorException
     */
    public boolean checkPermission(Resource user, Resource object, Resource permission) throws RBACException {
        if (checkPermission(user, permission)) {
            return true;
        } else if (object != null && isOwner(user, object) && getAllows(Resource.uriRef(DefaultRole.OWNER.getUri())).contains(permission)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkPermission(String user, String object, Permission permission) throws RBACException {
        Resource o = object != null ? Resource.uriRef(object) : null; // argh
        return checkPermission(Resource.uriRef(user), o, Resource.uriRef(permission.getUri()));
    }

    public void createRole(Resource roleUri, String label, EnumSet<Permission> allows) throws OperatorException {
        int accesslevel = Integer.parseInt(TupeloStore.getInstance().getConfiguration(ConfigurationKey.AccessLevelDefault));
        String accesspredicate = TupeloStore.getInstance().getConfiguration(ConfigurationKey.AccessLevelPredicate);
        createRole(roleUri, label, allows, accesspredicate, accesslevel);
    }

    public void createRole(Resource roleUri, String label, EnumSet<Permission> allows, String accesspredicate, int accesslevel) throws OperatorException {
        createRole(roleUri, label);
        for (Permission p : allows ) {
            Resource pu = Resource.uriRef(p.getUri());
            setPermissionValue(roleUri, pu, RBAC.ALLOW);
        }
        for (Permission p : EnumSet.complementOf(allows) ) {
            Resource pu = Resource.uriRef(p.getUri());
            setPermissionValue(roleUri, pu, RBAC.DO_NOT_ALLOW);
        }
        if ((accesspredicate != null) && (accesspredicate.trim().length() != 0)) {
            setAccessLevel(roleUri, Resource.uriRef(accesspredicate), accesslevel);
        }
    }

    public void setAccessLevel(Resource uri, Resource accessPredicate, int accesslevel) throws OperatorException {
        TripleWriter tw = new TripleWriter();

        // remove all levels
        for (Triple t : TupeloStore.getInstance().getContext().match(uri, accessPredicate, null) ) {
            tw.remove(t);
        }

        // add missing level
        tw.add(new Triple(uri, accessPredicate, Resource.literal(accesslevel)));

        // perform
        TupeloStore.getInstance().getContext().perform(tw);
    }

    /**
     * Create all the permissions available in Medici. Does not initialize RBAC
     * first; to do
     * that,
     * simply call {@link initialize}.
     * 
     * @throws RBACException
     * 
     * @throws Operator
     */
    public void intializePermissions() throws OperatorException {
        for (Permission p : Permission.values() ) {
            String uri = p.getUri();
            String label = p.getLabel();
            createPermission(Resource.uriRef(uri), label);
        }
    }

    public void associatePermissionsWithRoles(String accesspredicate, int defaultlevel) throws RBACException, OperatorException {
        ListTable<Resource> globalPermissions = new ListTable<Resource>(getGlobalPermissions(accesspredicate));
        for (Permission p : Permission.values() ) {
            Set<Resource> missingFrom = new HashSet<Resource>();
            for (Tuple<Resource> row : globalPermissions ) {
                Resource role = row.get(0);
                missingFrom.add(role);
            }
            for (Tuple<Resource> row : globalPermissions ) {
                Resource role = row.get(0);
                Resource permission = row.get(1);
                if (permission.getString().equals(p.getUri())) {
                    missingFrom.remove(role);
                }
            }
            for (Resource role : missingFrom ) {
                setPermissionValue(role, Resource.uriRef(p.getUri()), DO_NOT_ALLOW);
            }
        }
        if ((accesspredicate != null) && (accesspredicate.trim().length() != 0)) {
            Set<Resource> missingAccessLevel = new HashSet<Resource>();
            for (Tuple<Resource> row : globalPermissions ) {
                if (row.get(5) == null) {
                    missingAccessLevel.add(row.get(0));
                }
            }
            for (Resource role : missingAccessLevel ) {
                getContext().addTriple(role, Resource.uriRef(accesspredicate), defaultlevel);
            }
        }
    }

    /**
     * Initialize default roles and permissions and set them to defaults.
     * It will create a set of roles with associated default permissions.
     * 
     * @throws OperatorException
     * @throws RBACException
     */
    public void intializeDefaultRoles() throws OperatorException {

        //Defaulting roles to default permissions

        //If you want to completely wipe out the roles and permissions, include the following
        //intialize();
        //intializePermissions();

        for (DefaultRole role : DefaultRole.values() ) {
            String label = role.getName();
            Resource roleUri = Resource.uriRef(role.getUri());
            deleteRole(roleUri, false);
            createRole(roleUri, label, role.getPermissions());
        }
    }
}