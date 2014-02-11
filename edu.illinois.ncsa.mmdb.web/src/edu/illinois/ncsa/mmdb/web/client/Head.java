package edu.illinois.ncsa.mmdb.web.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author Ram
 * 
 */

public class Head extends JavaScriptObject {

    protected Head() {

    }

    public final native CustomJsArray<String> getVars() /*-{
		return this.vars;
    }-*/;

}
