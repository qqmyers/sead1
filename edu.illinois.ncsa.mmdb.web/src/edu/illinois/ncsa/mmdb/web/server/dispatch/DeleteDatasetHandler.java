package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;

import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteDatasetResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

/**
 * Delete dataset. Marks dataset as deleted but content still remains in the
 * repository.
 * 
 * @author Luigi Marini
 * 
 */
public class DeleteDatasetHandler implements
		ActionHandler<DeleteDataset, DeleteDatasetResult> {

	/** Commons logging **/
	private static Log log = LogFactory.getLog(DeleteDatasetHandler.class);
	
	@Override
	public DeleteDatasetResult execute(DeleteDataset arg0, ExecutionContext arg1)
			throws ActionException {
		String datasetUri = arg0.getUri();
		try {
			TupeloStore.getInstance().getContext().addTriple(
					Resource.uriRef(datasetUri), DcTerms.IS_REPLACED_BY, Rdf.NIL);
			log.debug("Dataset deleted" + datasetUri);
			return new DeleteDatasetResult(true);
		} catch (OperatorException e) {
			log.error("Error deleting dataset " + datasetUri, e);
			return new DeleteDatasetResult(false);
		}
	}

	@Override
	public Class<DeleteDataset> getActionType() {
		return DeleteDataset.class;
	}

	@Override
	public void rollback(DeleteDataset arg0, DeleteDatasetResult arg1,
			ExecutionContext arg2) throws ActionException {
		String datasetUri = arg0.getUri();
		try {
			TupeloStore.getInstance().getContext().removeTriple(
					Resource.uriRef(datasetUri), DcTerms.IS_REPLACED_BY, Rdf.NIL);
		} catch (OperatorException e) {
			throw new ActionException("unable to undelete dataset "
					+ datasetUri, e);
		}
	}
}
