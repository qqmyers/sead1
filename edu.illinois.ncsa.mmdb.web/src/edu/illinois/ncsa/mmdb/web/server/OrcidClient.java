package edu.illinois.ncsa.mmdb.web.server;

import javax.ws.rs.core.UriBuilder;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientRequestFactory;

/**
 * Client to interact with orchid.org API. Currently focusing on social sign on.
 * 
 * @author Luigi Marini
 * 
 */
public class OrcidClient {

    private static final String SANDBOX_URL   = "https://sandbox.orcid.org/oauth";
    private static final String AUTH_ENDPOINT = "/authorize";

    public static void oAuth() {
        // factory for all requests
        ClientRequestFactory crf = new ClientRequestFactory(UriBuilder.fromUri(SANDBOX_URL).build());
        // authorize token
        ClientRequest authorize = crf.createRelativeRequest(AUTH_ENDPOINT);
        authorize.queryParameter("", "");
        try {
            System.out.println(authorize.get(String.class).getEntity());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        oAuth();
    }
}
