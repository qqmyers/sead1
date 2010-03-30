/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.LinkedHashMap;
import java.util.Set;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetTags;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetTagsResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.TagEventBeanUtil;

/**
 * Get tags associated with a particular resource.
 *  
 * @author Luigi Marini
 *
 */
public class GetTagsHandler implements ActionHandler<GetTags, GetTagsResult>{
	
	/** Commons logging **/
	private static Log log = LogFactory.getLog(GetTagsHandler.class);
	
	@Override
	public GetTagsResult execute(GetTags arg0, ExecutionContext arg1)
			throws ActionException {

		BeanSession beanSession = TupeloStore.getInstance().getBeanSession();
		
		TagEventBeanUtil tebu = new TagEventBeanUtil(beanSession);
		
		LinkedHashMap<String, Integer> tags = new LinkedHashMap<String, Integer>();

		try {
			Set<String> tagsSet = tebu.getTags(Resource.uriRef(arg0.getUri()));
			for (String tag : tagsSet) {
				tags.put(tag, 1);
			}
		} catch (OperatorException e) {
			log.error("Error getting tags for " + arg0.getUri(), e);
		}

		return new GetTagsResult(tags);
	}

	@Override
	public Class<GetTags> getActionType() {
		return GetTags.class;
	}

	@Override
	public void rollback(GetTags arg0, GetTagsResult arg1, ExecutionContext arg2)
			throws ActionException {
		// TODO Auto-generated method stub
		
	}

}
