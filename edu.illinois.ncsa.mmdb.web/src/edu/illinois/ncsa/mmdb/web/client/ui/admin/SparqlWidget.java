/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010, NCSA.  All rights reserved.
 *
 * Developed by:
 * Cyberenvironments and Technologies (CET)
 * http://cet.ncsa.illinois.edu/
 *
 * National Center for Supercomputing Applications (NCSA)
 * http://www.ncsa.illinois.edu/
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the names of CET, University of Illinois/NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *******************************************************************************/
package edu.illinois.ncsa.mmdb.web.client.ui.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.dispatch.RunSparqlQuery;
import edu.illinois.ncsa.mmdb.web.client.dispatch.RunSparqlQueryResult;
import edu.illinois.ncsa.mmdb.web.client.ui.LabeledListBox;

public class SparqlWidget extends Composite {
    VerticalPanel              inputPanel;
    FlexTable                  resultsTable;
    TextArea                   queryBox;
    private DispatchAsync      dispatch;

    static String              tagQuery         = "PREFIX dc: <http://purl.org/dc/elements/1.1/>\r\n" +
                                                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" +
                                                        "PREFIX tags: <http://www.holygoat.co.uk/owl/redwood/0.1/tags/>\r\n" +
                                                        "PREFIX dcterms: <http://purl.org/dc/terms/>\r\n" +
                                                        "SELECT ?dataset ?o ?_ldMimeType ?_ldTitle ?_ued\r\n" +
                                                        "WHERE { ?dataset <tags:tag> ?event .\r\n" +
                                                        " ?event <tags:associatedTag> ?tag .\r\n" +
                                                        " ?tag <tags:name> 'art' .\r\n" +
                                                        " ?dataset <rdf:type> <cet:Dataset> .\r\n" +
                                                        " ?dataset <dc:date> ?o .\r\n" +
                                                        " ?dataset <dc:format> ?_ldMimeType .\r\n" +
                                                        " ?dataset <dc:title> ?_ldTitle .\r\n" +
                                                        " OPTIONAL { ?dataset <dcterms:isReplacedBy> ?_ued . }\r\n" +
                                                        "} order by ?_ued ?o limit 100";
    static String              datasetListQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" +
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
    static String              singleDataset    = "SELECT ?p ?o\r\n" +
                                                        "WHERE {\r\n" +
                                                        "<{DATASET URI}> ?p ?o .\r\n" +
                                                        "}";
    static String              derivationQuery  = "PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/> \r\n" +
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
    static String              prefixes         = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" +
                                                        "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\r\n" +
                                                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" +
                                                        "PREFIX dc: <http://purl.org/dc/elements/1.1/>\r\n" +
                                                        "PREFIX dcterms: <ttp://purl.org/dc/terms/>\r\n" +
                                                        "PREFIX dctypes: <ttp://purl.org/dc/dcmitype/>\r\n" +
                                                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>\r\n" +
                                                        "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n" +
                                                        "PREFIX tags: <http://www.holygoat.co.uk/owl/redwood/0.1/tags/>\r\n" +
                                                        "PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/>";
    static String              collectionsQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" +
                                                        "PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/>\r\n" +
                                                        "\r\n" +
                                                        "SELECT ?c ?t\r\n" +
                                                        "WHERE {\r\n" +
                                                        "?c <rdf:type> <cet:Collection> .\r\n" +
                                                        "?c <dc:title> ?t .\r\n" +
                                                        "}";

    static Map<String, String> exampleQueries   = new HashMap<String, String>();
    static {
        exampleQueries.put("prefixes", prefixes);
        exampleQueries.put("tag", tagQuery);
        exampleQueries.put("single dataset", singleDataset);
        exampleQueries.put("dataset list", datasetListQuery);
        exampleQueries.put("derivation", derivationQuery);
        exampleQueries.put("collections", collectionsQuery);
    }

    public SparqlWidget(DispatchAsync dispatch) {
        this.dispatch = dispatch;

        inputPanel = new VerticalPanel();
        initWidget(inputPanel);

        LabeledListBox examples = new LabeledListBox("example queries: ");
        for (String option : new TreeSet<String>(exampleQueries.keySet()) ) {
            examples.addItem(option, option);
        }
        inputPanel.add(examples);

        //
        queryBox = new TextArea();
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
                executeQuery();
            }
        });
        inputPanel.add(submit);

        resultsTable = new FlexTable();
        resultsTable.addStyleName("datasetTable");
        inputPanel.add(resultsTable);
    }

    void executeQuery() {
        RunSparqlQuery action = new RunSparqlQuery();
        action.setQuery(queryBox.getText());
        dispatch.execute(action, new AsyncCallback<RunSparqlQueryResult>() {
            public void onFailure(Throwable caught) {
                Window.alert("SPARQL query failed");
            }

            public void onSuccess(RunSparqlQueryResult result) {
                resultsTable.removeAllRows();
                int row = 0;
                for (List<String> resultRow : result.getResult() ) {
                    int col = 0;
                    for (final String rowCell : resultRow ) {
                        Label label = new Label(rowCell != null ? rowCell : "null");
                        label.addClickHandler(new ClickHandler() {
                            public void onClick(ClickEvent event) {
                                String query = singleDataset.replace("{DATASET URI}", rowCell);
                                queryBox.setText(query);
                                executeQuery();
                            }
                        });
                        resultsTable.setWidget(row, col, label);
                        resultsTable.getCellFormatter().addStyleName(row, col, "datasetTable");
                        col++;
                    }
                    row++;
                }
            }
        });
    }
}
