package edu.illinois.ncsa.mmdb.web.server;

import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.illinois.ncsa.mmdb.web.client.dispatch.Oauth2ServerFlowTokenRequestResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.Oauth2ServerFlowUserInfoResult;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;

/**
 * Client to interact with orchid.org API. Currently focusing on social sign on.
 *
 * @author Luigi Marini
 *
 */
public class OrcidClient {

    public static final String  SANDBOX_LOGIN_URL = "https://sandbox.orcid.org";
    public static final String  SANDBOX_API_URL   = "https://api.sandbox.orcid.org";
    private static final String BASE_URL          = "http://orcid.org";
    public static final String  AUTH_ENDPOINT     = "/oauth/authorize";
    private static final String CLIENT_ID         = TupeloStore.getInstance().getConfiguration(ConfigurationKey.OrcidClientId);
    public static final String  REDIRECT_URI      = "https://developers.google.com/oauthplayground";
    private static Log          log               = LogFactory.getLog(OrcidClient.class);

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

    public static String authenticationURL(String clientId, String redirectURI) {
        String orcidAuthorizeURL = "https://orcid.org/oauth/authorize";
        StringBuilder sb = new StringBuilder();
        sb.append("client_id=" + clientId + "&");
        sb.append("scope=" + "/authenticate" + "&");
        sb.append("response_type=" + "code&");
        sb.append("redirect_uri=" + redirectURI + "&");
        sb.append("state=" + "magic-bean");
        return orcidAuthorizeURL + "?" + sb.toString();
    }

    /* Get an authentication token using the code and retrieve the username/email via the API
     * (this requires two calls with Orcid vs. one with Google)
     */
    public static Oauth2ServerFlowTokenRequestResult requestAccessToken(String code) {
        return requestAccessToken(code, null);
    }

    public static Oauth2ServerFlowTokenRequestResult requestAccessToken(String code, String redirectURI) {
        Oauth2ServerFlowTokenRequestResult result = new Oauth2ServerFlowTokenRequestResult();

        String clientId = TupeloStore.getInstance().getConfiguration(ConfigurationKey.OrcidClientId);
        String clientSecret = TupeloStore.getInstance().getConfiguration(ConfigurationKey.OrcidClientSecret);
        ClientRequest authorize = new ClientRequest("https://api.sandbox.orcid.org/oauth/token");
        authorize.followRedirects(true);
        authorize.accept(MediaType.APPLICATION_JSON_TYPE);

        StringBuilder sb = new StringBuilder();
        sb.append("client_id=" + clientId + "&");
        sb.append("client_secret=" + clientSecret + "&");
        sb.append("grant_type=" + "authorization_code&");
        if (redirectURI != null) {
            sb.append("redirect_uri=" + redirectURI + "&");
        }
        sb.append("code=" + code);

        authorize.body(MediaType.APPLICATION_FORM_URLENCODED_TYPE, sb.toString());

        try {
            log.debug("Requesting access token " + authorize.getUri());
        } catch (Exception e1) {
            log.error("Error requesting access token", e1);
        }

        try {
            ClientResponse<String> clientResponse = authorize.post(String.class);
            log.debug("Authorization status: " + clientResponse.getStatus());
            log.debug("Authorization body: " + clientResponse.getEntity());
            log.debug("Location: " + clientResponse.getLocation());
            MultivaluedMap<String, String> headers = clientResponse.getHeaders();
            for (String key : headers.keySet() ) {
                log.debug(key + ": " + headers.get(key));
            }
            if (clientResponse.getStatus() == 200) {
                String tokenString = clientResponse.getEntity();
                org.json.JSONObject tokenObject = new org.json.JSONObject(tokenString);
                result.setAuthToken(tokenObject.getString("access_token"));
                result.setExpirationTime(tokenObject.getInt("expires_in") + (int) (System.currentTimeMillis() / 1000L));
                result.setId(tokenObject.getString("orcid"));
            }
        } catch (Exception e) {
            log.error("Failed to authorize user", e);
        }
        log.debug("Retrieved token for id: " + result.getId());
        return result;
    }

    public static Oauth2ServerFlowUserInfoResult requestUserInfo(String id, String token) {
        Oauth2ServerFlowUserInfoResult result = new Oauth2ServerFlowUserInfoResult();

        try {
            URL url = new URL("http://pub.sandbox.orcid.org/v1.1/" + id + "/orcid-bio");
            URLConnection urlCon = url.openConnection();
            urlCon.setRequestProperty("Accept", "application/xml");
            urlCon.setRequestProperty("Authorization", "Bearer " + token);
            urlCon.connect();
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(urlCon.getInputStream());
            try {
                DOMSource domSource = new DOMSource(doc);
                StringWriter writer = new StringWriter();
                StreamResult sresult = new StreamResult(writer);
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer transformer = tf.newTransformer();
                transformer.transform(domSource, sresult);
                log.debug("XML IN String format is: \n" + writer.toString());
            } catch (Exception e) {
                log.debug(e);
            }
            Element root = doc.getDocumentElement();
            NodeList firstName = root.getElementsByTagName("given-names");
            NodeList lastName = root.getElementsByTagName("family-name");
            result.setUserName(firstName.item(0).getTextContent() + " " + lastName.item(0).getTextContent());
            log.debug("Found name: " + result.getUserName());
            NodeList nl = root.getElementsByTagName("email");
            for (int i = 0; i < nl.getLength(); i++ ) {
                Node node = nl.item(i);
                org.w3c.dom.NamedNodeMap map = node.getAttributes();
                if ("true".equals(map.getNamedItem("primary").getNodeValue())) {
                    result.setEmail(node.getTextContent());
                    log.debug("Found email: " + result.getEmail());
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Failed to authorize user", e);
        }
        log.debug("Done authorizing against Orcid API");
        return result;

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

    class AccessTokenParams {
        private String client_id;
        private String client_secret;
        private String grant_type;
        private String redirect_uri;
        private String code;
    }
}
