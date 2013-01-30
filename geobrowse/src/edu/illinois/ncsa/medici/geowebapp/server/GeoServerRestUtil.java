package edu.illinois.ncsa.medici.geowebapp.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GeoServerRestUtil {
	public static String URL = "http://sead.ncsa.illinois.edu/geoserver/rest";
	public static String TAG_URL = "http://sead.ncsa.illinois.edu/resteasy/tags/[tag]/datasets";

	public static String getStores(String workspaceName) {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		try {
			httpclient.getCredentialsProvider().setCredentials(
					new AuthScope("sead.ncsa.illinois.edu", 80),
					new UsernamePasswordCredentials("", ""));

			HttpGet httpget = new HttpGet(URL + "/workspaces/" + workspaceName
					+ "/datastores.json");

			System.out.println("executing request" + httpget.getRequestLine());
			// HttpResponse response = httpclient.execute(httpget);

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = httpclient.execute(httpget, responseHandler);
			System.out.println(responseBody);

			JSONObject js = new JSONObject(responseBody);
			JSONObject dataStores = js.getJSONObject("dataStores");
			JSONArray dataStore = dataStores.getJSONArray("dataStore");
			System.out.println(dataStore.length());
			for (int i = 0; i < dataStore.length(); i++) {
				JSONObject ds = dataStore.getJSONObject(i);
				System.out.println(ds.getString("name"));
			}

			// HttpEntity entity = response.getEntity();
			//
			// System.out.println("----------------------------------------");
			// System.out.println(response.getStatusLine());
			// if (entity != null) {
			// System.out.println("Response content length: "
			// + entity.getContentLength());
			// System.out.println(entity.getc);
			// }
			// EntityUtils.consume(entity);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
		}

		return null;
	}

	public static Map<String, String> getLayers() {
		// uri, name
		Map<String, String> uriToNameMap = new HashMap<String, String>();
		DefaultHttpClient httpclient = new DefaultHttpClient();
		try {
			httpclient.getCredentialsProvider().setCredentials(
					new AuthScope("sead.ncsa.illinois.edu", 80),
					new UsernamePasswordCredentials("admin", "NCSAsead"));

			HttpGet httpget = new HttpGet(URL + "/layers.json");

			System.out.println("executing request" + httpget.getRequestLine());
			// HttpResponse response = httpclient.execute(httpget);

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = httpclient.execute(httpget, responseHandler);
			System.out.println(responseBody);

			JSONObject js = new JSONObject(responseBody);
			JSONObject layers = js.getJSONObject("layers");
			JSONArray layer = layers.getJSONArray("layer");
			System.out.println(layer.length());
			for (int i = 0; i < layer.length(); i++) {
				JSONObject ds = layer.getJSONObject(i);
				String name = ds.getString("name");
				if (name != null) {
					String[] nameUriPair = getLayer(name);
					if (nameUriPair != null) {
						uriToNameMap.put(nameUriPair[1], nameUriPair[0]);
						System.out.println(nameUriPair[1] + ", "
								+ nameUriPair[0]);
					}
				}
			}
			return uriToNameMap;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
		}

		return uriToNameMap;
	}

	public static String[] getLayer(String name) {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		try {
			httpclient.getCredentialsProvider().setCredentials(
					new AuthScope("sead.ncsa.illinois.edu", 80),
					new UsernamePasswordCredentials("admin", "NCSAsead"));

			HttpGet httpget = new HttpGet(URL + "/layers/" + name + ".json");

			System.out.println("Getting layer [" + name + "]: "
					+ httpget.getRequestLine());
			// HttpResponse response = httpclient.execute(httpget);

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = httpclient.execute(httpget, responseHandler);

			JSONObject js = new JSONObject(responseBody);
			JSONObject layer = js.getJSONObject("layer");
			JSONObject resource = layer.getJSONObject("resource");
			String href = resource.getString("href");

			String uri = getURIfromHref(href);
			if (uri != null) {
				System.out.println(name + ", " + uri);
				return new String[] { name, uri };
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
		}

		return null;
	}

	public static String getURIfromHref(String href) {
		String[] split = href.split("/");
		for (int i = 0; i < split.length; i++) {
			String s = split[i];
			if ("datastores".equals(s))
				return split[i + 1];
		}
		return null;
	}

	// public static String getLayers() {
	// String responseStr = null;
	//
	// String requestUrl = URL + "/layers.json";
	//
	// DefaultHttpClient httpclient = new DefaultHttpClient();
	//
	// String encoding = (String) Base64.encode("test1:test1");
	// HttpPost httppost = new HttpPost("http://host:post/test/login");
	// httppost.setHeader("Authorization", "Basic " + encoding);
	//
	// System.out.println("executing request " + httppost.getRequestLine());
	// HttpResponse response = httpclient.execute(httppost);
	// HttpGet httpget = new HttpGet(requestUrl);
	//
	// ResponseHandler<String> responseHandler = new BasicResponseHandler();
	// try {
	//
	// // httpclient.getCredentialsProvider().setCredentials(
	// // new AuthScope(targetHost.getHostName(),
	// // targetHost.getPort()),
	// // new UsernamePasswordCredentials("admin", "NCSAsead"));
	//
	// // Create AuthCache instance
	// // AuthCache authCache = new BasicAuthCache();
	// // Generate BASIC scheme object and add it to the local auth cache
	// // BasicScheme basicAuth = new BasicScheme();
	// // authCache.put(targetHost, basicAuth);
	//
	// // Add AuthCache to the execution context
	// BasicHttpContext localcontext = new BasicHttpContext();
	// // localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);
	//
	// HttpResponse response = httpclient.execute(targetHost, httpget,
	// localcontext);
	// return responseStr;
	// } catch (ClientProtocolException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// } finally {
	// try {
	// httpclient.getConnectionManager().shutdown();
	// return responseStr;
	// } catch (Exception ignore) {
	// }
	// }
	// return null;
	// }
	//
	// public static void main(String[] args) {
	// System.out.println(getLayers());
	// }
	public static void main(String[] args) throws Exception {
		getStores("medici");
		getLayers();
	}
}
