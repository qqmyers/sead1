package org.sead.acr.dashboard;

import java.io.IOException;
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

import org.sead.acr.common.MediciProxy;
import org.sead.acr.common.SparqlQueryServlet;
import org.sead.acr.common.utilities.MimeMap;
import org.sead.acr.common.utilities.PropertiesLoader;
import org.sead.acr.common.utilities.Queries;
import org.sead.acr.common.utilities.json.JSONArray;
import org.sead.acr.common.utilities.json.JSONException;
import org.sead.acr.common.utilities.json.JSONObject;

public class Dashboard extends SparqlQueryServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2553450304238410849L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void handleQuery(HttpServletRequest request,
			HttpServletResponse response) throws HTTPException, Exception {

		MediciProxy mp = getProxy();
		String collections = mp.getSparqlJSONResponse("query="
				+ Queries.ALL_TOPLEVEL_COLLECTIONS);
		String recentUploads = mp.getSparqlJSONResponse("query="
				+ Queries.RECENT_UPLOADS);
		String creators = mp.getSparqlJSONResponse("query="
				+ Queries.TEAM_MEMBERS);
		String projectInfo = mp.getSparqlJSONResponse("query="
				+ Queries.PROJECT_INFO);
		String datasets = mp.getSparqlJSONResponse("query="
				+ Queries.ALL_DATASETS);
		String layers = mp.getSparqlJSONResponse("query="
				+ Queries.ALL_WMS_LAYERS_INFO);

		// parse dataset distribution and form a json
		String datasetDistribution = parseDatasetDistribution(datasets);

		// parse layers info and form a json
		String layersInfo = parseLayerInfo(layers);

		/*
		 * String collections = ""; String recentUploads = ""; String creators =
		 * "";
		 */
		String projectPath = PropertiesLoader.getProperties().getProperty(
				"domain");
		
		String geobrowserUrl = PropertiesLoader.getProperties().getProperty(
				"geobrowserUrl"); 
		if((geobrowserUrl==null)||(geobrowserUrl.length()==0)) {
			geobrowserUrl= projectPath.substring(0, projectPath.lastIndexOf('/')) + "/geobrowse";
		}
		
		// build the geoproxy url
		String geoProxyUrl = projectPath+"/geoproxy/wms";
		
		request.setAttribute("projectPath", projectPath);
		request.setAttribute("geobrowserUrl", geobrowserUrl);
		request.setAttribute("collections", collections);
		request.setAttribute("recentUploads", recentUploads);
		request.setAttribute("creators", creators);
		request.setAttribute("projectInfo", projectInfo);
		request.setAttribute("datasetDistribution", datasetDistribution);

		// adding info about layers
		request.setAttribute("layersInfo", layersInfo);

		// adding geoproxy url
		request.setAttribute("geoProxyUrl", geoProxyUrl);
		
		log.debug("Is Anon:  " + super.isAnonymous());
		request.setAttribute("isAnonymous", super.isAnonymous());

		setRedirectResource("/jsp/dashboard.jsp");

		response.addHeader("cache-control",
				"no-store, no-cache, must-revalidate, max-age=-1"); // don't
																	// cache
		response.addHeader("cache-control", "post-check=0, pre-check=0, false"); // really
																					// don't
																					// cache
		response.addHeader("pragma", "no-cache, no-store"); // no, we mean it,
															// really don't
															// cache
		response.addHeader("expires", "-1"); // if you cache, we're going to be
												// very, very angry
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
			for (int i = 0; i < resultArray.length(); i++) {
				try {
					String filename = resultArray.getJSONObject(i)
							.getJSONObject("binding").getString("literal");
					String nameParts[];
					nameParts = filename.split("\\.");
					if (nameParts.length == 2) {
						String fileExt = nameParts[1];
						String mimeType = MimeMap.findCategory(fileExt);
						if (map.get(mimeType) != null) {
							map.put(mimeType, map.get(mimeType) + 1);
						} else {
							map.put(mimeType, 1);
						}
					}
					// Add filenames with no extension as unknown mime
					// types? Use mimetype info in Medici rather than
					// re-deriving from file extension?
				} catch (org.sead.acr.common.utilities.json.JSONException je) {
					// Why catch and ignore?
				}
			}
		} catch (org.sead.acr.common.utilities.json.JSONException je) {
			//TODO: need to handle the JSONException
			// There are zero or one entries - for now we'll just leave the
			// graph blank
		}
		String datasetDistribution = map.toString();
		return datasetDistribution;
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
			for (int i = 0; i < resultArray.length(); i++) {
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
					for (int j = 0; j < jsonArray.length(); j++) {
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
							.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil"))
						continue;

					newJson.put("uri", uri);
					newJson.put("layerName", layerName);
					newJson.put("extents", extents);

					layersArray.put(newJson);

				} catch (org.sead.acr.common.utilities.json.JSONException je) {
					// Why catch and ignore?
				}
			}
		} catch (org.sead.acr.common.utilities.json.JSONException je) {
			//TODO: need to handle the JSONException
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

	// Not called
	protected String getQuery(String tagID) {
		return null;
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
		for (String pair : pairs) {
			int idx = pair.indexOf("=");
			query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
					URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
		}
		return query_pairs;
	}
}
