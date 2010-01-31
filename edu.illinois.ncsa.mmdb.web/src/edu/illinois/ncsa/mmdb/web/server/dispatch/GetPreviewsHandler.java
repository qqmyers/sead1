package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.Date;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.mortbay.log.Log;
import org.tupeloproject.kernel.ThingSession;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Cet;

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
		String datasetUri = getPreviews.getUri();
		GetPreviewsResult result = new GetPreviewsResult();
		try {
			if(datasetUri != null) {
				String smallPreview = RestServlet.getSmallPreviewUri(datasetUri);
				String largePreview = RestServlet.getLargePreviewUri(datasetUri);
				if(smallPreview != null) {
					result.setPreview(GetPreviews.SMALL, pibu.get(smallPreview));
				}
				if(largePreview != null) {
					result.setPreview(GetPreviews.LARGE, pibu.get(largePreview));
				}
				if(smallPreview == null && largePreview == null) { // no previews.
					ThingSession ts = TupeloStore.getInstance().getBeanSession().getThingSession();
					Date endTime =  ts.getDate(Resource.uriRef(datasetUri), Cet.cet("metadata/extractor/endTime"));
					if(endTime != null) {
						Log.info("END TIME = "+endTime);
						// there won't be previews, so stop asking!
						result.setStopAsking(true);
					}
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
