package edu.illinois.ncsa.mmdb.web.client.presenter;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.dispatch.RunSparqlQuery;
import edu.illinois.ncsa.mmdb.web.client.dispatch.RunSparqlQueryResult;
import edu.illinois.ncsa.mmdb.web.client.mvp.Presenter;

public class UserMetadataPresenter implements Presenter {
    static String                   availableFieldsQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" +
                                                                 "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" +
                                                                 "PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/>\r\n" +
                                                                 "\r\n" +
                                                                 "SELECT ?label ?f\r\n" +
                                                                 "WHERE {\r\n" +
                                                                 "  ?f <rdf:type> <cet:userMetadataField> .\r\n" +
                                                                 "  ?f <rdfs:label> ?label .\r\n" +
                                                                 "}\r\n" +
                                                                 "ORDER BY ASC(?label)";

    protected final MyDispatchAsync dispatch;
    protected final Display         display;

    public UserMetadataPresenter(MyDispatchAsync dispatch, Display display) {
        this.dispatch = dispatch;
        this.display = display;
    }

    @Override
    public void bind() {
        dispatch.execute(new RunSparqlQuery(availableFieldsQuery), // FIXME write a dedicated dispatch instead of using SPARQL
                new AsyncCallback<RunSparqlQueryResult>() {
                    public void onFailure(Throwable caught) {
                    }

                    public void onSuccess(RunSparqlQueryResult result) {
                        if (result.getResult().size() > 0) {
                            for (List<String> entry : result.getResult() ) {
                                String label = entry.get(0);
                                String predicate = entry.get(1);
                                display.addMetadataField(predicate, label);
                            }
                        }
                    }
                });
    }

    public interface Display {
        /**
         * Indicate to the display the name and URI of a user metadata predicate
         */
        void addMetadataField(String uri, String name);

        /**
         * Indicate to the display that a given user metadata predicate has a
         * given value (for whatever content the presenter is presenting)
         */
        void addMetadataValue(String uri, String value);
    }
}
