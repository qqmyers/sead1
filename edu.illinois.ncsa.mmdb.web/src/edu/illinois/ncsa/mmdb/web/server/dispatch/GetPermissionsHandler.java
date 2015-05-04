package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPermissions;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPermissionsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.PermissionSetting;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.rbac.medici.PermissionValue;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBAC;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;

public class GetPermissionsHandler implements ActionHandler<GetPermissions, GetPermissionsResult> {
    Log log = LogFactory.getLog(GetPermissionsHandler.class);

    @Override
    public GetPermissionsResult execute(GetPermissions action, ExecutionContext arg1) throws ActionException {
        RBAC rbac = new RBAC(TupeloStore.getInstance().getContext());

        try {
            GetPermissionsResult result = new GetPermissionsResult();
            String accessPredicate = TupeloStore.getInstance().getConfiguration(ConfigurationKey.AccessLevelPredicate);
            int accessLevel = TupeloStore.getInstance().getConfiguration(ConfigurationKey.AccessLevelValues).split("[ ]*,[ ]*").length - 1;
            for (Tuple<Resource> row : rbac.getGlobalPermissions(accessPredicate) ) {
                Resource role = row.get(0); // the uri of a role
                Resource permission = row.get(1); // the uri of a permission
                Resource valueType = row.get(2); // the value type (e.g., ALLOW, DENY, DO_NOT_ALLOW)
                String roleName = row.get(3).getString();

                PermissionValue v = PermissionValue.DO_NOT_ALLOW;
                if (valueType.equals(RBAC.ALLOW)) {
                    v = PermissionValue.ALLOW;
                } else if (valueType.equals(RBAC.DENY)) {
                    v = PermissionValue.DENY;
                }

                if (!result.getAccessLevel().containsKey(row.get(0).toString())) {
                    int val = (row.get(5) != null) ? Integer.parseInt(row.get(5).toString()) : accessLevel;
                    if (val > accessLevel) {
                        val = accessLevel;
                    }
                    result.getAccessLevel().put(row.get(0).toString(), val);
                }

                Permission p = null;
                for (Permission c : Permission.values() ) {
                    if (c.getUri().equals(permission.getString())) {
                        p = c;
                    }
                }

                //log.trace("Permission " + p.getLabel() + " is set to " + v.getName() + " for " + roleName);
                result.addSetting(new PermissionSetting(role.getString(), p, v, roleName));
            }

            return result;
        } catch (RBACException e) {
            throw new ActionException("Cannot get permissions", e);
        }
        // TODO: register this handler
    }

    @Override
    public Class<GetPermissions> getActionType() {
        return GetPermissions.class;
    }

    @Override
    public void rollback(GetPermissions arg0, GetPermissionsResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
