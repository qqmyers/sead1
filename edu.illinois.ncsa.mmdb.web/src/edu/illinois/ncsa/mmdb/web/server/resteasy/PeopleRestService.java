package edu.illinois.ncsa.mmdb.web.server.resteasy;

/**
 * @author myersjd@umich.edu
 */

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

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
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.addRequestProperty("Accept", "application/json");

            int status = conn.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                log.warn("Received " + status + " from c3pr /people service");
                return Response.status(status).build();
            } else {

                return Response.ok(conn.getInputStream()).build();

            }
        } catch (IOException e) {
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
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.addRequestProperty("Accept", "application/json");

            int status = conn.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                log.warn("Received " + status + " from c3pr /people/" + id + " service");
                return Response.status(status).build();
            } else {

                return Response.ok(conn.getInputStream()).build();

            }
        } catch (IOException e) {
            log.error("Error retrieving /people/" + id + " from c3pr: ", e);
        }
        return Response.status(404).build();
    }

    public static JSONObject getPersonJSON(String id) {
        try {
            URL url = new URL(TupeloStore.getInstance().getConfiguration(ConfigurationKey.CPURL) + "/people/" + id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
        }
        return null;

    }
}
