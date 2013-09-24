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

import edu.illinois.ncsa.mmdb.web.client.dispatch.PermissionSetting;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetPermissions;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetPermissionsResult;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.illinois.ncsa.mmdb.web.server.SEADRbac;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.rbac.medici.PermissionValue;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBAC;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;

public class SetPermissionsHandler implements ActionHandler<SetPermissions, SetPermissionsResult> {
    private static Log log = LogFactory.getLog(SetPermissionsHandler.class);

    public SetPermissionsResult execute(SetPermissions action, ExecutionContext arg1) throws ActionException {
        SEADRbac rbac = TupeloStore.getInstance().getRbac();

        // for now, iterate over the settings
        for (PermissionSetting s : action.getSettings() ) {
            Resource role = Resource.uriRef(s.getRoleUri());
            Resource permission = Resource.uriRef(s.getPermission().getUri());

            Resource value;
            switch (s.getValue()) { // there are only ever these three values
                case ALLOW:
                    value = RBAC.ALLOW;
                    break;
                case DENY:
                    value = RBAC.DENY;
                    break;
                case DO_NOT_ALLOW:
                    value = RBAC.DO_NOT_ALLOW;
                    break;
                default:
                    value = RBAC.DO_NOT_ALLOW;
                    break;
            }

            try {
                if (!lockout(rbac, action.getUser(), s)) {
                    rbac.setPermissionValue(role, permission, value);
                } else {
                    throw new ActionException("Changing this permission would lock you out of the system.");
                }
                log.info("Set " + s.getPermission().getLabel() + " permission on role " + s.getRoleUri() + " to " + s.getValue().getName());
            } catch (OperatorException e) {
                log.error("Unable to set " + s.getPermission().getLabel() + " permission on role " + s.getRoleUri(), e);
                throw new ActionException("Server error: " + e.getMessage(), e);
            } catch (RBACException e) {
                // TODO Auto-generated catch block
                log.error("Unable to set " + s.getPermission().getLabel() + " permission on role " + s.getRoleUri(), e);
                throw new ActionException("Server error: " + e.getMessage(), e);
            }
        }

        return new SetPermissionsResult();
    }

    // would changing this session lock the authenticated user out of rbac admin?
    boolean lockout(RBAC rbac, String u, PermissionSetting setting) throws RBACException {
        if (setting.getValue() == PermissionValue.ALLOW) { // we're allowing, so no prob
            return false;
        }
        Resource permission = Resource.uriRef(setting.getPermission().getUri());
        Set<Resource> neededPermissions = new HashSet<Resource>(); // set of permissions needed to administer roles
        neededPermissions.add(Resource.uriRef(Permission.VIEW_MEMBER_PAGES.getUri())); // get to the home page
        neededPermissions.add(Resource.uriRef(Permission.VIEW_ADMIN_PAGES.getUri())); // get to the admin tab
        neededPermissions.add(Resource.uriRef(Permission.EDIT_ROLES.getUri())); // edit roles
        if (!neededPermissions.contains(permission)) { // we're not removing any of the needed permissions, so no prob
            return false;
        }
        Resource user = Resource.uriRef(u);
        Resource role = Resource.uriRef(setting.getRoleUri());
        Collection<Resource> userRoles = rbac.getRoles(user); // roles the user belongs to
        for (Tuple<Resource> row : rbac.getGlobalPermissions(null) ) { // in all permissions
            int i = 0;
            Resource r = row.get(i++);
            Resource p = row.get(i++);
            boolean allow = row.get(i++).equals(RBAC.ALLOW);
            // here's the logic. we already know that we're denying a needed permission on some role.
            // given this global permission, if it's on a role that the user belongs to, and it's one of the permissions,
            // and it's allowed, and it's not the role *and* permission we're denying, then that takes care of that permission
            // and we no longer need it.
            if (allow && userRoles.contains(r) && neededPermissions.contains(p) && !(r.equals(role) && p.equals(permission))) {
                neededPermissions.remove(p);
            }
        }
        return neededPermissions.size() != 0; // do we still need perms? if so, it's a lockout
    }

    @Override
    public Class<SetPermissions> getActionType() {
        return SetPermissions.class;
    }

    @Override
    public void rollback(SetPermissions arg0, SetPermissionsResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
