package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteRole;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.rbac.medici.Permission;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBAC;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.medici.MediciRbac;

public class DeleteRoleHandler implements ActionHandler<DeleteRole, EmptyResult> {
    Log log = LogFactory.getLog(DeleteRoleHandler.class);

    @Override
    public EmptyResult execute(DeleteRole arg0, ExecutionContext arg1) throws ActionException {
        TupeloStore ts = TupeloStore.getInstance();
        if (ts.isAllowed(arg0, Permission.EDIT_ROLES)) {
            try {
                Resource user = Resource.uriRef(arg0.getUser());
                Resource role = Resource.uriRef(arg0.getUri());
                if (!lockout(user, role)) {
                    ts.getRbac().deleteRole(role);
                } else {
                    throw new ActionException("Deleting this role would lock you out of the system.");
                }
                return new EmptyResult();
            } catch (OperatorException e) {
                log.error("unable to delete role", e);
                throw new ActionException("Server error: " + e.getMessage(), e);
            } catch (RBACException e) {
                log.error("unable to delete role", e);
                throw new ActionException("Server error: " + e.getMessage(), e);
            }
        } else {
            log.debug("user " + arg0.getUser() + " is not authorized to delete roles");
        }
        throw new ActionException("Unauthorized");
    }

    // would deleting this role make it impossible for the user to administer roles?
    // FIXME this is a tile from edit role handler
    boolean lockout(Resource user, Resource role) throws RBACException {
        MediciRbac rbac = TupeloStore.getInstance().getRbac();
        Collection<Resource> roles = rbac.getRoles(user); // roles the user belongs to
        Set<Resource> neededPermissions = new HashSet<Resource>(); // set of permissions needed to administer roles
        neededPermissions.add(Resource.uriRef(Permission.VIEW_MEMBER_PAGES.getUri())); // get to the home page
        neededPermissions.add(Resource.uriRef(Permission.VIEW_ADMIN_PAGES.getUri())); // get to the admin tab
        neededPermissions.add(Resource.uriRef(Permission.EDIT_ROLES.getUri())); // edit roles
        for (Tuple<Resource> row : rbac.getGlobalPermissions(null) ) { // in all permissions
            int i = 0;
            Resource r = row.get(i++);
            Resource permission = row.get(i++);
            Resource valueType = row.get(i++);
            if (!r.equals(role) && roles.contains(r)) { // the user belongs to this role, and it's not the one we want to remove
                if (valueType.equals(RBAC.ALLOW)) { // it's allowed
                    neededPermissions.remove(permission); // remove it from the perms we need (if it's in that set)
                }
            }
        }
        return neededPermissions.size() != 0; // do we still need perms? if so, it's a lockout
    }

    @Override
    public Class<DeleteRole> getActionType() {
        return DeleteRole.class;
    }

    @Override
    public void rollback(DeleteRole arg0, EmptyResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
