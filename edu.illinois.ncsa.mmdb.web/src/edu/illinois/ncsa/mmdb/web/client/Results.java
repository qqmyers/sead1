package edu.illinois.ncsa.mmdb.web.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author Ram
 * 
 */

public class Results extends JavaScriptObject {

    protected Results() {

    }

    public final native CustomJsArray<Bindings> getBindings() /*-{
		return this.bindings;
    }-*/;

}
