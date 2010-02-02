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
	static String singleDataset = "SELECT ?p ?o\r\n" + 
			"WHERE {\r\n" + 
			"<tag:medici@uiuc.edu,2009:data_hkSgQzM1BRFoR1O7OKDqGA> ?p ?o .\r\n" + 
			"}";
	static String derivationQuery = "PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/> \r\n" + 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \r\n" + 
			"SELECT ?input ?output\r\n" + 
			"WHERE { \r\n" + 
			" ?step <cet:workflow/step/hasOutput> ?o1 . \r\n" + 
			" ?step <cet:workflow/step/hasInput> ?i1 . \r\n" + 
			" ?o2 <cet:workflow/datalist/hasData> ?o3 . \r\n" + 
			" ?o3 ?o3s ?output .\r\n" + 
			" ?output <rdf:type> <cet:Dataset> . \r\n" + 
			" ?o1 ?o1s ?o2 . \r\n" + 
			" ?i1 ?i1s ?i2 . \r\n" + 
			" ?i2 <cet:workflow/datalist/hasData> ?i3 . \r\n" + 
			" ?i3 ?i3s ?input . \r\n" + 
			" ?input <rdf:type> <cet:Dataset> . \r\n" + 
			"}\r\n" + 
			"LIMIT 20";
	static String prefixes = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" + 
			"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\r\n" + 
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
			"PREFIX dc: <http://purl.org/dc/elements/1.1/>\r\n" + 
			"PREFIX dcterms: <ttp://purl.org/dc/terms/>\r\n" + 
			"PREFIX dctypes: <ttp://purl.org/dc/dcmitype/>\r\n" + 
			"PREFIX owl: <http://www.w3.org/2002/07/owl#>\r\n" + 
			"PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n" + 
			"PREFIX tags: <http://www.holygoat.co.uk/owl/redwood/0.1/tags/>\r\n" + 
			"PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/>";
	static String collectionsQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" + 
			"PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/>\r\n" + 
			"\r\n" + 
			"SELECT ?c ?t\r\n" + 
			"WHERE {\r\n" + 
			"?c <rdf:type> <cet:Collection> .\r\n" + 
			"?c <dc:title> ?t .\r\n" + 
			"}";
	
	static Map<String,String> exampleQueries = new HashMap<String,String>();
	static {
		exampleQueries.put("prefixes",prefixes);
		exampleQueries.put("tag",tagQuery);
		exampleQueries.put("single dataset",singleDataset);
		exampleQueries.put("dataset list",datasetListQuery);
		exampleQueries.put("derivation", derivationQuery);
		exampleQueries.put("collections",collectionsQuery);
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
		queryBox.setText(prefixes);
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
