package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;

public class PropertiesReader {
    static RequestCallback requestCallback;
    static RequestBuilder  builder;
    static String          vivoURL      = "";
    private static String  discoveryURL = "";

    public static String getVIVOURL() {
        if (vivoURL == "") {
            initializePropertiesFile();
        }
        return vivoURL;
        //return vivoURL;
    }

    public static String getDiscoveryURL() {
        if (discoveryURL == "") {
            initializePropertiesFile();
        }
        return discoveryURL;
        //return vivoURL;
    }

    //FIXME - read this as a real properties file rather than parsing the strings!
    public static void initializePropertiesFile() {
        String url_host = GWT.getModuleBaseURL() + "public.properties";

        requestCallback = new RequestCallback() {
            public void onError(Request request, Throwable exception) {
                exception.printStackTrace();
            }

            public void onResponseReceived(Request request, Response response) {
                try {
                    String responseText = response.getText();
                    if (vivoURL == "") {
                        vivoURL = responseText.substring(responseText.indexOf("vivourl=") + "vivourl".length() + 1, responseText.indexOf('\n'));
                    }
                    if (discoveryURL == "") {
                        discoveryURL = responseText.substring(responseText.indexOf("discoveryurl=") + "discoveryurl".length() + 1);
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };

        builder = new RequestBuilder(RequestBuilder.GET, url_host);
        try {
            builder.sendRequest(null, requestCallback);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
