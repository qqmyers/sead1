package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.Date;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.ThingSession;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Cet;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviewsResult;
import edu.illinois.ncsa.mmdb.web.rest.RestServlet;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.PreviewImageBeanUtil;

/**
 * Get image previews.
 * 
 * @author Luigi Marini
 * @author Joe Futrelle
 * 
 */
public class GetPreviewsHandler implements
		ActionHandler<GetPreviews, GetPreviewsResult> {

	/** Commons logging **/
	private static Log log = LogFactory.getLog(GetPreviewsHandler.class);

	@Override
	public GetPreviewsResult execute(GetPreviews getPreviewsAction,
			ExecutionContext arg1) throws ActionException {
		
		PreviewImageBeanUtil pibu = new PreviewImageBeanUtil(TupeloStore
				.getInstance().getBeanSession());
		
		String datasetUri = getPreviewsAction.getUri();
		
		GetPreviewsResult result = new GetPreviewsResult();
		
		try {
			if (datasetUri != null) {
				String smallPreview = RestServlet.getSmallPreviewUri(datasetUri);
				String largePreview = RestServlet.getLargePreviewUri(datasetUri);
				// in the following case this is a collection
				String collectionPreview = RestServlet.getCollectionPreviewUri(datasetUri); 
				if (smallPreview != null) {
					result.setPreview(GetPreviews.SMALL, pibu.get(smallPreview));
				}
				if (largePreview != null) {
					result.setPreview(GetPreviews.LARGE, pibu.get(largePreview));
				}
				if(collectionPreview != null) {
					result.setPreview(GetPreviews.BADGE, pibu.get(collectionPreview));
				}
				if (smallPreview == null && largePreview == null && collectionPreview == null) { // no previews.
					TupeloStore.refetch(datasetUri);
					ThingSession ts = TupeloStore.getInstance().getBeanSession().getThingSession();
					// FIXME "endTime1" is a kludgy way to represent execution stage information
					Date endTime = ts.getDate(Resource.uriRef(datasetUri), Cet.cet("metadata/extractor/endTime1"));
					log.debug("End time on "+datasetUri+" = "+endTime); // FIXME debug
					if (endTime != null) {
						// there won't be previews, so stop asking!
						log.debug("Telling client to stop asking for previews for "+ datasetUri);
						result.setStopAsking(true);
					}
				}
			}
		} catch (Exception x) {
			log.error("Error getting previews", x);
			// FIXME report
		}
		return result;
	}

	@Override
	public Class<GetPreviews> getActionType() {
		return GetPreviews.class;
	}

	@Override
	public void rollback(GetPreviews arg0, GetPreviewsResult arg1,
			ExecutionContext arg2) throws ActionException {
		// TODO Auto-generated method stub

	}

}
