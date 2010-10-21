package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.InitializeRoles;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.rbac.medici.Permission;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBAC;

public class InitializeRolesHandler implements ActionHandler<InitializeRoles, EmptyResult> {
    Log log = LogFactory.getLog(InitializeRolesHandler.class);

    @Override
    public EmptyResult execute(InitializeRoles arg0, ExecutionContext arg1) throws ActionException {
        RBAC rbac = new RBAC(TupeloStore.getInstance().getContext());

        try {
            log.info("Initializing role-based access control");
            rbac.intialize();
            for (Permission p : Permission.values() ) {
                String uri = p.getUri();
                String label = p.getLabel();
                log.info("Creating permission '" + label + "'");
                rbac.createPermission(Resource.uriRef(uri), label);
            }
            // now create some roles
            for (String label : new String[] { "Administrator", "Author", "Reviewer", "Viewer" } ) {
                log.info("Creating role '" + label + "'");
                Resource roleUri = Resource.uriRef("http://medici.ncsa.illinois.edu/ns/" + label);
                rbac.createRole(roleUri, label);
                for (Permission p : Permission.values() ) {
                    Resource pu = Resource.uriRef(p.getUri());
                    rbac.setPermissionValue(roleUri, pu, RBAC.DO_NOT_ALLOW);
                }
            }
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
