/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.OperatorException;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetAllTags;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetTagsResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.TagBean;
import edu.uiuc.ncsa.cet.bean.TagEventBean;
import edu.uiuc.ncsa.cet.bean.tupelo.TagEventBeanUtil;

/**
 * Return all tags in the system.
 * 
 * @author Luigi Marini
 *
 */
public class GetAllTagsHandler implements ActionHandler<GetAllTags, GetTagsResult> {

	/** Commons logging **/
	private static Log log = LogFactory.getLog(GetAllTags.class);
	
	@Override
	public GetTagsResult execute(GetAllTags arg0, ExecutionContext arg1)
			throws ActionException {
		BeanSession beanSession = TupeloStore.getInstance().getBeanSession();
		
		TagEventBeanUtil tebu = new TagEventBeanUtil(beanSession);
		
		List<String> tags = new ArrayList<String>();

		Collection<TagEventBean> allTags = new HashSet<TagEventBean>();
		
		try {
			allTags = tebu.getAll();
		} catch (OperatorException e) {
			log.error("Error getting tags", e);
		} catch (Exception e) {
			log.error("Error getting tags", e);
		}
		
		Iterator<TagEventBean> iterator = allTags.iterator();
		while (iterator.hasNext()) {
			TagEventBean next = iterator.next();
			Set<TagBean> tags2 = next.getTags();
			Iterator<TagBean> iterator2 = tags2.iterator();
			while (iterator2.hasNext()) {
				TagBean next2 = iterator2.next();
				String tagString = next2.getTagString();
				if (!tags.contains(tagString)) {
					tags.add(tagString);
				}
			}
		}
		
		// sort
		Collections.sort(tags);

		return new GetTagsResult(tags);
	}

	@Override
	public Class<GetAllTags> getActionType() {
		return GetAllTags.class;
	}

	@Override
	public void rollback(GetAllTags arg0, GetTagsResult arg1,
			ExecutionContext arg2) throws ActionException {
		// TODO Auto-generated method stub
		
	}

}
