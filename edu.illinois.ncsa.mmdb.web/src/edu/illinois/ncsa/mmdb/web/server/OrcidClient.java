package edu.illinois.ncsa.mmdb.web.server;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    private static final String CLIENT_ID     = "0000-0002-8511-0211";
    private static final Object REDIRECT_URI  = "https://developers.google.com/oauthplayground";
    private static Log          log           = LogFactory.getLog(OrcidClient.class);

    public static void oAuth() {
        log.debug("Connecting to Orcid API");
        // factory for all requests
        ClientRequestFactory crf = new ClientRequestFactory(UriBuilder.fromUri(SANDBOX_URL).build());
        // authorize token
        ClientRequest authorize = crf.createRelativeRequest(AUTH_ENDPOINT);
        authorize.queryParameter("client_id", CLIENT_ID);
        authorize.queryParameter("response_type", "code");
        authorize.queryParameter("scope", "/authenticate");
        authorize.queryParameter("redirect_uri", REDIRECT_URI);
        try {
            log.debug("Authorization: " + authorize.get(String.class).getEntity());
        } catch (Exception e) {
            log.error("Failed to authorize user", e);
        }
        log.debug("Done authorizing against Orcid API");
    }

    public static void main(String[] args) {
        oAuth();
    }
}
