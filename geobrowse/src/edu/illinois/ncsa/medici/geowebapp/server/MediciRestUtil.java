package edu.illinois.ncsa.medici.geowebapp.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sead.acr.common.MediciProxy;
import org.sead.acr.common.utilities.Queries;

import edu.illinois.ncsa.medici.geowebapp.shared.LayerInfo;
import edu.illinois.ncsa.medici.geowebapp.shared.LocationInfo;

public class MediciRestUtil {

	protected static Log log = LogFactory.getLog(MediciRestUtil.class);

	/**
	 * get all tags
	 * 
	 * @param mp
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public static List<String> getTags(MediciProxy mp) throws IOException,
			JSONException {

		String tagsJson = mp.executeAuthenticatedGet("/resteasy/tags", null);
		log.warn("tags in json: " + tagsJson);

		return parseTags(tagsJson);
	}

	/**
	 * parse tag json and return list of string
	 * 
	 * @param tagsJson
	 * @return
	 * @throws JSONException
	 */
	public static List<String> parseTags(String tagsJson) throws JSONException {
		List<String> tags = new ArrayList<String>();

		JSONArray jsArray = new JSONArray(tagsJson);

		for (int i = 0; i < jsArray.length(); i++) {
			String tag = jsArray.getString(i);
			tags.add(tag);
		}

		return tags;
	}

	/**
	 * Construct the List of uris by given tag
	 * 
	 * @param tag
	 * @param mp
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	private static List<String> getUrisByTag(String tag, MediciProxy mp)
			throws IOException, JSONException {

		List<String> uris = new ArrayList<String>();

		String responseBody = mp.executeAuthenticatedGet("/resteasy/tags/"
				+ tag + "/layers", null);
		log.info("tags " + responseBody);
		/*
		 * if (responseBody.startsWith("[")) { JSONArray jsArray = new
		 * JSONArray(responseBody); for (int i = 0; i < jsArray.length(); i++) {
		 * JSONObject js = jsArray.getJSONObject(i); String uri =
		 * js.getString("uri"); log.debug("UrisByTag: " + uri); uris.add(uri); }
		 * }
		 */
		if (responseBody.startsWith("{")) {
			JSONObject js = new JSONObject(responseBody);
			for (String uri : JSONObject.getNames(js)) {
				log.debug("UrisByTag: " + uri);
				uris.add(uri);
			}
		}

