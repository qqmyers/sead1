package edu.illinois.ncsa.mmdb.web.server.rest;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.data.ClientInfo;
import org.restlet.security.Enroler;
import org.restlet.security.Role;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.rbac.medici.Permission;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.medici.MediciRbac;

/**
 * Returns a list of Roles based on permissions associated with the userid. The
 * roles used as guard, need to be the same object as those returned by this
 * Enroler, i.e. make sure to use the help functions getRole().
 * 
 * @author Rob Kooper
 * 
 */
public class MediciAuthorizer implements Enroler {
    private static Log               log = LogFactory.getLog(MediciAuthorizer.class);

    static private Map<String, Role> roles;

    /**
     * Initialize the list of roles based on permissions, this ensures we return
     * the same role object for the same URI.
     */
    static {
        roles = new HashMap<String, Role>();
        for (Permission p : Permission.values() ) {
            roles.put(p.getUri(), new Role(p.getUri(), p.getLabel()));
        }
    }

    /**
     * Given a permission, return the role associated with the URI. Need to make
     * sure that for each URI only a single Role object exists otherwise the
     * equality will fail.
     * 
     * @param permission
     *            the permission to be converted to a Role.
     * @return the Role based on the URI of the permission.
     */
    static Role getRole(Permission permission) {
        if (roles.containsKey(permission.getUri())) {
            return roles.get(permission.getUri());
        }
        Role role = new Role(permission.getUri(), permission.getLabel());
        roles.put(role.getName(), role);
        return role;
    }

    /**
     * Given a uri, return the role associated with thus uri. Need to make sure
     * that for each uri only a single Role object exists otherwise the equality
     * will ail.
     * 
     * @param uri
     *            the uri to be converted to a Role.
     * @return the Role based on the uri.
     */
    static Role getRole(Resource uri) {
        return getRole(uri.getString());
    }

    /**
     * Given a uri, return the role associated with thus uri. Need to make sure
     * that for each uri only a single Role object exists otherwise the equality
     * will ail.
     * 
     * @param uri
     *            the uri to be converted to a Role.
     * @return the Role based on the uri.
     */
    static Role getRole(String uri) {
        if (roles.containsKey(uri)) {
            return roles.get(uri);
        }
        Role role = new Role(uri, uri);
        roles.put(role.getName(), role);
        return role;

    }

    // ----------------------------------------------------------------------
    // given userid, find all roles
    // ----------------------------------------------------------------------
    @Override
    public void enrole(ClientInfo clientInfo) {
        // fetch roles based on user
        MediciRbac rbac = new MediciRbac(TupeloStore.getInstance().getContext());
        Resource user = Resource.uriRef(clientInfo.getUser().getIdentifier());

        // add all roles to clientinfo
        try {
            for (Resource perm : rbac.getRolePermissions(user) ) {
                clientInfo.getRoles().add(getRole(perm));
            }
        } catch (RBACException e) {
            log.warn("Could not get roles, no roles given.", e);
        }
    }
}
