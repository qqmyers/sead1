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
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.TagEventBeanUtil;

public class TagResourceHandler implements ActionHandler<TagResource, TagResourceResult>{
	
	/** Tupelo bean session **/
	private static final BeanSession beanSession = TupeloStore.getInstance().getBeanSession();
	
	/** Datasets DAO **/
	private static DatasetBeanUtil dbu = new DatasetBeanUtil(beanSession);
	
	/** Commons logging **/
	private static Log log = LogFactory.getLog(TagResourceHandler.class);
	
	@Override
	public TagResourceResult execute(TagResource arg0, ExecutionContext arg1)
			throws ActionException {

		String uri = arg0.getId();
		
		Set<String> tags = arg0.getTags();
		
		TagEventBeanUtil tebu = new TagEventBeanUtil(beanSession);

		try {
			tebu.addTags(Resource.uriRef(uri), null, tags);
			log.debug("Tagging " + uri + " with tags " + tags);
		} catch (OperatorException e) {
			e.printStackTrace();
		}

		// make sure cached datasetbean contains the tag event
		try {
			DatasetBean datasetBean = dbu.get(uri);
			datasetBean = dbu.update(datasetBean);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
