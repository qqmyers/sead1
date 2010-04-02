package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import edu.illinois.ncsa.mmdb.web.client.dispatch.IsPreviewPending;
import edu.illinois.ncsa.mmdb.web.client.dispatch.IsPreviewPendingResult;
import edu.illinois.ncsa.mmdb.web.rest.RestServlet;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

public class IsPreviewPendingHandler implements ActionHandler<IsPreviewPending,IsPreviewPendingResult> {

    @Override
    public IsPreviewPendingResult execute(IsPreviewPending arg0, ExecutionContext arg1) throws ActionException {
        String uri = arg0.getUri();
        IsPreviewPendingResult result = new IsPreviewPendingResult();
        result.setReady(TupeloStore.getInstance().getPreview(uri, arg0.getSize()) != null);
        result.setPending(!RestServlet.shouldCache404(uri));
        return result;
    }

    @Override
    public Class<IsPreviewPending> getActionType() {
        // TODO Auto-generated method stub
        return IsPreviewPending.class;
    }

    @Override
    public void rollback(IsPreviewPending arg0, IsPreviewPendingResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
