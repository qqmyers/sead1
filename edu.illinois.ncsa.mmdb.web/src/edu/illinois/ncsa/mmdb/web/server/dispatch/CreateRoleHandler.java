package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.EnumSet;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.mmdb.web.client.dispatch.CreateRole;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SubjectResult;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

public class CreateRoleHandler implements ActionHandler<CreateRole, SubjectResult> {
    Log log = LogFactory.getLog(CreateRoleHandler.class);

    @Override
    public SubjectResult execute(CreateRole arg0, ExecutionContext arg1) throws ActionException {
        TupeloStore ts = TupeloStore.getInstance();
        if (ts.isAllowed(arg0, Permission.EDIT_ROLES)) {
            try {
                Resource roleUri = Resource.uriRef(); // mint
                EnumSet<Permission> none = EnumSet.noneOf(Permission.class);
                ts.getRbac().createRole(roleUri, arg0.getName(), none);
                return new SubjectResult(roleUri.getString());
            } catch (OperatorException e) {
                log.error("unable to create role", e);
                throw new ActionException("error", e);
            }
        } else {
            log.debug("user " + arg0.getUser() + " is not authorized to create roles");
        }
        throw new ActionException("unauthorized");
    }

    @Override
    public Class<CreateRole> getActionType() {
        return CreateRole.class;
    }

    @Override
    public void rollback(CreateRole arg0, SubjectResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
