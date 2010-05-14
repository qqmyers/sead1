package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;

import edu.illinois.ncsa.mmdb.web.client.dispatch.BatchResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteDatasets;
import edu.illinois.ncsa.mmdb.web.server.AccessControl;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

public class DeleteDatasetsHandler implements ActionHandler<DeleteDatasets, BatchResult> {

    @Override
    public BatchResult execute(DeleteDatasets arg0, ExecutionContext arg1) throws ActionException {
        BatchResult result = new BatchResult();
        TripleWriter mod = new TripleWriter();
        boolean isAdmin = AccessControl.isAdmin(arg0.getUser());
        for (String datasetUri : arg0.getResources() ) {
            // check for authorization
            if (!isAdmin && !AccessControl.isCreator(arg0.getUser(), datasetUri)) {
                result.setFailure(datasetUri, "Unauthorized");
            } else {
                mod.add(Triple.create(Resource.uriRef(datasetUri), DcTerms.IS_REPLACED_BY, Rdf.NIL));
                result.addSuccess(datasetUri);
            }
        }
        try {
            TupeloStore.getInstance().getContext().perform(mod);
        } catch (OperatorException e) {
            throw new ActionException("delete failed completely", e);
        }
        return result;
    }

    @Override
    public Class<DeleteDatasets> getActionType() {
        return DeleteDatasets.class;
    }

    @Override
    public void rollback(DeleteDatasets arg0, BatchResult arg1, ExecutionContext arg2) throws ActionException {
        // FIXME implement
    }

}
