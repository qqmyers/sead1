/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;

import edu.uiuc.ncsa.cet.bean.CollectionBean;

/**
 * @author lmarini
 * 
 */
public class CollectionMembershipWidget extends Composite {

	FlowPanel mainContainer;
	private final Label titleLabel;

	public CollectionMembershipWidget() {
		mainContainer = new FlowPanel();
		mainContainer.addStyleName("datasetRightColSection");
		initWidget(mainContainer);

		titleLabel = new Label("Collections");
		titleLabel.addStyleName("datasetRightColHeading");
		mainContainer.add(titleLabel);
	}

	public void addCollection(CollectionBean collection) {
		Hyperlink link = new Hyperlink(collection.getTitle(), "collection?uri="
				+ collection.getUri());
		mainContainer.add(link);
	}
}
