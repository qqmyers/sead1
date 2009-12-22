package edu.illinois.ncsa.mmdb.web.server.dispatch;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUploadDestination;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUploadDestinationResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

public class GetUploadDestinationHandler implements ActionHandler<GetUploadDestination,GetUploadDestinationResult> {

	@Override
	public GetUploadDestinationResult execute(GetUploadDestination arg0,
			ExecutionContext arg1) throws ActionException {
		String token = TupeloStore.getInstance().getHistoryForUpload(arg0.getSessionKey());
		GetUploadDestinationResult result = new GetUploadDestinationResult();
		result.setHistoryToken(token);
		return result;
	}

	@Override
	public Class<GetUploadDestination> getActionType() {
		// TODO Auto-generated method stub
		return GetUploadDestination.class;
	}

	@Override
	public void rollback(GetUploadDestination arg0,
			GetUploadDestinationResult arg1, ExecutionContext arg2)
			throws ActionException {
		// TODO Auto-generated method stub
		
	}

}
