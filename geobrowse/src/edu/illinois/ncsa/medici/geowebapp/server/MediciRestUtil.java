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
	 * get all layers
	 * 
	 * @param mp
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public static List<LayerInfo> getLayers(MediciProxy mp)
			throws JSONException, IOException {
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
	public static List<LayerInfo> getLayersByTag(String tag, MediciProxy mp)
			throws JSONException, IOException {
		String layers = null;
		if ((tag == null) || tag.equals("")) {
			layers = mp.executeAuthenticatedGet("/resteasy/datasets/layers",
					null);
		} else {
			layers = mp.executeAuthenticatedGet("/resteasy/tags/" + tag
					+ "/layers", null);
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
	 * Get all datasets that are features (have a geopoint)
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
		return getLocationsByTag(null, mp);
	}

	/**
	 * Parse the query result
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
		
		if(locations==null) return null;
		
		List<LocationInfo> locationInfoList = new ArrayList<LocationInfo>();
		try {

			JSONObject locationsObj = new JSONObject(locations);

			String[] names = JSONObject.getNames(locationsObj);
			for (String datasetId : names) {
				if (!datasetId.equals("@context")) {
					JSONObject location = locationsObj.getJSONObject(datasetId);
					LocationInfo li = new LocationInfo();

					li.setUri(location.getString("Identifier"));
					li.setTitle(location.getString("Title"));
					JSONObject point = location.getJSONObject("GeoPoint");
					li.setLat(point.getDouble("lat"));
					li.setLon(point.getDouble("long"));
					locationInfoList.add(li);
				}

			}
		} catch (JSONException e) {
			log.warn("parseLocationInfo - JSON error", e);
			return null;
		}

		return locationInfoList;
	}

	/**
	 * Get all datasets that are features (have a geopoint) filtered by tag
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
		String locations = null;
		if ((tag == null) || tag.equals("")) {
			locations = mp.executeAuthenticatedGet(
					"/resteasy/datasets/features", null); 
		} else {
			locations = mp.executeAuthenticatedGet(
				"/resteasy/tags/" + tag + "/features", null);
		} 
		log.debug("locations in json: " + locations);
		return parseLocationInfo(locations);
	}
}
