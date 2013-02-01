package edu.illinois.ncsa.medici.geowebapp.server;

import java.io.IOException;
import java.net.URLEncoder;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import sun.misc.BASE64Encoder;

public class MediciRestUtil {
	public static String TAG_URL = "http://sead.ncsa.illinois.edu/resteasy/tags";

	public static String getUriByTag(String tag) {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		try {
			HttpGet httpget = new HttpGet(TAG_URL + "/" + tag + "/datasets");
			String u = "";
			String p = "";
			String userPassString = u+":"+p;
			BASE64Encoder b = new BASE64Encoder();

			httpget.addHeader("Authorization",
					"Basic " + b.encode(userPassString.getBytes()));

			System.out.println("executing request" + httpget.getRequestLine());
			// HttpResponse response = httpclient.execute(httpget);

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = httpclient.execute(httpget, responseHandler);
			System.out.println(responseBody);

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();

		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
		}

		return null;
	}

	public static void main(String[] args) {
		getUriByTag("analysis");
	}
}
