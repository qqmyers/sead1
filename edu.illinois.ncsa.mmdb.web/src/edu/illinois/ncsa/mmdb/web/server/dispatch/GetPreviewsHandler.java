package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviewsResult;
import edu.illinois.ncsa.mmdb.web.rest.RestServlet;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.PreviewImageBeanUtil;

public class GetPreviewsHandler implements ActionHandler<GetPreviews, GetPreviewsResult> {

	@Override
	public GetPreviewsResult execute(GetPreviews getPreviews, ExecutionContext arg1)
			throws ActionException {
		PreviewImageBeanUtil pibu = new PreviewImageBeanUtil(TupeloStore.getInstance().getBeanSession());
		String datasetUri = getPreviews.getDatasetUri();
		GetPreviewsResult result = new GetPreviewsResult();
		try {
			if(datasetUri != null) {
				String smallPreview = RestServlet.getSmallPreviewUri(datasetUri);
				if(smallPreview != null) {
					result.setPreview(GetPreviews.SMALL, pibu.get(smallPreview));
				}
				String largePreview = RestServlet.getLargePreviewUri(datasetUri);
				if(largePreview != null) {
					result.setPreview(GetPreviews.LARGE, pibu.get(largePreview));
				}
			}
		} catch(Exception x) {
			// FIXME report
		}
		return result;
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
