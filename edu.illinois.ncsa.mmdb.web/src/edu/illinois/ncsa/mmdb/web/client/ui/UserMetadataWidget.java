package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserMetadataFields;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserMetadataFieldsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.dispatch.RunSparqlQuery;
import edu.illinois.ncsa.mmdb.web.client.dispatch.RunSparqlQueryResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetProperty;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetPropertyResult;

public class UserMetadataWidget extends Composite {
	static String availableFieldsQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" + 
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
			"PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/>\r\n" + 
			"\r\n" + 
			"SELECT ?label ?f\r\n" + 
			"WHERE {\r\n" + 
			"  ?f <rdf:type> <cet:userMetadataField> .\r\n" + 
			"  ?f <rdfs:label> ?label .\r\n" + 
			"}\r\n" +
			"ORDER BY ASC(?label)";
	
	String uri;
	MyDispatchAsync dispatch;
	VerticalPanel mainPanel;
	LabeledListBox fieldChoice;
	TextArea valueText;
	
	public UserMetadataWidget(final String uri, final MyDispatchAsync dispatch) {
		this.uri = uri;
		this.dispatch = dispatch;
		
		mainPanel = new VerticalPanel();
		initWidget(mainPanel);
		
		dispatch.execute(new RunSparqlQuery(availableFieldsQuery),
				new AsyncCallback<RunSparqlQueryResult>() {
					public void onFailure(Throwable caught) {
					}
					public void onSuccess(RunSparqlQueryResult result) {
						if(result.getResult().size() > 0) {
							addFieldAddControls(result.getResult());
							dispatch.execute(new GetUserMetadataFields(uri),
									new AsyncCallback<GetUserMetadataFieldsResult>() {
										public void onFailure(Throwable caught) {
										}
										public void onSuccess(GetUserMetadataFieldsResult result) {
											for(String predicate : result.getFieldLabels().keySet()) {
												addNewField(predicate, result.getFieldLabels().get(predicate),
														result.getValues().get(predicate));
											}
										}
							});
						}
					}
		});
		
	}

	void addNewField(String predicate, String label, String value) {
		Set<String> v = new HashSet<String>();
		v.add(value);
		addNewField(predicate, label, v);
	}
	
	Map<String,VerticalPanel> fieldWidgets = new HashMap<String,VerticalPanel>();
	
	void addNewField(String predicate, String label, Collection<String> values) {
		Label name = new Label(label+":");
		
		VerticalPanel panel = fieldWidgets.get(predicate);
		if(panel == null) {
			panel = new VerticalPanel();
			fieldWidgets.put(predicate,panel);
			mainPanel.insert(panel,0);
		} else {
			panel.clear();
		}
		
		panel.add(name);
		for(String value : values) {
			panel.add(new Label(value));
		}
	}

	Map<String,String> labels = new HashMap<String,String>();
	
	void addFieldAddControls(List<List<String>> result) {
		fieldChoice = new LabeledListBox("Field:");
		for(List<String> entry : result) {
			String label = entry.get(0);
			String predicate = entry.get(1);
			fieldChoice.addItem(label, predicate);
			labels.put(predicate, label);
		}
		mainPanel.add(fieldChoice);
		
		valueText = new TextArea();
		mainPanel.add(valueText);
		
		HorizontalPanel buttons = new HorizontalPanel();
		mainPanel.add(buttons);
		
		Image addButton = new Image("./images/list-add.png");
		addButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addValue();
			}
		});
		
		Image removeButton = new Image("./images/list-remove.png");
		removeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				removeValue();
			}
		});
		
		buttons.add(addButton);
		buttons.add(removeButton);
	}
	
	void addValue() {
		final String property = fieldChoice.getSelected();
		dispatch.execute(new SetProperty(uri, property, valueText.getText()),
				new AsyncCallback<SetPropertyResult>() {
					public void onFailure(Throwable caught) {
					}
					public void onSuccess(SetPropertyResult result) {
						addNewField(property, labels.get(property), valueText.getText());
					}
		});
	}
	
	void removeValue() {
		final String property = fieldChoice.getSelected();
		dispatch.execute(new SetProperty(uri, property, new HashSet<String>()),
				new AsyncCallback<SetPropertyResult>() {
					public void onFailure(Throwable caught) {
					}
					public void onSuccess(SetPropertyResult result) {
						Widget w = fieldWidgets.get(property);
						if(w != null) {
							mainPanel.remove(w);
							fieldWidgets.remove(property);
						}
					}
		});
	}
}
