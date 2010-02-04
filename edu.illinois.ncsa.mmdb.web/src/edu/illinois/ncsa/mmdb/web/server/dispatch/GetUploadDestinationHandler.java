package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUploadDestination;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUploadDestinationResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

/**
 * TODO Add comments
 * 
 * @author Joe Futrelle
 * 
 */
public class GetUploadDestinationHandler implements
		ActionHandler<GetUploadDestination, GetUploadDestinationResult> {

	@Override
	public GetUploadDestinationResult execute(GetUploadDestination arg0,
			ExecutionContext arg1) throws ActionException {
		String token = TupeloStore.getInstance().getHistoryForUpload(
				arg0.getSessionKey());
		GetUploadDestinationResult result = new GetUploadDestinationResult();
		result.setHistoryToken(token);
		return result;
	}

	@Override
	public Class<GetUploadDestination> getActionType() {
		return GetUploadDestination.class;
	}

	@Override
	public void rollback(GetUploadDestination arg0,
			GetUploadDestinationResult arg1, ExecutionContext arg2)
			throws ActionException {
		// TODO Auto-generated method stub

	}

}
