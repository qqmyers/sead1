package edu.illinois.ncsa.mmdb.web.server;

import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.resteasy.client.ClientRequest;
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
 * @author myersjd@umich.edu
 *
 */
public class OrcidClient {

    private static String ORCID_TOKEN_REQUEST_URL   = "https://pub.orcid.org/oauth/token";
    private static String ORCID_PUBLIC_API_BASE_URL = "http://pub.orcid.org/v1.2/";

    private static Log    log                       = LogFactory.getLog(OrcidClient.class);

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
        ClientRequest authorize = new ClientRequest(ORCID_TOKEN_REQUEST_URL);
        authorize.followRedirects(true);
        authorize.accept(MediaType.APPLICATION_JSON_TYPE);

        StringBuilder sb = new StringBuilder();
        sb.append("client_id=" + clientId + "&");
        sb.append("client_secret=" + clientSecret + "&");
        sb.append("grant_type=" + "client_credentials&");
        if (redirectURI != null) {
            sb.append("redirect_uri=" + redirectURI + "&");
        }
        //sb.append("code=" + code);

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
            URL url = new URL(ORCID_PUBLIC_API_BASE_URL + id + "/orcid-bio");
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
}
