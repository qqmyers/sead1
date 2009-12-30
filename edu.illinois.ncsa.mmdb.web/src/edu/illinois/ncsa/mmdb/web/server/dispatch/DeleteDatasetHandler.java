package edu.illinois.ncsa.mmdb.web.server.dispatch;

import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Rdf;

import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteDatasetResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

public class DeleteDatasetHandler implements ActionHandler<DeleteDataset,DeleteDatasetResult> {
	Resource IS_REPLACED_BY = Resource.uriRef("http://purl.org/dc/terms/isReplacedBy");
	
	public DeleteDatasetResult execute(DeleteDataset arg0, ExecutionContext arg1) throws ActionException {
		String datasetUri = arg0.getUri();
		try {
			TupeloStore.getInstance().getContext().addTriple(Resource.uriRef(datasetUri),IS_REPLACED_BY,Rdf.NIL);
			return new DeleteDatasetResult(true);
		} catch (OperatorException e) {
			return new DeleteDatasetResult(false);
		}
	}

	public Class<DeleteDataset> getActionType() {
		// TODO Auto-generated method stub
		return DeleteDataset.class;
	}

	public void rollback(DeleteDataset arg0, DeleteDatasetResult arg1,
			ExecutionContext arg2) throws ActionException {
		String datasetUri = arg0.getUri();
		try {
			TupeloStore.getInstance().getContext().removeTriple(Resource.uriRef(datasetUri),IS_REPLACED_BY,Rdf.NIL);
		} catch (OperatorException e) {
			throw new ActionException("unable to undelete dataset "+datasetUri,e);
		}
	}
}
