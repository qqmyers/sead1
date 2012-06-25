package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

public class PropertiesReader {
    static RequestCallback   requestCallback;
    static RequestBuilder    builder;
    private static final int STATUS_CODE_OK = 200;
    static String            vivoURL        = "";

    public static String getVIVOURL() {
        return vivoURL;
    }

    public static void initializePropertiesFile() {
        String url_host = GWT.getModuleBaseURL() + "public.properties";

        requestCallback = new RequestCallback() {
            public void onError(Request request, Throwable exception) {
                exception.printStackTrace();
            }

            public void onResponseReceived(Request request, Response response) {
                String responseText = response.getText();
                vivoURL = responseText.substring(responseText.indexOf("=") + 1);
            }
        };

        builder = new RequestBuilder(RequestBuilder.GET, url_host);
        try {
            builder.sendRequest(null, requestCallback);
        } catch (RequestException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
