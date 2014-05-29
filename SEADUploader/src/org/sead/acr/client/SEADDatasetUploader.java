/*******************************************************************************
 * Copyright 2014 University of Michigan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.sead.acr.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SEADDatasetUploader {

	private static final String FRBR_EO = "http://purl.org/vocab/frbr/core#embodimentOf";
	private static final String SHA1_DIGEST = "http://sead-data.net/terms/hasSHA1Digest";
	private static final String DCTERMS_HAS_PART = "http://purl.org/dc/terms/hasPart";

	private static long max = 9223372036854775807l;
	private static boolean merge = false;

	private static long globalFileCount = 0l;
	private static long totalBytes = 0L;

	protected static boolean listonly = false;

	private static String server = null;

	static PrintWriter pw = null;
	private static HttpClientContext localContext = HttpClientContext.create();
	private static String refresh_token;
	private static String access_token;
	private static long tokenStartTime;
	private static int token_expires_in;

	public static void main(String args[]) throws Exception {

		// Create a local instance of cookie store
		CookieStore cookieStore = new BasicCookieStore();

		// Create local HTTP context

		// Bind custom cookie store to the local context
		localContext.setCookieStore(cookieStore);

		File outputFile = new File("SEADUploadLog_"
				+ System.currentTimeMillis() + ".txt");
		try {
			pw = new PrintWriter(new FileWriter(outputFile));
		} catch (Exception e) {
			println(e.getMessage());
		}

		for (String arg : args) {
			println("Arg is : " + arg);
			if (arg.equalsIgnoreCase("-listonly")) {
				listonly = true;
				println("List Only Mode");
			} else if (arg.equals("-merge")) {
				merge = true;
				println("Merge mode ON");

			} else if (arg.startsWith("-limit")) {
				max = Long.parseLong(arg.substring(6));
				println("Max ingest file count: " + max);
			}
		}

		// go through arguments
		for (String arg : args) {
			if (!((arg.equalsIgnoreCase("-listonly")) || (arg.equals("-merge")) || (arg
					.startsWith("-limit")))) {
				// First non-flag arg is the server URL
				if (server == null) {
					// println("setting server: " + arg);
					server = arg;
					if (!authenticate(server)) {
						println("Authentication failure - exiting.");
						System.exit(0);
					}
				} else {
					File file = new File(arg);
					String tagId = null;
					if (merge) {
						tagId = itemExists("/" + file.getName());
					}

					if (file.isDirectory()) {

						String newUri = uploadCollection(file, "", null, tagId);
						if (newUri != null) {
							println("              " + file.getPath()
									+ " CREATED as: " + newUri);
						} else if(tagId==null) {
							println("Error processing: " + file.getPath());
						}  else {
							println ("              Found " + file.getPath() + " as " + tagId);
						}

					} else {

						if (globalFileCount < max) {
							String newUri = uploadDataset(file, null, tagId);
							if (newUri != null) {
								println("              UPLOADED as: " + newUri);
							} else if(tagId==null) {
								println("Error processing: " + file.getPath());
							} else {
								println ("              Found " + file.getPath() + " as " + tagId);
							}
						}
					}
				}
			}
		}
		if (pw != null) {
			pw.flush();
			pw.close();
		}

	}

	private static boolean authenticate(String server) {

		boolean authenticated = false;
		println("Authenticating");
		String user_code = null;
		String device_code = null;
		String verification_url = null;
		int expires_in = 0;

		ObjectMapper mapper = new ObjectMapper();

		GoogleProps gProps;

		try {
			refresh_token = new String(Files.readAllBytes(Paths
					.get("refresh.txt")));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// Contact google for a user code
		CloseableHttpClient httpclient = HttpClients.createDefault();

		try {

			// Read Google Oauth2 info
			gProps = mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE,
					true).readValue(new File("sead-google.json"),
					GoogleProps.class);

			if (refresh_token == null) {
				String codeUri = gProps.auth_uri.substring(0,
						gProps.auth_uri.length() - 4)
						+ "device/code";

				HttpPost codeRequest = new HttpPost(codeUri);

				MultipartEntityBuilder meb = MultipartEntityBuilder.create();
				addLiteralMetadata(meb, "client_id", gProps.client_id);
				addLiteralMetadata(meb, "scope", "email profile");
				HttpEntity reqEntity = meb.build();

				codeRequest.setEntity(reqEntity);
				CloseableHttpResponse response = httpclient
						.execute(codeRequest);
				try {

					if (response.getStatusLine().getStatusCode() == 200) {
						HttpEntity resEntity = response.getEntity();
						if (resEntity != null) {
							String responseJSON = EntityUtils
									.toString(resEntity);
							ObjectNode root = (ObjectNode) new ObjectMapper()
									.readTree(responseJSON);
							device_code = root.get("device_code").asText();
							user_code = root.get("user_code").asText();
							verification_url = root.get("verification_url")
									.asText();
							expires_in = root.get("expires_in").asInt();
						}
					} else {
						println("Error response from Google: "
								+ response.getStatusLine().getReasonPhrase());
					}
				} finally {
					response.close();
				}
				// Ask user to login via browser
				println("To begin upload, login via Google:");
				System.out.println("1) Go to : " + verification_url
						+ " in your browser");
				System.out.println("2) Type : " + user_code
						+ " in your browser");
				System.out.println("3) Hit <Return> to continue.");
				System.in.read();

				System.out.println("Proceeding");

				// Query for token now that user has gone through browser part
				// of
				// flow
				HttpPost tokenRequest = new HttpPost(gProps.token_uri);

				MultipartEntityBuilder tokenRequestParams = MultipartEntityBuilder
						.create();
				addLiteralMetadata(tokenRequestParams, "client_id",
						gProps.client_id);
				addLiteralMetadata(tokenRequestParams, "client_secret",
						gProps.client_secret);
				addLiteralMetadata(tokenRequestParams, "code", device_code);
				addLiteralMetadata(tokenRequestParams, "grant_type",
						"http://oauth.net/grant_type/device/1.0");

				reqEntity = tokenRequestParams.build();

				tokenRequest.setEntity(reqEntity);

				response = httpclient.execute(tokenRequest);
				try {
					if (response.getStatusLine().getStatusCode() == 200) {
						HttpEntity resEntity = response.getEntity();
						if (resEntity != null) {
							String responseJSON = EntityUtils
									.toString(resEntity);
							println(responseJSON);
							ObjectNode root = (ObjectNode) new ObjectMapper()
									.readTree(responseJSON);
							access_token = root.get("access_token").asText();
							refresh_token = root.get("refresh_token").asText();
							tokenStartTime = System.currentTimeMillis() / 1000;
							token_expires_in = root.get("expires_in").asInt();
						}
					} else {
						println("Error response from Google: "
								+ response.getStatusLine().getReasonPhrase());
					}
				} finally {
					response.close();
				}

				if (refresh_token != null) {
					PrintWriter writer = new PrintWriter("refresh.txt", "UTF-8");
					writer.println(refresh_token);
					writer.close();
				}
			} else {
				/* Try refresh token */
				// Query for token now that user has gone through browser part
				// of
				// flow
				HttpPost refreshRequest = new HttpPost(gProps.token_uri);

				MultipartEntityBuilder tokenRequestParams = MultipartEntityBuilder
						.create();
				addLiteralMetadata(tokenRequestParams, "client_id",
						gProps.client_id);
				addLiteralMetadata(tokenRequestParams, "client_secret",
						gProps.client_secret);
				addLiteralMetadata(tokenRequestParams, "refresh_token",
						refresh_token);
				addLiteralMetadata(tokenRequestParams, "grant_type",
						"refresh_token");

				HttpEntity reqEntity = tokenRequestParams.build();

				refreshRequest.setEntity(reqEntity);

				CloseableHttpResponse response = httpclient
						.execute(refreshRequest);
				try {
					if (response.getStatusLine().getStatusCode() == 200) {
						HttpEntity resEntity = response.getEntity();
						if (resEntity != null) {
							String responseJSON = EntityUtils
									.toString(resEntity);
							println(responseJSON);
							ObjectNode root = (ObjectNode) new ObjectMapper()
									.readTree(responseJSON);
							access_token = root.get("access_token").asText();
							// refresh_token =
							// root.get("refresh_token").asText();
							tokenStartTime = System.currentTimeMillis() / 1000;
							token_expires_in = root.get("expires_in").asInt();
						}
					} else {
						println("Error response from Google: "
								+ response.getStatusLine().getReasonPhrase());
						new File("refresh.txt").delete();
					}
				} finally {
					response.close();
				}

			}
			// Now login to server and create a session
			HttpPost seadAuthenticate = new HttpPost(server
					+ "/api/authenticate");
			List<NameValuePair> nvpList = new ArrayList<NameValuePair>(1);
			nvpList.add(0, new BasicNameValuePair("googleAccessToken",
					access_token));

			seadAuthenticate.setEntity(new UrlEncodedFormEntity(nvpList));

			CloseableHttpResponse response = httpclient.execute(
					seadAuthenticate, localContext);
			try {
				if (response.getStatusLine().getStatusCode() == 200) {
					HttpEntity resEntity = response.getEntity();
					if (resEntity != null) {
						String seadSessionId = EntityUtils.toString(resEntity);
						authenticated = true;

					}
				} else {
					println("Error response from " + server + " : "
							+ response.getStatusLine().getReasonPhrase());
				}
			} finally {
				response.close();
				httpclient.close();
			}
		} catch (IOException e) {
			println("Cannot read sead-google.json");
			println(e.getMessage());
		}

		// localContext should have the cookie with the SEAD session key, which
		// nominally is all that's needed.
		// FixMe: If there is no activity for more than 15 minutes, the session
		// may expire, in which case,
		// re-authentication using the refresh token to get a new google token
		// to allow SEAD login again may be required
		return authenticated;
	}

	protected static String uploadCollection(File dir, String path,
			String parentId, String collectionId) {
		Set<String> childDatasetUris = new HashSet<String>();
		Set<String> childCollectionUris = new HashSet<String>();

		println("\nPROCESSING(C): " + dir.getPath());
		if (collectionId != null) {
			println("              Found as: " + collectionId);
		} else {
			println("              Does not yet exist on server.");
		}
		path += "/" + dir.getName();

		try {
			for (File file : dir.listFiles()) {
				if (file.getName().startsWith(".")) {
					continue;
				}
				String existingUri = null;
				String newUri = null;

				if (merge) {
					existingUri = itemExists(path + "/" + file.getName());

				}
				/*
				 * Stop processing new items when we hit the limit, but finish
				 * writing/adding children to the current collection.
				 */
				if (globalFileCount < max) {

					/*
					 * If existingUri != null, recursive calls will check
					 * children only (and currently will do nothing for a
					 * dataset, but someday may check for changes and upload a
					 * new version.)
					 */

					if (file.isDirectory()) {
						newUri = uploadCollection(file, path, collectionId,
								existingUri);
						if ((existingUri == null) && (newUri != null)) {
							println("              " + file.getPath()
									+ " CREATED as: " + newUri);
						}
					} else {

						// fileStats[1] += file.length();
						newUri = uploadDataset(file, path, existingUri);
						if ((existingUri == null) && (newUri != null)) {
							globalFileCount++;
							totalBytes += file.length();
							println("              UPLOADED as: " + newUri);
							println("CURRENT TOTAL: " + globalFileCount
									+ " files :" + totalBytes + " bytes");
						}
					}
				}

				if (!listonly) {
					// If we're trying to write (w or w/o merge), a null means
					// an error
					// If listOnly, we don't need to do anything with childUris
					// and don't care about returned value (null if !merge, id
					// or null if merge)
					if (existingUri == null) {
						if (newUri != null) { // Child didn't exist, now it does
							if (file.isDirectory()) {
								childCollectionUris.add(newUri);
							} else {
								childDatasetUris.add(newUri);
							}
						} else { // Child didn't exist and still doesn't
							throw (new IOException(
									"Did not process item correctly: "
											+ file.getAbsolutePath()));
						}
					} else if (collectionId != null) { // Child did exist, and
														// parent (current
														// focus) does
						// Need to check existing children and add any missing
						// ones that aren't new (those were already added)
						Set<String> children = getChildren(collectionId);
						if (!children.contains(existingUri)) {
							if (file.isDirectory()) {
								childCollectionUris.add(existingUri);
							} else {
								childDatasetUris.add(existingUri);
							}

						}
					}
				}
			}

			if (!listonly) { // We're potentially making changes
				if (collectionId == null) { // a collection for path not on
											// server or we
											// don't care - we have to write the
											// collection

					CloseableHttpClient httpclient = HttpClients
							.createDefault();
					try {

						HttpPost httppost = new HttpPost(server
								+ "/resteasy/collections");

						MultipartEntityBuilder meb = MultipartEntityBuilder
								.create();
						addLiteralMetadata(meb, "collection", dir.getName());
						addLiteralMetadata(meb, FRBR_EO, path);
						for (String child : childCollectionUris) {
							addURIMetadata(meb, DCTERMS_HAS_PART, child);
						}
						for (String child : childDatasetUris) {
							addURIMetadata(meb, DCTERMS_HAS_PART, child);
						}
						HttpEntity reqEntity = meb.build();
						httppost.setEntity(reqEntity);
						CloseableHttpResponse response = httpclient.execute(
								httppost, localContext);
						try {
							if (response.getStatusLine().getStatusCode() == 200) {
								HttpEntity resEntity = response.getEntity();
								if (resEntity != null) {
									collectionId = EntityUtils
											.toString(resEntity);
								}
							} else {
								println("Error response when processing "
										+ dir.getAbsolutePath()
										+ " : "
										+ response.getStatusLine()
												.getReasonPhrase());

							}
						} finally {
							response.close();
						}
					} catch (IOException e) {
						println("Error processing " + dir.getAbsolutePath()
								+ " : " + e.getMessage());
					} finally {
						try {
							httpclient.close();
						} catch (IOException e) {
							println("Couldn't close connection for file: "
									+ dir.getAbsolutePath() + " : "
									+ e.getMessage());
						}
					}
				} else {
					// Just need to add any new children - addsubCollcall
					CloseableHttpClient httpclient = HttpClients
							.createDefault();
					try {
						for (String child : childDatasetUris) {
							createChildRelationship(httpclient, collectionId,
									child, true);
						}
						for (String child : childCollectionUris) {
							createChildRelationship(httpclient, collectionId,
									child, false);
						}

					} finally {
						try {
							// don't report collection as new
							collectionId = null;
							httpclient.close();
						} catch (IOException e) {
							println("Couldn't close connection for file: "
									+ dir.getAbsolutePath() + " : "
									+ e.getMessage());
						}
					}

				}
			}
		} catch (IOException io) {
			println("error: " + io.getMessage());// One or more files not
													// uploaded correctly - stop
													// processing...
		}
		return collectionId;
	}

	private static Set<String> getChildren(String collectionId) {
		Set<String> tagIds = new HashSet<String>();
		CloseableHttpClient httpclient = HttpClients.createDefault();

		try {
			String serviceUrl = server + "/resteasy/collections/"
					+ URLEncoder.encode(collectionId, "UTF-8") + "/"
					+ "datasets";

			HttpGet httpget = new HttpGet(serviceUrl);
			CloseableHttpResponse response = httpclient.execute(httpget,
					localContext);
			try {
				if (response.getStatusLine().getStatusCode() == 200) {
					HttpEntity resEntity = response.getEntity();
					if (resEntity != null) {
						String json = EntityUtils.toString(resEntity);
						Map<String, Object> mapObject = new ObjectMapper()
								.readValue(
										json,
										new TypeReference<Map<String, Object>>() {
										});
						if (mapObject.size() > 1) {
							for (String key : mapObject.keySet()) {
								if (!key.equals("@context")) {
									tagIds.add(key);
								}
							}
						}

					}
				} else {
					println("Error response when checking for children of "
							+ collectionId + " : "
							+ response.getStatusLine().getReasonPhrase());

				}
			} finally {
				response.close();
			}
		} catch (IOException e) {
			println("Error processing children check on " + collectionId
					+ " : " + e.getMessage());
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				println("Couldn't close httpclient: " + e.getMessage());
			}
		}
		return (tagIds);
	}

	protected static void createChildRelationship(HttpClient httpclient,
			String collectionId, String child, boolean isDataset)
			throws IOException {

		String url = server + "/resteasy/collections/"
				+ URLEncoder.encode(collectionId, "UTF-8");
		if (isDataset) {
			url = url + "/datasets";
		} else {
			url = url + "/collections";
		}
		HttpPost addChildPost = new HttpPost(url);

		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
		if (isDataset) {
			postParameters.add(new BasicNameValuePair("dataset_id", child));
		} else {
			postParameters.add(new BasicNameValuePair("collection_id", child));

		}

		addChildPost.setEntity(new UrlEncodedFormEntity(postParameters));
		CloseableHttpResponse response = (CloseableHttpResponse) httpclient
				.execute(addChildPost, localContext);
		try {
			if (response.getStatusLine().getStatusCode() == 200) {
				println("ADDED " + child + " to existing collection "
						+ collectionId);
			} else {
				println("Error adding " + child + " to " + collectionId + " : "
						+ response.getStatusLine().getReasonPhrase() + " : "
						+ EntityUtils.toString(response.getEntity()));
			}
		} finally {
			try {
				response.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static String uploadDataset(File file, String path, String dataId) {

		println("\nPROCESSING(D): " + file.getPath());
		if (dataId != null) {
			println("              Found as: " + dataId);
		} else {
			println("              Does not yet exist on server.");
		}
		path += "/" + file.getName();

		if (!listonly) {
			if (dataId == null) { // doesn't exist or we don't care (!merge)
				CloseableHttpClient httpclient = HttpClients.createDefault();
				try {

					HttpPost httppost = new HttpPost(server
							+ "/resteasy/datasets");

					FileBody bin = new FileBody(file);
					MultipartEntityBuilder meb = MultipartEntityBuilder
							.create();
					meb.addPart("datablob", bin);

					addLiteralMetadata(meb, FRBR_EO, path);
					// addLiteralMetadata(meb, "http://purl.org/dc/terms/title",
					// fileName);

					/*
					 * addURIMetadata(meb, "http://purl.org/dc/terms/creator",
					 * "http://localhot:1234/me"); addURIMetadata(meb,
					 * "http://purl.org/dc/terms/description",
					 * "http://localhost:1234/desc1"); addURIMetadata(meb,
					 * "http://purl.org/dc/terms/description",
					 * "http://localhost:1234/desc2");
					 */
					HttpEntity reqEntity = meb.build();

					httppost.setEntity(reqEntity);

					CloseableHttpResponse response = httpclient.execute(
							httppost, localContext);
					try {
						if (response.getStatusLine().getStatusCode() == 200) {
							HttpEntity resEntity = response.getEntity();
							if (resEntity != null) {
								dataId = EntityUtils.toString(resEntity);

							}
						} else {
							println("Error response when processing "
									+ file.getAbsolutePath()
									+ " : "
									+ response.getStatusLine()
											.getReasonPhrase());

						}
					} finally {
						response.close();
					}
				} catch (IOException e) {
					println("Error processing " + file.getAbsolutePath()
							+ " : " + e.getMessage());
				} finally {
					try {
						httpclient.close();
					} catch (IOException e) {
						println("Couldn't close connection for file: "
								+ file.getAbsolutePath() + " : "
								+ e.getMessage());
					}
				}
			} else {
				dataId = null;
				// FixMe - if dataId exists, we could check sha1 and upload a
				// version if file changes

			}
		}
		return dataId;
	}

	static String itemExists(String sourcepath) {
		String tagId = null;
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			String serviceUrl = server + "/resteasy/collections/metadata/"
					+ URLEncoder.encode(FRBR_EO, "UTF-8") + "/" + "literal/"
					+ URLEncoder.encode(sourcepath, "UTF-8");
			HttpGet httpget = new HttpGet(serviceUrl);

			CloseableHttpResponse response = httpclient.execute(httpget,
					localContext);
			try {
				if (response.getStatusLine().getStatusCode() == 200) {
					HttpEntity resEntity = response.getEntity();
					if (resEntity != null) {
						String json = EntityUtils.toString(resEntity);
						Map<String, Object> mapObject = new ObjectMapper()
								.readValue(
										json,
										new TypeReference<Map<String, Object>>() {
										});
						if (mapObject.size() > 1) {
							for (String key : mapObject.keySet()) {
								if (!key.equals("@context")) {
									tagId = key;
									break;
								}
							}
						}

					}
				} else {
					println("Error response when checking for existing item at "
							+ sourcepath
							+ " : "
							+ response.getStatusLine().getReasonPhrase());

				}
			} finally {
				response.close();
			}
		} catch (IOException e) {
			println("Error processing check on " + sourcepath + " : "
					+ e.getMessage());
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				println("Couldn't close httpclient: " + e.getMessage());
			}
		}

		return (tagId);
	}

	static void addLiteralMetadata(MultipartEntityBuilder meb,
			String predicate, String value) {
		meb.addTextBody(predicate, value);

	}

	static void addURIMetadata(MultipartEntityBuilder meb, String predicate,
			String value) {
		meb.addTextBody(predicate, value,
				ContentType.create("text/uri-list", Consts.ISO_8859_1));
	}

	protected static void println(String s) {
		System.out.println(s);
		if (pw != null)
			pw.println(s);
		return;
	}
}