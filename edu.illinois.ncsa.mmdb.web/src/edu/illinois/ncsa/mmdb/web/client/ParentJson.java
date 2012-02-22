package edu.illinois.ncsa.mmdb.web.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author Ram
 * 
 */

public class ParentJson extends JavaScriptObject {

    protected ParentJson() {

    }

    /*
     * private Head head = new Head(); private Results results = new Results();
     */

    public final native Head getHead() /*-{
		return this.head;
    }-*/;

    /*
     * void setHead(Head head) { this.head = head; }
     */

    public final native Results getResults() /*-{
		return this.results;
    }-*/;

    /*
     * void setResults(Results results) { this.results = results; }
     */

}
