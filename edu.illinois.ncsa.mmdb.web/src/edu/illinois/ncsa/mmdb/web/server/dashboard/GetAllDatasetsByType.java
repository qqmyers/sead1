package edu.illinois.ncsa.mmdb.web.server.dashboard;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sead.acr.common.SparqlQueryServlet;
import org.sead.acr.common.utilities.MimeMap;
import org.sead.acr.common.utilities.Queries;

public class GetAllDatasetsByType extends SparqlQueryServlet {

    /**
	 *
	 */
    private static final long serialVersionUID = -8642016544488942571L;

    protected String getQuery(String tagID) {
        return Queries.ALL_DATASETS_BY_TYPE;
    }

    protected void handleResult(String responseJson,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Return results if no exceptions occurred
        PrintWriter pw = response.getWriter();
        response.setContentType("application/json");
        try {
            pw.write(parseDatasetDistribution(responseJson));
        } catch (JSONException e) {
            throw (new IOException(e.getMessage()));
        }
        pw.flush();
        pw.close();
    }

    /**
     * parse the json result of sparql query for dataset distribution
     *
     * @param datasets
     * @return
     * @throws JSONException
     */
    private String parseDatasetDistribution(String datasets)
            throws JSONException {
        JSONObject obj = new JSONObject(datasets);
        Map<String, Integer> map = new HashMap<String, Integer>();

        try {
            JSONArray resultArray = obj.getJSONObject("sparql")
                    .getJSONObject("results").getJSONArray("result");

            for (int i = 0; i < resultArray.length(); i++ ) {
                try {
                    // Should be an array - if not, no title and we can count as
                    // other)
                    JSONArray itemArray = resultArray.getJSONObject(i)
                            .getJSONArray("binding");
                    String mimetype = null;
                    String category = "Other";
                    boolean deleted = false;
                    for (int j = 0; j < itemArray.length(); j++ ) {
                        String key = itemArray.getJSONObject(j).getString(
                                "name");
                        if (key.equals("mime")) {
                            mimetype = itemArray.getJSONObject(j).getString(
                                    "literal");
                        } else if (key.equals("deleted")) {
                            deleted = true;
                        }

                    }
                    if (!deleted) {

                        if (mimetype != null) {
                            category = MimeMap.findCategoryFromType(mimetype);
                        }

                        if (map.get(category) != null) {
                            map.put(category, map.get(category) + 1);
                        } else {
                            map.put(category, 1);
                        }
                    }
                    // Use mimetype info in Medici rather than
                    // re-deriving from file extension?
                } catch (JSONException je) {
                    log.warn("i= " + i + " : " + je.getMessage());
                }
            }
        } catch (JSONException je) {
            // TODO: need to handle the JSONException
            // There are zero or one entries - for now we'll just leave the
            // graph blank
        }
        JSONArray topArray = new JSONArray();
        for (String key : map.keySet() ) {
            JSONArray itemArray = new JSONArray();
            itemArray.put(key);
            itemArray.put(map.get(key).intValue());
            topArray.put(itemArray);
        }
        return topArray.toString();
    }

}
