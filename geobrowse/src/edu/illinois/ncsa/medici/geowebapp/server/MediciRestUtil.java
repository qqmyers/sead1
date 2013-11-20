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

public class MediciRestUtil {

	protected static Log log = LogFactory.getLog(MediciRestUtil.class);

	public static List<String> getTags(MediciProxy mp) throws IOException,
			JSONException {
		List<String> tags = new ArrayList<String>();

		String responseBody = mp
				.executeAuthenticatedGet("/resteasy/tags", null);

		JSONArray jsArray = new JSONArray(responseBody);
		for (int i = 0; i < jsArray.length(); i++) {
			String tag = jsArray.getString(i);
			tags.add(tag);
		}
		return tags;
	}

	public static List<String> getUrisByTag(String tag, MediciProxy mp)
			throws IOException, JSONException {

		List<String> uris = new ArrayList<String>();

		String responseBody = mp.executeAuthenticatedGet("/resteasy/tags/"
				+ tag + "/datasets", null);

		JSONArray jsArray = new JSONArray(responseBody);
		for (int i = 0; i < jsArray.length(); i++) {
			JSONObject js = jsArray.getJSONObject(i);
			String uri = js.getString("uri");
			log.debug("UrisByTag: " + uri);
			uris.add(uri);
		}

		return uris;
	}

	public static List<LayerInfo> getLayers(MediciProxy mp)
			throws MalformedURLException, IOException,
			org.sead.acr.common.utilities.json.JSONException, JSONException {
		String layers = mp.getSparqlJSONResponse("query="
				+ Queries.ALL_WMS_LAYERS_INFO);
		return parseLayerInfo(layers);
	}

	public static LinkedHashMap<String, LayerInfo> getLayerInfoMap(MediciProxy mp) throws MalformedURLException, IOException, org.sead.acr.common.utilities.json.JSONException, JSONException {
		LinkedHashMap<String, LayerInfo> map = new LinkedHashMap<String, LayerInfo>();
		List<LayerInfo> allLayers = getLayers(mp);
		for (LayerInfo li: allLayers) {
			map.put(li.getUri(), li);
		}
		return map;
	}
	
	/**
	 * get the layers filtered by tag
	 * 
	 * @param tag
	 * @param mp
	 * @return
	 * @throws JSONException
	 * @throws org.sead.acr.common.utilities.json.JSONException
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public static List<LayerInfo> getLayersByTag(String tag, MediciProxy mp)
			throws MalformedURLException, IOException,
			org.sead.acr.common.utilities.json.JSONException, JSONException {
		
		List<LayerInfo> layers = new ArrayList<LayerInfo>();
		LinkedHashMap<String, LayerInfo> map = getLayerInfoMap(mp);
		List<String> urisByTag = getUrisByTag(tag, mp);
		
		for(String uri: map.keySet()) {
			if(urisByTag.contains(uri) ) {
				layers.add(map.get(uri));
			}
		}
		
		return layers;
		
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
	public static List<LayerInfo> parseLayerInfo(String layers)
			throws JSONException, MalformedURLException,
			UnsupportedEncodingException {

		JSONObject layerObj = new JSONObject(layers);

		// new json arry to store the extracted info
		List<LayerInfo> layerInfoList = new ArrayList<LayerInfo>();

		JSONArray resultArray = layerObj.getJSONObject("sparql")
				.getJSONObject("results").getJSONArray("result");
		for (int i = 0; i < resultArray.length(); i++) {

			// getting wmsURL to parse out extents
			JSONArray jsonArray = resultArray.getJSONObject(i).getJSONArray(
					"binding");

			LayerInfo li = new LayerInfo();

			String uri = "";
			String layerName = "";
			String layerUrl = "";
			String extents = "";
			String deleted = "";
			String title = "";
			String srs = "";
			for (int j = 0; j < jsonArray.length(); j++) {
				JSONObject entry = jsonArray.getJSONObject(j);
				if (entry.getString("name").equals("uri")) {
					uri = entry.getString("uri");
				} else if (entry.getString("name").equals("layername")) {
					layerName = entry.getString("literal");
				} else if (entry.getString("name").equals("title")) {
					title = entry.getString("literal");
				} else if (entry.getString("name").equals("layerurl")) {
					layerUrl = entry.getString("literal");
					Map<String, String> params = splitQuery(new URL(layerUrl));
					extents = params.get("bbox");
					srs = params.get("srs");
				} else if (entry.getString("name").equals("deleted")) {
					deleted = entry.getString("uri");
				}
			}

			// if the dataset is deleted, skip
			if (deleted
					.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil"))
				continue;
			li.setSrs(srs);
			li.setName(layerName);
			li.setTitle(title);
			li.setUri(uri);
			String[] split = extents.split(",");
			li.setMinx(Double.parseDouble(split[0]));
			li.setMiny(Double.parseDouble(split[1]));
			li.setMaxx(Double.parseDouble(split[2]));
			li.setMaxy(Double.parseDouble(split[3]));

			layerInfoList.add(li);

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
