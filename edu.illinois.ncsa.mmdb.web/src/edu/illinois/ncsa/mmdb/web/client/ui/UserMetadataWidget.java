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
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

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
	LabeledListBox fieldChoice;
	TextArea valueText;
	FlexTable fieldTable;

	public UserMetadataWidget(final String uri, final MyDispatchAsync dispatch) {
		this.uri = uri;
		this.dispatch = dispatch;

		fieldTable = new FlexTable();
		fieldTable.setWidth("100%");
		initWidget(fieldTable);
		
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

	void setProperty(String predicate, String label, String value) {
		Set<String> v = new HashSet<String>();
		v.add(value);
		addNewField(predicate, label, v);
	}

	int getRowForField(String predicate) {
		for(int row = 0; row < fieldTable.getRowCount()-1; row++) {
			Label l = (Label) fieldTable.getWidget(row, 0);
			if(predicate.equals(l.getTitle())) {
				return row;
			}
		}
		return -1;
	}
	
	void addNewField(final String predicate, String label, Collection<String> values) {
		int row = getRowForField(predicate);
		if(row == -1) {
			row = fieldTable.getRowCount()-1;
			fieldTable.insertRow(row);
		}

		Label predicateLabel = new Label(label);
		predicateLabel.setTitle(predicate);
		fieldTable.setWidget(row, 0, predicateLabel);
		VerticalPanel panel = new VerticalPanel();
		for(String value : values) {
			panel.add(new Label(value));
		}
		fieldTable.setWidget(row,1,panel);
		Button removeButton = new Button("Remove");
		removeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				removeValue(predicate);
			}
		});
		fieldTable.setWidget(row,2,removeButton);
	}

	Map<String,String> labels = new HashMap<String,String>();
	
	void addFieldAddControls(List<List<String>> result) {
		fieldChoice = new LabeledListBox("Set field:");
		for(List<String> entry : result) {
			String label = entry.get(0);
			String predicate = entry.get(1);
			fieldChoice.addItem(label, predicate);
			labels.put(predicate, label);
		}
		
		int row = fieldTable.getRowCount();
		
		fieldTable.setWidget(row,0,fieldChoice);

		VerticalPanel valuePanel = new VerticalPanel();
		valuePanel.add(new Label("Value to set it to:"));
		valueText = new TextArea();
		valueText.setWidth("100%");
		valuePanel.add(valueText);
		fieldTable.setWidget(row,1,valuePanel);
		
		Button addButton = new Button("Set value");
		addButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addValue();
			}
		});
		fieldTable.setWidget(row,2,addButton);
	}
	
	void addValue() {
		final String property = fieldChoice.getSelected();
		dispatch.execute(new SetProperty(uri, property, valueText.getText()),
				new AsyncCallback<SetPropertyResult>() {
					public void onFailure(Throwable caught) {
					}
					public void onSuccess(SetPropertyResult result) {
						setProperty(property, labels.get(property), valueText.getText());
					}
		});
	}
	
	void removeValue(final String property) {
		dispatch.execute(new SetProperty(uri, property, new HashSet<String>()),
				new AsyncCallback<SetPropertyResult>() {
					public void onFailure(Throwable caught) {
					}
					public void onSuccess(SetPropertyResult result) {
						int row = getRowForField(property);
						if(row != -1) {
							fieldTable.removeRow(row);
						}
					}
		});
	}
}
