package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteRole;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.rbac.medici.Permission;

public class DeleteRoleHandler implements ActionHandler<DeleteRole, EmptyResult> {
    Log log = LogFactory.getLog(DeleteRoleHandler.class);

    @Override
    public EmptyResult execute(DeleteRole arg0, ExecutionContext arg1) throws ActionException {
        TupeloStore ts = TupeloStore.getInstance();
        if (ts.isAllowed(arg0, Permission.EDIT_ROLES)) {
            try {
                ts.getRbac().deleteRole(Resource.uriRef(arg0.getUri()));
                return new EmptyResult();
            } catch (OperatorException e) {
                log.error("unable to delete role", e);
                throw new ActionException("error", e);
            }
        } else {
            log.debug("user " + arg0.getUser() + " is not authorized to delete roles");
        }
        throw new ActionException("unauthorized");
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
