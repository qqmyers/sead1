/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.HashSet;
import java.util.Set;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetTags;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetTagsResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.TagEventBeanUtil;

/**
 * @author lmarini
 *
 *	TODO make TagBeanUtil associatable and switch to using that
 */
public class GetTagsHandler implements ActionHandler<GetTags, GetTagsResult>{

	/** Tupelo bean session **/
	private static final BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

	/** Tags DAO **/
	private static TagEventBeanUtil tebu = new TagEventBeanUtil(beanSession);

	
	@Override
	public GetTagsResult execute(GetTags arg0, ExecutionContext arg1)
			throws ActionException {

		Set<String> tags = new HashSet<String>();

		try {
			tags = tebu.getTags(Resource.uriRef(arg0.getId()));
		} catch (OperatorException e) {
			e.printStackTrace();
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
