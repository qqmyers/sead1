package edu.illinois.ncsa.mmdb.web.server.dashboard;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sead.acr.common.SparqlQueryServlet;
import org.sead.acr.common.utilities.Queries;

public class GetWMSLayers extends SparqlQueryServlet {

    /**
	 *
	 */
    private static final long serialVersionUID = -8642016544488942571L;

    protected String getQuery(String tagID) {
        return Queries.ALL_WMS_LAYERS_INFO;

    }

    protected void handleResult(String responseJson,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Return results if no exceptions occurred
        PrintWriter pw = response.getWriter();
        response.setContentType("application/json");
        try {
            pw.write(parseLayerInfo(responseJson));
        } catch (JSONException e) {
            throw (new IOException(e.getMessage()));
        }
        pw.flush();
        pw.close();
    }

    /**
     *
     * parse the json result of sparql query for wms layers
     *
     * @param layers
     * @return
     * @throws JSONException
     * @throws MalformedURLException
     * @throws UnsupportedEncodingException
     */
    private String parseLayerInfo(String layers) throws JSONException,
            MalformedURLException, UnsupportedEncodingException {

        JSONObject layerObj = new JSONObject(layers);

        // new json arry to store the extracted info
        JSONArray layersArray = new JSONArray();

        try {
            JSONArray resultArray = layerObj.getJSONObject("sparql")
                    .getJSONObject("results").getJSONArray("result");
            for (int i = 0; i < resultArray.length(); i++ ) {
                try {
                    // getting wmsURL to parse out extents
                    JSONArray jsonArray = resultArray.getJSONObject(i)
                            .getJSONArray("binding");

                    Map<String, String> newJson = new HashMap<String, String>();
                    String uri = "";
                    String layerName = "";
                    String layerUrl = "";
                    String extents = "";
                    String deleted = "";
                    for (int j = 0; j < jsonArray.length(); j++ ) {
                        JSONObject entry = jsonArray.getJSONObject(j);
                        if (entry.getString("name").equals("uri")) {
                            uri = entry.getString("uri");
                        } else if (entry.getString("name").equals("layername")) {
                            layerName = entry.getString("literal");
                        } else if (entry.getString("name").equals("layerurl")) {
                            layerUrl = entry.getString("literal");
                            Map<String, String> params = splitQuery(new URL(
                                    layerUrl));
                            extents = params.get("bbox");
                        } else if (entry.getString("name").equals("deleted")) {
                            deleted = entry.getString("uri");
                        }
                    }

                    // if the dataset is deleted, skip
                    if (deleted
                            .equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil")) {
                        continue;
                    }

                    newJson.put("uri", uri);
                    newJson.put("layerName", layerName);
                    newJson.put("extents", extents);

                    layersArray.put(newJson);

                } catch (JSONException je) {
                    // Why catch and ignore?
                }
            }
        } catch (JSONException je) {
            // TODO: need to handle the JSONException
            // There are zero or one entries - for now we'll just leave the
            // graph blank
        }
        return layersArray.toString();
    }

    protected void handleHTTPException(HTTPException he,
            HttpServletResponse response) throws IOException {
        super.handleHTTPException(he, response);
        setRedirectResource("/login");
    }

    /**
     * Split the query parameters of URL with key-value pair
     *
     * @param url
     * @return
     * @throws UnsupportedEncodingException
     */
    public static Map<String, String> splitQuery(URL url)
            throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        String query = url.getQuery();
        String[] pairs = query.split("&");
        for (String pair : pairs ) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
                    URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }

}
