package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.mmdb.web.client.dispatch.DefaultTheRole;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.common.DefaultRole;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

public class DefaultRoleHandler implements ActionHandler<DefaultTheRole, EmptyResult> {
    Log log = LogFactory.getLog(DefaultRoleHandler.class);

    @Override
    public EmptyResult execute(DefaultTheRole arg0, ExecutionContext arg1) throws ActionException {
        TupeloStore ts = TupeloStore.getInstance();
        if (ts.isAllowed(arg0, Permission.EDIT_ROLES)) {
            try {
                Resource role = Resource.uriRef(arg0.getUri());
                for (DefaultRole defaultRole : DefaultRole.values() ) {
                    if (arg0.getUri().equals(defaultRole.getUri())) {
                        log.info("Defaulting role: " + defaultRole.getName());
                        ts.getRbac().deleteRole(role, false);
                        Resource roleUri = Resource.uriRef(defaultRole.getUri());
                        ts.getRbac().createRole(roleUri, defaultRole.getName(), defaultRole.getPermissions());
                    }
                }
                return new EmptyResult();
            } catch (OperatorException e) {
                log.error("unable to default role", e);
                throw new ActionException("Server error: " + e.getMessage(), e);
            }
        } else {
            log.debug("user " + arg0.getUser() + " is not authorized to default roles");
        }
        throw new ActionException("Unauthorized");
    }

    @Override
    public Class<DefaultTheRole> getActionType() {
        return DefaultTheRole.class;
    }

    @Override
    public void rollback(DefaultTheRole arg0, EmptyResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
