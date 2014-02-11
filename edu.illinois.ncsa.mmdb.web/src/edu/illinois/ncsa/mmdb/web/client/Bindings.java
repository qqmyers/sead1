package edu.illinois.ncsa.mmdb.web.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author Ram
 * 
 */

public class Bindings extends JavaScriptObject {

    protected Bindings() {

    }

    public final native URL getURL() /*-{
		return this.URL;
    }-*/;

    public final native Label getLabel() /*-{
		return this.Label;
    }-*/;
}
