package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.mmdb.web.client.dispatch.PermissionSetting;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetPermissions;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetPermissionsResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBAC;

public class SetPermissionsHandler implements ActionHandler<SetPermissions, SetPermissionsResult> {
    private static Log log = LogFactory.getLog(SetPermissionsHandler.class);

    public SetPermissionsResult execute(SetPermissions action, ExecutionContext arg1) throws ActionException {

        // TODO: register this handler

        RBAC rbac = new RBAC(TupeloStore.getInstance().getContext());

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
                rbac.setPermissionValue(role, permission, value);
                log.info("Set " + s.getPermission().getLabel() + " permission on role " + s.getRoleUri() + " to " + s.getValue().getName());
            } catch (OperatorException e) {
                log.error("Unable to set " + s.getPermission().getLabel() + " permission on role " + s.getRoleUri(), e);
            }
        }

        return new SetPermissionsResult();
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
