/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.Iterator;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetAllTags;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetTagsResult;

/**
 * A page listing all tags in the system.
 * 
 * @author Luigi Marini
 *
 */
public class TagsPage extends Page {

	private FlowPanel tagsPanel;

	public TagsPage(DispatchAsync dispatchAsync) {
		super("Tags", dispatchAsync);
		getTags();
	}

	private void getTags() {
		dispatchAsync.execute(new GetAllTags(), new AsyncCallback<GetTagsResult>() {

			@Override
			public void onFailure(Throwable caught) {
				GWT.log("Error getting tags", caught);
			}

			@Override
			public void onSuccess(GetTagsResult result) {
				Iterator<String> iterator = result.getTags().iterator();
				while (iterator.hasNext()) {
					String tag = iterator.next();
					Hyperlink link = new Hyperlink(tag, "tag?title=" + tag);
					link.addStyleName("tagInPanel");
					tagsPanel.add(link);
				}
			}
			
		});
	}

	@Override
	public void layout() {
		tagsPanel = new FlowPanel();
		tagsPanel.addStyleName("tagsPanel");
		mainLayoutPanel.add(tagsPanel);
	}
}