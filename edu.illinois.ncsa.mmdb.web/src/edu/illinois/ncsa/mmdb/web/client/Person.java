package edu.illinois.ncsa.mmdb.web.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author Ram
 * 
 */

public class Person extends JavaScriptObject {

    protected Person() {

    }

    public final native String getType() /*-{
		return this.type;
    }-*/;

    public final native String getValue() /*-{
		return this.value;
    }-*/;

}

class FirstName extends Person {

    protected FirstName() {
    }

    public final native String getDataType() /*-{
		return this.datatype;
    }-*/;
}

class LastName extends Person {

    protected LastName() {
    }

    public final native String getDataType() /*-{
		return this.datatype;
    }-*/;
}
