package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.ThingSession;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Dc;

import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetTitle;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.rbac.medici.Permission;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.medici.MediciRbac;

public class SetTitleHandler implements ActionHandler<SetTitle, EmptyResult> {
    Log log = LogFactory.getLog(SetTitleHandler.class);

    @Override
    public EmptyResult execute(SetTitle arg0, ExecutionContext arg1) throws ActionException {
        Context context = TupeloStore.getInstance().getContext();
        MediciRbac rbac = new MediciRbac(context);
        try {
            if (rbac.checkPermission(arg0.getUser(), arg0.getUri(), Permission.EDIT_METADATA)) {
                try {
                    Resource subject = Resource.uriRef(arg0.getUri());
                    ThingSession ts = new ThingSession(TupeloStore.getInstance().getContext());
                    ts.setValue(subject, Dc.TITLE, arg0.getTitle());
                    ts.save();
                    ts.close();
                    // attempt to refetch the bean
                    TupeloStore.refetch(subject);
                    TupeloStore.getInstance().changed(subject.getString());
                    log.debug("set title on " + arg0.getUri() + " to " + arg0.getTitle());
                } catch (OperatorException x) {
                    throw new ActionException("failed to set title", x);
                }
            } else {
                log.debug("no permission to set title on " + arg0.getUri());
            }
        } catch (RBACException x) {
            throw new ActionException("failed to check set metadata permission", x);
        }
        return new EmptyResult();
    }

    @Override
    public Class<SetTitle> getActionType() {
        // TODO Auto-generated method stub
        return SetTitle.class;
    }

    @Override
    public void rollback(SetTitle arg0, EmptyResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
