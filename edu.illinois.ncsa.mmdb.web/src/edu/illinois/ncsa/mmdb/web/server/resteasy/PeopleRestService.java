package edu.illinois.ncsa.mmdb.web.server.resteasy;

/**
 * @author myersjd@umich.edu
 */

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

/**
 * SEAD People Service Proxy Endpoints
 *
 * Kludge: A simple proxy to allow javascript queries from browsers to c3pr
 * given current firewall restrictions
 *
 */
@Path("/people")
public class PeopleRestService {
    /** Commons logging **/
    private static Log log = LogFactory.getLog(PeopleRestService.class);

    /**
     * Get people list
     *
     * @return basic metadata as JSON-LD
     */
    @GET
    @Path("/")
    @Produces("application/json")
    public Response getPeopleAsJSON() {
        try {
            URL url = new URL(TupeloStore.getInstance().getConfiguration(ConfigurationKey.CPURL) + "/people");

            SSLContext sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, null, null);

            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setSSLSocketFactory(sc.getSocketFactory());

            conn.setReadTimeout(10000);
            conn.addRequestProperty("Accept", "application/json");

            int status = conn.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                if (status != HttpURLConnection.HTTP_NOT_FOUND) {
                    log.warn("Received " + status + " from c3pr /people service");
                } else {
                    log.debug("Received " + status + " from c3pr /people service");
                }
                return Response.status(status).build();
            } else {
                log.debug("Returning people json from c3pr");
                return Response.ok(conn.getInputStream()).build();

            }
        } catch (IOException e) {
            log.error("Error retrieving /people from c3pr: ", e);
        } catch (Exception e) {
            log.error("Error retrieving /people from c3pr: ", e);
        }
        return Response.status(404).build();
    }

    /**
     * Get people list
     *
     * @return basic metadata as JSON-LD
     */
    @GET
    @Path("/{id}")
    @Produces("application/json")
    public Response getPersonAsJSON(@PathParam("id") @Encoded String id) {

        try {
            URL url = new URL(TupeloStore.getInstance().getConfiguration(ConfigurationKey.CPURL) + "/people/" + id);
            SSLContext sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, null, null);

            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setSSLSocketFactory(sc.getSocketFactory());
            conn.setReadTimeout(10000);
            conn.addRequestProperty("Accept", "application/json");

            int status = conn.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                if (status != HttpURLConnection.HTTP_NOT_FOUND) {
                    log.warn("Received " + status + " from c3pr /people service");
                } else {
                    log.debug("Received " + status + " from c3pr /people service");
                }
                return Response.status(status).build();
            } else {
                log.debug("Returning JSON for : " + id);
                return Response.ok(conn.getInputStream()).build();

            }
        } catch (IOException e) {
            log.error("Error retrieving /people/" + id + " from c3pr: ", e);
        } catch (Exception e) {
            log.error("Error retrieving /people/" + id + " from c3pr: ", e);
        }
        return Response.status(404).build();
    }

    public static JSONObject getPersonJSON(String id) {
        JSONObject person = getPersonJSONForID(id);
        log.debug("Checking for person: " + id);
        if (person != null) {
            log.debug("Found person: " + person.toString(2));
            return person;
        } else {
            //Never fail! - if the person's identifier is valid but not in pdt, request that it be added

            try {
                URL url2 = new URL(TupeloStore.getInstance().getConfiguration(ConfigurationKey.CPURL) + "/people");
                SSLContext sc = SSLContext.getInstance("TLSv1.2");
                sc.init(null, null, null);
                HttpsURLConnection post = (HttpsURLConnection) url2.openConnection();
                post.setSSLSocketFactory(sc.getSocketFactory());
                post.setReadTimeout(15000);
                post.setConnectTimeout(15000);
                post.setRequestMethod("POST");
                post.addRequestProperty("Accept", "application/json");
                post.addRequestProperty("Content-type", "application/json");
                post.setDoInput(true);
                post.setDoOutput(true);

                OutputStream os = post.getOutputStream();
                OutputStreamWriter writer =
                        new OutputStreamWriter(os, "UTF-8");
                writer.write("{\"identifier\":\"" + URLDecoder.decode(id, "UTF-8") + "\"}");

                writer.flush();
                writer.close();
                os.close();
                int responseCode = post.getResponseCode();
                log.debug("Adding :" + id + ", response is " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_CREATED) {
                    JSONObject newPerson = new JSONObject(org.apache.commons.io.IOUtils.toString(post.getInputStream()));
                    person = getPersonJSONForID(newPerson.getString("identifier"));
                    log.debug("Created person: " + person.toString(2));

                }
            } catch (IOException io) {
                log.warn(io.getLocalizedMessage());
            } catch (Exception e) {
                log.warn(e.getLocalizedMessage());
            }
        }
        return person;

    }

    private static JSONObject getPersonJSONForID(String id) {
        try {
            URL url = new URL(TupeloStore.getInstance().getConfiguration(ConfigurationKey.CPURL) + "/people/" + id);
            SSLContext sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, null, null);

            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setSSLSocketFactory(sc.getSocketFactory());
            conn.setReadTimeout(10000);
            conn.addRequestProperty("Accept", "application/json");

            int status = conn.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {

                return new JSONObject(org.apache.commons.io.IOUtils.toString(conn.getInputStream()));

            }
        } catch (IOException e) {
            log.error("Error retrieving /people/" + id + " from c3pr: ", e);
        } catch (JSONException e) {
            log.warn(e);
        } catch (Exception e) {
            log.warn(e);
        }
        return null;
    }
}
