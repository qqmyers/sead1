/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollections;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollectionsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.uiuc.ncsa.cet.bean.CollectionBean;

/**
 * @author lmarini
 *
 */
public class AddToCollectionDialog extends DialogBox {

	private final ListBox list;
	private final Button submitButton;
	
	public AddToCollectionDialog(MyDispatchAsync service, ClickHandler clickHandler) {
		super();
		setAnimationEnabled(true);
		setGlassEnabled(true);
		setText("Add to collection");

		FlowPanel mainContainer = new FlowPanel();
		mainContainer.addStyleName("addToCollectionDialog");
		mainContainer.setSize("400px", "200px");
		setWidget(mainContainer);

		mainContainer.add(new Label("Select collection"));

		list = new ListBox();
		list.setVisibleItemCount(5);
		list.setWidth("300px");
		mainContainer.add(list);

		// retrieve collections
		service.execute(new GetCollections(),
				new AsyncCallback<GetCollectionsResult>() {

					@Override
					public void onFailure(Throwable arg0) {
						GWT.log("Error retrieving collections", arg0);
					}

					@Override
					public void onSuccess(GetCollectionsResult arg0) {
						for (CollectionBean collection : arg0.getCollections()) {
							list.addItem(collection.getTitle(), collection
									.getUri());
						}
					}
				});

		// buttons
		FlowPanel buttonsPanels = new FlowPanel();
		mainContainer.add(buttonsPanels);
		
		// submit button
		submitButton = new Button("Submit", clickHandler);
		buttonsPanels.add(submitButton);

		// close button
		Button closeButton = new Button("Cancel", new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {
				hide();
			}
		});
		buttonsPanels.add(closeButton);
		center();
	}

	public String getSelectedValue() {
		return list.getValue(list.getSelectedIndex());
	}
}
