package edu.illinois.ncsa.mmdb.web.server;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;

/**
 * Client to interact with orchid.org API. Currently focusing on social sign on.
 * 
 * @author Luigi Marini
 * 
 */
public class OrcidClient {

    public static final String SANDBOX_LOGIN_URL = "https://sandbox.orcid.org";
    public static final String SANDBOX_API_URL   = "https://api.sandbox.orcid.org";
    //    private static final String BASE_URL      = "http://orcid.org";
    public static final String AUTH_ENDPOINT     = "/oauth/authorize";
    public static final String CLIENT_ID         = "0000-0002-8511-0211";
    //    private static final String CLIENT_ID         = TupeloStore.getInstance().getConfiguration(ConfigurationKey.OrcidClientId);
    public static final String REDIRECT_URI      = "https://developers.google.com/oauthplayground";
    private static Log         log               = LogFactory.getLog(OrcidClient.class);

    public static String createAuthenticationURL(String clientId) throws MalformedURLException {
        final StringBuilder sb = new StringBuilder();
        final List<NameValuePair> queryParams = new ArrayList<NameValuePair>();
        queryParams.add(new BasicNameValuePair("client_id", clientId));
        queryParams.add(new BasicNameValuePair("response_type", "code"));
        queryParams.add(new BasicNameValuePair("scope", "/authenticate"));
        queryParams.add(new BasicNameValuePair("redirect_uri", REDIRECT_URI));
        sb.append(SANDBOX_LOGIN_URL + AUTH_ENDPOINT);
        sb.append("?");
        sb.append(URLEncodedUtils.format(queryParams, "UTF-8"));
        return sb.toString();
    }

    public static void oAuth() {
        log.debug("Connecting to Orcid API");
        // factory for all requests
        ClientRequestFactory crf = new ClientRequestFactory(UriBuilder.fromUri(SANDBOX_LOGIN_URL).build());
        // test connection
        //        ClientRequest test = crf.createRelativeRequest("/oauth");
        //        test.followRedirects(true);
        //        try {
        //            ClientResponse<String> clientResponse = test.get(String.class);
        //            log.debug("Test connection: " + clientResponse.getStatus() + " " + clientResponse.getEntity());
        //            log.debug("Location: " + clientResponse.getLocation());
        //            MultivaluedMap<String, String> headers = clientResponse.getHeaders();
        //            for (String key : headers.keySet() ) {
        //                log.debug(key + ": " + headers.get(key));
        //            }
        //        } catch (Exception e) {
        //            log.error("Failed to connect to base uri", e);
        //        }
        // authorize token
        ClientRequest authorize = crf.createRelativeRequest(AUTH_ENDPOINT);
        authorize.followRedirects(true);
        authorize.accept(MediaType.APPLICATION_JSON_TYPE);
        authorize.queryParameter("client_id", CLIENT_ID);
        authorize.queryParameter("response_type", "code");
        authorize.queryParameter("scope", "/authenticate");
        authorize.queryParameter("redirect_uri", REDIRECT_URI);

        try {
            ClientResponse<String> clientResponse = authorize.get(String.class);
            log.debug("Authorization status: " + clientResponse.getStatus());
            log.debug("Authorization body: " + clientResponse.getEntity());
            log.debug("Location: " + clientResponse.getLocation());
            MultivaluedMap<String, String> headers = clientResponse.getHeaders();
            for (String key : headers.keySet() ) {
                log.debug(key + ": " + headers.get(key));
            }

        } catch (Exception e) {
            log.error("Failed to authorize user", e);
        }
        log.debug("Done authorizing against Orcid API");
    }

    public static void main(String[] args) {
        oAuth();
    }
}
