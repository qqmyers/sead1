package edu.illinois.ncsa.mmdb.web.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author Ram
 * 
 */

public class Bindings extends JavaScriptObject {

    protected Bindings() {

    }

    public final native Person getPerson() /*-{
		return this.Person;
    }-*/;

    public final native FirstName getFirstName() /*-{
		return this.FirstName;
    }-*/;

    public final native LastName getLastName() /*-{
		return this.LastName;
    }-*/;

    public final String getFullName() {

        return getFirstName().getValue() + " " + getLastName().getValue();
    }

}
