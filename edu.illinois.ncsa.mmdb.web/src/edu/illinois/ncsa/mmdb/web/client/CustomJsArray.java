package edu.illinois.ncsa.mmdb.web.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author Ram
 * 
 */

public class CustomJsArray<E> extends JavaScriptObject {
    protected CustomJsArray() {
    }

    public final native int length() /*-{
		return this.length;
    }-*/;

    public final native E get(int i) /*-{
		return this[i];
    }-*/;
}
