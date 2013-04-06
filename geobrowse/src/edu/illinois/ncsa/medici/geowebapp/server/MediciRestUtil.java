package edu.illinois.ncsa.medici.geowebapp.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sun.misc.BASE64Encoder;

public class MediciRestUtil {
//	public static String tagRestUrl = "http://sead.ncsa.illinois.edu/resteasy/tags";
	public static String tagRestUrl;
	public static String user;
	public static String pw;

	public static List<String> getTags() {
		List<String> tags = new ArrayList<String>();
		DefaultHttpClient httpclient = new DefaultHttpClient();
		try {
			HttpGet httpget = new HttpGet(tagRestUrl);
			String userPassString = user + ":" + pw;
			BASE64Encoder b = new BASE64Encoder();

			httpget.addHeader("Authorization",
					"Basic " + b.encode(userPassString.getBytes()));

			System.out.println("executing request" + httpget.getRequestLine());
			// HttpResponse response = httpclient.execute(httpget);

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = httpclient.execute(httpget, responseHandler);
			System.out.println(responseBody);
			JSONArray jsArray = new JSONArray(responseBody);
			for (int i = 0; i < jsArray.length(); i++) {
				String tag = jsArray.getString(i);
				tags.add(tag);
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			httpclient.getConnectionManager().shutdown();
		}

		return tags;
	}

	public static List<String> getUrisByTag(String tag) {
		List<String> uris = new ArrayList<String>();
		DefaultHttpClient httpclient = new DefaultHttpClient();
		try {
			HttpGet httpget = new HttpGet(tagRestUrl + "/" + tag + "/datasets");
			String userPassString = user + ":" + pw;
			BASE64Encoder b = new BASE64Encoder();

			httpget.addHeader("Authorization",
					"Basic " + b.encode(userPassString.getBytes()));

			System.out.println("executing request" + httpget.getRequestLine());
			// HttpResponse response = httpclient.execute(httpget);

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = httpclient.execute(httpget, responseHandler);

			JSONArray jsArray = new JSONArray(responseBody);
			for (int i = 0; i < jsArray.length(); i++) {
				JSONObject js = jsArray.getJSONObject(i);
				String uri = js.getString("uri");
				System.out.println(uri);
				uris.add(uri);
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			httpclient.getConnectionManager().shutdown();
		}

		return uris;
	}

}
