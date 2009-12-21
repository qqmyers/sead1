package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.LinkedList;
import java.util.List;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviewsResult;
import edu.illinois.ncsa.mmdb.web.rest.RestServlet;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

public class GetPreviewsHandler implements ActionHandler<GetPreviews, GetPreviewsResult> {

	@Override
	public GetPreviewsResult execute(GetPreviews getPreviews, ExecutionContext arg1)
			throws ActionException {
		List<String> previews = new LinkedList<String>();
		String datasetUri = getPreviews.getDatasetUri();
		if(datasetUri != null) {
			String smallPreview = RestServlet.getSmallPreviewUri(datasetUri);
			if(smallPreview != null) { previews.add(smallPreview); }
			String largePreview = RestServlet.getLargePreviewUri(datasetUri);
			if(largePreview != null) { previews.add(largePreview); }
		}
		return new GetPreviewsResult(previews);
	}

	@Override
	public Class<GetPreviews> getActionType() {
		// TODO Auto-generated method stub
		return GetPreviews.class;
	}

	@Override
	public void rollback(GetPreviews arg0, GetPreviewsResult arg1,
			ExecutionContext arg2) throws ActionException {
		// TODO Auto-generated method stub
		
	}

}
