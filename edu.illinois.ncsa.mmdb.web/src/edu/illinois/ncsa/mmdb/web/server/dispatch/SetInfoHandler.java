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
import org.tupeloproject.rdf.terms.Files;

import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetInfo;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.rbac.medici.Permission;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.medici.MediciRbac;

public class SetInfoHandler implements ActionHandler<SetInfo, EmptyResult> {
    Log log = LogFactory.getLog(SetInfoHandler.class);

    @Override
    public EmptyResult execute(SetInfo arg0, ExecutionContext arg1) throws ActionException {
        Context context = TupeloStore.getInstance().getContext();
        MediciRbac rbac = new MediciRbac(context);
        try {
            if (rbac.checkPermission(arg0.getUser(), arg0.getUri(), Permission.EDIT_METADATA)) {
                try {
                    Resource subject = Resource.uriRef(arg0.getUri());
                    ThingSession ts = new ThingSession(TupeloStore.getInstance().getContext());
                    // change the value of corresponding info depending on the input. (Another one will be null.)
                    if (arg0.getMimetype() != null) {
                        ts.setValue(subject, Dc.FORMAT, arg0.getMimetype());
                        ts.save();
                        log.debug("set MIME type on " + arg0.getUri() + " to " + arg0.getMimetype());
                    }
                    if (arg0.getFilename() != null) {
                        ts.setValue(subject, Files.HAS_NAME, arg0.getFilename());
                        ts.save();
                        log.debug("set file name on " + arg0.getUri() + " to " + arg0.getFilename());
                    }
                    ts.close();

                    // attempt to refetch the bean
                    TupeloStore.refetch(subject);
                    TupeloStore.getInstance().changed(subject.getString());
                } catch (OperatorException x) {
                    throw new ActionException("failed to set info", x);
                }
            } else {
                log.debug("no permission to set info on " + arg0.getUri());
            }
        } catch (RBACException x) {
            throw new ActionException("failed to check set metadata permission", x);
        }
        return new EmptyResult();
    }

    @Override
    public Class<SetInfo> getActionType() {
        // TODO Auto-generated method stub
        return SetInfo.class;
    }

    @Override
    public void rollback(SetInfo arg0, EmptyResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