		return uris;
	}

	/**
	 * get all layers
	 * 
	 * @param mp
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public static List<LayerInfo> getLayers(MediciProxy mp) throws JSONException, IOException {
		return getLayersByTag(null, mp);
	}
	
	/**
	 * get the layers filtered by tag
	 * 
	 * @param tag
	 * @param mp
	 * @return
	 * @throws JSONException
	 * @throws IOException
	 */
	public static List<LayerInfo> getLayersByTag(String tag, MediciProxy mp) throws JSONException, IOException
			{
		String layers = null;
		if(tag == null) {
		layers = mp.executeAuthenticatedGet("/resteasy/datasets/layers", null);
		} else {
			layers = mp.executeAuthenticatedGet("/resteasy/tags/" + tag + "/layers", null);
		}
		log.warn("layers in json: " + layers);

		return parseLayerInfo(layers);
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
	private static List<LayerInfo> parseLayerInfo(String layers)
			throws MalformedURLException, UnsupportedEncodingException,
			JSONException {

		List<LayerInfo> layerInfoList = new ArrayList<LayerInfo>();

		// unexpected json error; should throw JSONException to invalidate the
		// session
		JSONObject layersObj = new JSONObject(layers);
		String[] names = JSONObject.getNames(layersObj);
		for (String datasetId : names) {
			if (!datasetId.equals("@context")) {
				JSONObject layerObj = layersObj.getJSONObject(datasetId);
				LayerInfo li = new LayerInfo();

				li.setUri(layerObj.getString("Identifier"));
				li.setName(layerObj.getString("WMSLayerName"));
				li.setTitle(layerObj.getString("Title"));

				String layerUrl = layerObj.getString("WMSLayerUrl");
				String extents = "";
				String srs = "";

				Map<String, String> params = splitQuery(new URL(layerUrl));
				extents = params.get("bbox");
				srs = params.get("srs");
				li.setSrs(srs);
				String[] split = extents.split(",");
				li.setMinx(Double.parseDouble(split[0]));
				li.setMiny(Double.parseDouble(split[1]));
				li.setMaxx(Double.parseDouble(split[2]));
				li.setMaxy(Double.parseDouble(split[3]));

				layerInfoList.add(li);

			}
		}
		return layerInfoList;
	}

	/**
	 * Split the query parameters of URL with key-value pair
	 * 
	 * @param url
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private static Map<String, String> splitQuery(URL url)
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

	/**
	 * Get all locations of datasets via sparql query
	 * 
	 * @param mp
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws org.sead.acr.common.utilities.json.JSONException
	 * @throws JSONException
	 */
	public static List<LocationInfo> getLocations(MediciProxy mp)
			throws MalformedURLException, IOException,
			org.sead.acr.common.utilities.json.JSONException, JSONException {
		String locations = mp.getSparqlJSONResponse("query="
				+ Queries.ALL_DATASET_LOCATION);
		log.warn("locations in json: " + locations);

		return parseLocationInfo(locations);
	}

	/**
	 * Parse the sparql result
	 * 
	 * @param locations
	 * @return
	 * @throws JSONException
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	private static List<LocationInfo> parseLocationInfo(String locations)
			throws MalformedURLException, UnsupportedEncodingException,
			JSONException {

		List<LocationInfo> locationInfoList = new ArrayList<LocationInfo>();

		// unexpected json error; should throw JSONException to invalidate the
		// session
		JSONObject locationObj = new JSONObject(locations);
		JSONObject sparqlJson = locationObj.getJSONObject("sparql");

		// expect json error; should return null instead of invalidating the
		// session
		try {
			Object resultObject = sparqlJson.getJSONObject("results").get(
					"result");
			JSONArray resultArray = null;
			if (resultObject instanceof JSONArray) {
				resultArray = (JSONArray) resultObject;
			} else {
				resultArray = new JSONArray();
				resultArray.put(resultObject);
			}
			for (int i = 0; i < resultArray.length(); i++) {

				// getting wmsURL to parse out extents
				JSONArray jsonArray = resultArray.getJSONObject(i)
						.getJSONArray("binding");

				LocationInfo li = new LocationInfo();

				String uri = "";
				String deleted = "";
				String title = "";
				Double lat = null;
				Double lon = null;
				for (int j = 0; j < jsonArray.length(); j++) {
					JSONObject entry = jsonArray.getJSONObject(j);
					if (entry.getString("name").equals("uri")) {
						uri = entry.getString("uri");
					} else if (entry.getString("name").equals("title")) {
						title = entry.getString("literal");
					} else if (entry.getString("name").equals("lat")) {
						JSONObject jo = entry.getJSONObject("literal");
						lat = jo.getDouble("content");
					} else if (entry.getString("name").equals("lon")) {
						JSONObject jo = entry.getJSONObject("literal");
						lon = jo.getDouble("content");
					} else if (entry.getString("name").equals("deleted")) {
						deleted = entry.getString("uri");
					}
				}

				// if the dataset is deleted, skip
				if (deleted
						.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil"))
					continue;
				li.setTitle(title);
				li.setUri(uri);
				li.setLat(lat);
				li.setLon(lon);

				locationInfoList.add(li);

			}

		} catch (JSONException e) {
			log.warn("parseLocationInfo - JSON error", e);
			return null;
		}

		return locationInfoList;
	}

	/**
	 * Get locations of datasets filtered by tag
	 * 
	 * @param tag
	 * @param mp
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws org.sead.acr.common.utilities.json.JSONException
	 * @throws JSONException
	 */
	public static List<LocationInfo> getLocationsByTag(String tag,
			MediciProxy mp) throws MalformedURLException, IOException,
			org.sead.acr.common.utilities.json.JSONException, JSONException {

		List<LocationInfo> locations = new ArrayList<LocationInfo>();
		List<String> urisByTag = getUrisByTag(tag, mp);

		// if there is no uris with the given tag, don't do anything
		// if there is uris with the given tag, find the corresponding loctions
		// with the uri
		if (!urisByTag.isEmpty()) {

			LinkedHashMap<String, LocationInfo> map = getLocationInfoMap(mp);
			for (String uri : map.keySet()) {
				if (urisByTag.contains(uri)) {
					locations.add(map.get(uri));
				}
			}
		}

		return locations;
	}

	/**
	 * 
	 * Get locations map by uri of dataset
	 * 
	 * @param mp
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws org.sead.acr.common.utilities.json.JSONException
	 * @throws JSONException
	 */
	private static LinkedHashMap<String, LocationInfo> getLocationInfoMap(
			MediciProxy mp) throws MalformedURLException, IOException,
			org.sead.acr.common.utilities.json.JSONException, JSONException {
		LinkedHashMap<String, LocationInfo> map = new LinkedHashMap<String, LocationInfo>();
		List<LocationInfo> allLocations = getLocations(mp);
		for (LocationInfo li : allLocations) {
			map.put(li.getUri(), li);
		}
		return map;
	}

}
