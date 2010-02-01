package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.dispatch.RunSparqlQuery;
import edu.illinois.ncsa.mmdb.web.client.dispatch.RunSparqlQueryResult;

public class SparqlPage extends Page {
	VerticalPanel inputPanel;
	FlexTable resultsTable;
	
	static String tagQuery = "PREFIX dc: <http://purl.org/dc/elements/1.1/>\r\n" + 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" + 
			"PREFIX tags: <http://www.holygoat.co.uk/owl/redwood/0.1/tags/>\r\n" + 
			"SELECT ?dataset ?o ?_ldMimeType ?_ldTitle ?_ued\r\n" + 
			"WHERE { ?dataset <tags:tag> ?event .\r\n" + 
			" ?event <tags:associatedTag> ?tag .\r\n" + 
			" ?tag <tags:name> 'art' .\r\n" + 
			" ?dataset <rdf:type> <cet:Dataset> .\r\n" + 
			" ?dataset <dc:date> ?o .\r\n" + 
			" ?dataset <dc:format> ?_ldMimeType .\r\n" + 
			" ?dataset <dc:title> ?_ldTitle .\r\n" + 
			" OPTIONAL { ?dataset <http://purl.org/dc/terms/isReplacedBy> ?_ued . }\r\n" + 
			"} order by ?_ued ?o limit 100";
	static String datasetListQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" + 
			"PREFIX dc: <http://purl.org/dc/elements/1.1/>\r\n" + 
			"PREFIX dcterms: <http://purl.org/dc/terms/>\r\n" + 
			"PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/>\r\n" + 
			"\r\n" + 
			"SELECT ?s ?t ?d ?r\r\n" + 
			"WHERE {\r\n" + 
			"  ?s <rdf:type> <cet:Dataset> .\r\n" + 
			"  ?s <dc:title> ?t .\r\n" + 
			"  ?s <dc:date> ?d .\r\n" + 
			"  OPTIONAL { ?s <dcterms:isReplacedBy> ?r } .\r\n" + 
			"}\r\n" + 
			"ORDER BY ASC(?r) DESC(?d) \r\n" + 
			"LIMIT 15";
	
	static Map<String,String> exampleQueries = new HashMap<String,String>();
	static {
		exampleQueries.put("tag",tagQuery);
		exampleQueries.put("dataset list",datasetListQuery);
	}

	public SparqlPage(final MyDispatchAsync dispatchAsync) {
		super("SPARQL",dispatchAsync);

		inputPanel = new VerticalPanel();
		mainLayoutPanel.add(inputPanel);
		
		LabeledListBox examples = new LabeledListBox("example queries: ");
		for(String option : new TreeSet<String>(exampleQueries.keySet())) {
			examples.addItem(option, option);
		}
		inputPanel.add(examples);
		
		//
		final TextArea queryBox = new TextArea();
		queryBox.setCharacterWidth(90);
		queryBox.setVisibleLines(15);
		inputPanel.add(queryBox);

		examples.addValueChangeHandler(new ValueChangeHandler<String>() {
			public void onValueChange(ValueChangeEvent<String> event) {
				queryBox.setText(exampleQueries.get(event.getValue()));
			}
		});

		Button submit = new Button("Execute");
		submit.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				RunSparqlQuery action = new RunSparqlQuery();
				action.setQuery(queryBox.getText());
				dispatchAsync.execute(action, new AsyncCallback<RunSparqlQueryResult>() {
					public void onFailure(Throwable caught) {
						Window.alert("SPARQL query failed");
					}
					public void onSuccess(RunSparqlQueryResult result) {
						resultsTable.removeAllRows();
						int row = 0;
						for(List<String> resultRow : result.getResult()) {
							int col = 0;
							for(String rowCell : resultRow) {
								resultsTable.setWidget(row, col, new Label(rowCell != null ? rowCell : "null")); 
								resultsTable.getCellFormatter().addStyleName(row, col, "datasetTable");
								col++;
							}
							row++;
						}
					}
				});
			}
		});
		inputPanel.add(submit);
		
		resultsTable = new FlexTable();
		resultsTable.addStyleName("datasetTable");
		inputPanel.add(resultsTable);
	}
}
