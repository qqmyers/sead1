package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.Collection;
import java.util.Date;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.EditUserRetirement;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.illinois.ncsa.mmdb.web.server.SEADRbac;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;

/**
 * Add/remove user retirement flag.
 *
 * @author myersjd@umich.edu
 *
 */
public class EditUserRetirementHandler implements ActionHandler<EditUserRetirement, EmptyResult> {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(EditUserRetirementHandler.class);

    @Override
    public EmptyResult execute(EditUserRetirement action, ExecutionContext arg1) throws ActionException {

        log.debug("Retirement request: Retire user = " + action.getTargetUser() + ", admin = " + action.getUser());
        if (action.getUser().equals(action.getTargetUser())) {
            log.debug("Attempt to retire self");
            throw new ActionException("Cannot retire self");
        }

        SEADRbac rbac = TupeloStore.getInstance().getRbac();
        try {
            if (!rbac.checkPermission(action.getUser(), Permission.EDIT_ROLES)) {
                log.warn("User doesn't have permission to edit roles");
                throw new ActionException("Cannot Edit Roles");

            }
        } catch (RBACException e) {
            log.error(e);
            throw new ActionException("Error determining permissions");
        }

        log.debug("user = " + action.getTargetUser() + ", admin = " + action.getUser());

        UriRef admin = Resource.uriRef(action.getUser());
        UriRef user = Resource.uriRef(action.getTargetUser());

        switch (action.getType()) {
            case ADD:
                removeRoles(user);
                setRetirement(user, true);
                break;
            case REMOVE:
                setRetirement(user, false);
                break;
            default:
                log.error("Edit action type not found" + action.getType());
                throw new ActionException("Error: bad request");
        }

        return new EmptyResult();
    }

    private void removeRoles(UriRef user) {
        SEADRbac rbac = TupeloStore.getInstance().getRbac();
        try {
            Collection<Resource> roles = rbac.getRoles(user);
            for (Resource role : roles ) {
                rbac.removeRole(user, role);
            }
        } catch (RBACException e) {
            log.warn("Unable to list user roles: " + user.toString(), e);

        } catch (OperatorException e) {
            log.warn("Unable to remove user roles: " + user.toString(), e);
        }

    }

    public static void setRetirement(UriRef person, boolean retire) {
        try {
            TripleWriter tw = new TripleWriter();
            if (retire) {
                tw.add(person, Cet.cet("retired"), new Date());
            }
            Unifier uf = new Unifier();
            uf.addPattern(person, Cet.cet("retired"), "date");
            uf.setColumnNames("date");
            TupeloStore.getInstance().getContext().perform(uf);
            for (Tuple<Resource> row : uf.getResult() ) {
                tw.remove(person, Cet.cet("retired"), row.get(0));
            }
            TupeloStore.getInstance().getContext().perform(tw);
        } catch (OperatorException ex) {
            log.warn(" FAILED to write retirement flag info for  " + person.getString(), ex);
        }
    }

    @Override
    public Class<EditUserRetirement> getActionType() {
        return EditUserRetirement.class;
    }

    @Override
    public void rollback(EditUserRetirement arg0, EmptyResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub
    }

}
