package edu.illinois.ncsa.medici.geowebapp.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.sead.acr.common.MediciProxy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MediciRestUtil {

	protected static Log log = LogFactory.getLog(MediciRestUtil.class);

	public static List<String> getTags(MediciProxy mp) throws IOException, JSONException {
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

	public static List<String> getUrisByTag(String tag, MediciProxy mp) throws IOException, JSONException {

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

}
