package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;

import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.InitializeRoles;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.medici.MediciRbac;

public class InitializeRolesHandler implements ActionHandler<InitializeRoles, EmptyResult> {
    Log log = LogFactory.getLog(InitializeRolesHandler.class);

    @Override
    public EmptyResult execute(InitializeRoles arg0, ExecutionContext arg1) throws ActionException {
        MediciRbac rbac = new MediciRbac(TupeloStore.getInstance().getContext());

        try {
            log.info("Initializing role-based access control");
            rbac.intializeDefaultRoles();
            log.info("Finished initializing role-based access control");
        } catch (OperatorException x) {
            throw new ActionException("failed to initialize access control");
        }
        return new EmptyResult();
    }

    @Override
    public Class<InitializeRoles> getActionType() {
        // TODO Auto-generated method stub
        return InitializeRoles.class;
    }

    @Override
    public void rollback(InitializeRoles arg0, EmptyResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
