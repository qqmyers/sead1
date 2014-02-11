package edu.illinois.ncsa.mmdb.web.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author Ram
 * 
 */

public class Label extends JavaScriptObject {

    protected Label() {

    }

    public final native String getType() /*-{
		return this.type;
    }-*/;

    public final native String getValue() /*-{
		return this.value;
    }-*/;

}