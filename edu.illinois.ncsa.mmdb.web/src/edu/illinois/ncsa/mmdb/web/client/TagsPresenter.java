/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client;

import java.util.Set;

import com.google.gwt.event.shared.HandlerManager;

import edu.illinois.ncsa.mmdb.web.client.mvp.BasePresenter;
import edu.illinois.ncsa.mmdb.web.client.mvp.View;
import edu.uiuc.ncsa.cet.bean.TagEventBean;

/**
 * @author lmarini
 *
 */
public class TagsPresenter extends BasePresenter<TagsPresenter.TagsViewInterface> {

	public TagsPresenter(TagsViewInterface display, HandlerManager eventBus) {
		super(display, eventBus);
		// TODO Auto-generated constructor stub
	}

	interface TagsViewInterface extends View {

		
	}
	
	public void showTags(Set<TagEventBean> tagEvents) {

	}
}
