package edu.illinois.ncsa.medici.geowebapp.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sead.acr.common.MediciProxy;
import org.sead.acr.common.utilities.PropertiesLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.medici.geowebapp.shared.LayerInfo;

public class GeoServerRestUtil {

	protected static Log log = LogFactory.getLog(GeoServerRestUtil.class);


	public static List<LayerInfo> getLayers(MediciProxy mp) throws IOException, JSONException {
		List<LayerInfo> layerList = new ArrayList<LayerInfo>();

			String responseBody = mp.executeAuthenticatedGeoGet(
					"/rest/layers.json", null);

			JSONObject js = new JSONObject(responseBody);
			JSONObject layers = js.getJSONObject("layers");
			JSONArray layer = layers.getJSONArray("layer");
			for (int i = 0; i < layer.length(); i++) {
				JSONObject ds = layer.getJSONObject(i);
				String name = ds.getString("name");
				if (name != null) {
					LayerInfo layerInfo = getLayer(name, mp);
					if (layerInfo != null) {
						layerList.add(layerInfo);
					}
				}
			}


		return layerList;
	}

	public static Map<String, LayerInfo> getLayerUriMap(MediciProxy mp)  throws IOException, JSONException{
		// uri, name
		Map<String, LayerInfo> uriToNameMap = new HashMap<String, LayerInfo>();


			String responseBody = mp.executeAuthenticatedGeoGet(
					"/rest/layers.json", null);

			JSONObject js = new JSONObject(responseBody);
			JSONObject layers = js.getJSONObject("layers");
			JSONArray layer = layers.getJSONArray("layer");
			log.debug(layer.length());
			for (int i = 0; i < layer.length(); i++) {
				JSONObject ds = layer.getJSONObject(i);
				String name = ds.getString("name");
				if (name != null) {
					LayerInfo layerInfo = getLayer(name, mp);
					if (layerInfo != null) {
						uriToNameMap.put(layerInfo.getUri(), layerInfo);
					}
				}
			}
		return uriToNameMap;
	}

	public static LayerInfo getLayer(String name, MediciProxy mp)  throws IOException, JSONException {
		LayerInfo layerInfo = null;
	
			String responseBody = mp.executeAuthenticatedGeoGet("/rest/layers/"
					+ name + ".json", null);

			JSONObject js = new JSONObject(responseBody);
			JSONObject layer = js.getJSONObject("layer");
			JSONObject resource = layer.getJSONObject("resource");
			String type = layer.getString("type");
			String href = resource.getString("href");

			String relHref = getRelativeFromHref(href);
			String uri = getURIfromHref(href);

			if (uri != null) {
				layerInfo = new LayerInfo();
				layerInfo.setName(name);
				layerInfo.setUri(uri);

				// getting lat lon bounding box
				log.debug("Getting featureType/coverage [" + name + "]: "
						+ relHref);

				responseBody = mp.executeAuthenticatedGeoGet(relHref, null);

				js = new JSONObject(responseBody);
				JSONObject dataStore = null;
				if (type.equals("RASTER")) {
					dataStore = js.getJSONObject("coverage");
				} else {
					dataStore = js.getJSONObject("featureType");
				}
				JSONObject latLongBBox = dataStore
						.getJSONObject("latLonBoundingBox");
				layerInfo.setCrs(latLongBBox.getString("crs"));
				layerInfo.setMinx(latLongBBox.getDouble("minx"));
				layerInfo.setMaxx(latLongBBox.getDouble("maxx"));
				layerInfo.setMiny(latLongBBox.getDouble("miny"));
				layerInfo.setMaxy(latLongBBox.getDouble("maxy"));
				log.debug(name + ", " + uri);
			}
		return layerInfo;
	}

	private static String getRelativeFromHref(String href) {
		String rel = null;
		String server = PropertiesLoader.getProperties().getProperty(
				"geoserver");
		if (href.indexOf(server) == 0) {
			rel = href.substring(server.length());
		} else {
			log.warn("href of layer does not match geoserver URL");
			log.warn("href:" +  href);
			log.warn("geoserver URL: " + server);
		}
		return rel;
	}

	public static String getURIfromHref(String href) {
		String[] split = href.split("/");
		for (int i = 0; i < split.length; i++) {
			String s = split[i];
			if ("datastores".equals(s) || "coveragestores".equals(s)) { // support
																		// both
																		// vector
																		// and
																		// coverage
				try {
					if (split[i + 1] == null)
						return null;
					String uri = URLDecoder.decode(split[i + 1], "UTF-8");
					return uri;
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					return null;
				}
			}
		}
		return null;
	}

	public static List<LayerInfo> getLayersByTag(String tag, MediciProxy mp)   throws IOException, JSONException {
		List<LayerInfo> layers = new ArrayList<LayerInfo>();

		// getting URIs by tag
		List<String> urisByTag = MediciRestUtil.getUrisByTag(tag, mp);
		if (urisByTag.isEmpty())
			return layers;

		// getting layers with uri
		Map<String, LayerInfo> layerMap = getLayerUriMap(mp);
		if (layerMap.isEmpty())
			return layers;

		for (String uri : urisByTag) {
			LayerInfo layer = layerMap.get(uri);
			if (layer != null)
				layers.add(layer);
		}
		return layers;
	}

	public static void main(String[] args) throws Exception {
		MediciProxy mp = new MediciProxy();
		mp.setGeoCredentials("admin", "password",
				"http://sead.ncsa.illinois.edu/geoserver");

		List<LayerInfo> layersByTag = getLayersByTag("angelo", mp);
		for (LayerInfo l : layersByTag) {
			log.debug(l.getName());
		}
	}
}
