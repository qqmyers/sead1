package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.Set;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.mmdb.web.client.dispatch.TagResource;
import edu.illinois.ncsa.mmdb.web.client.dispatch.TagResourceResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.TagEventBeanUtil;

/**
 * Retrieve tags for a specific resource.
 * 
 * @author Luigi Mairini
 *
 */
public class TagResourceHandler implements ActionHandler<TagResource, TagResourceResult>{
	
	/** Commons logging **/
	private static Log log = LogFactory.getLog(TagResourceHandler.class);
	
	@Override
	public TagResourceResult execute(TagResource arg0, ExecutionContext arg1)
			throws ActionException {

		BeanSession beanSession = TupeloStore.getInstance().getBeanSession();
		
		TagEventBeanUtil tebu = new TagEventBeanUtil(beanSession);
		
		String uri = arg0.getUri();
		
		Set<String> tags = arg0.getTags();

		try {
			if(arg0.isDelete()) {
				tebu.removeTags(Resource.uriRef(uri), tags);
				for(String tag : tebu.getTags(arg0.getUri())) {
					if(tags.contains(tag)) {
						log.error("failed to delete tag "+tag);
					}
				}
				TupeloStore.refetch(uri);
				log.debug("removing tags "+tags+" from "+uri);
			} else {
				tebu.addTags(Resource.uriRef(uri), null, tags);
				log.debug("Tagging " + uri + " with tags " + tags);
			}
		} catch (OperatorException e) {
			log.error("Error tagging " + uri, e);
		}
		
		return new TagResourceResult();
	}

	@Override
	public Class<TagResource> getActionType() {
		return TagResource.class;
	}

	@Override
	public void rollback(TagResource arg0, TagResourceResult arg1,
			ExecutionContext arg2) throws ActionException {
		// TODO Auto-generated method stub
		
	}

}
