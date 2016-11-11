/*******************************************************************************
 * Copyright 2014, 2016 University of Michigan
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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.border.EmptyBorder;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sead.acr.client.util.FileResource;
import org.sead.acr.client.util.PublishedFolderProxyResource;
import org.sead.acr.client.util.PublishedResource;
import org.sead.acr.client.util.Resource;
import org.sead.acr.client.util.ResourceFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * The SEAD Uploader supports the upload of files/directories from a local disk,
 * or existing SEAD publications for which a valid OREMap file is available from
 * a URL (repositories must update the data file links in the ORE for the
 * Uploader to retrieve them)
 * 
 * In addition to sending files and creating a SEAD collection/dataset (1.5) or
 * Dataset/Folder/File (2.0) structure, the Uploader adds path metadata, usable
 * in detecting whether an item has already been created/uplaoded. For
 * publications, it also sends metadata, tags, comments, and spatial reference
 * metadata, performing some mapping to clarify when metadata applies only to
 * the original/published version and when the new live copy 'inherits' the
 * metadata. This can be adjusted using the black and gray lists of terms and/or
 * providing custom code to map metadata to SEAD 2.0 conventions.
 * 
 */

public class SEADUploader {

	public static final String FRBR_EO = "http://purl.org/vocab/frbr/core#embodimentOf";
	private static final String DCTERMS_HAS_PART = "http://purl.org/dc/terms/hasPart";

	private static long max = 9223372036854775807l;
	private static boolean merge = false;
	private static boolean verify = false;
	private static boolean importRO = false;
	private static boolean sead2space = false;
	private static String sead2datasetId = null;

	private static long globalFileCount = 0l;
	private static long totalBytes = 0L;

	protected static boolean listonly = false;

	protected static Set<String> excluded = new HashSet<String>();;

	private static String server = null;

	static PrintWriter pw = null;

	static HttpClientContext localContext = null;

	private static ResourceFactory rf = null;

	private static HashMap<String, String> roDataIdToNewId = new HashMap<String, String>();
	private static HashMap<String, String> roCollIdToNewId = new HashMap<String, String>();
	private static HashMap<String, String> roFolderProxy = new HashMap<String, String>();

	public static void main(String args[]) throws Exception {

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
			} else if (arg.equals("-verify")) {
				verify = true;
				println("Verify Mode: Will verify hash values for file comparisons");
			} else if (arg.equals("-ro")) {
				importRO = true;
				println("RO Mode: Intpreting last commandline arg as the URL for an OREMap");
			} else if (arg.equalsIgnoreCase("-sead2")) {
				sead2space = true;
				println("SEAD2 Space: Uploading to a SEAD2 Instance");
			} else if (arg.startsWith("-limit")) {
				max = Long.parseLong(arg.substring(6));
				println("Max ingest file count: " + max);
			} else if (arg.startsWith("-ex")) {

				excluded.add(arg.substring(3));
				println("Exluding pattern: " + arg.substring(3));
			}

		}

		// go through arguments
		for (String arg : args) {
			if (!((arg.equalsIgnoreCase("-listonly"))
					|| (arg.equalsIgnoreCase("-merge"))
					|| (arg.equalsIgnoreCase("-verify"))
					|| (arg.equalsIgnoreCase("-ro"))
					|| (arg.equalsIgnoreCase("-sead2"))
					|| (arg.startsWith("-limit")) || (arg.startsWith("-ex")))) {
				// First non-flag arg is the server URL
				if (server == null) {
					println("Server: " + arg);
					server = arg;
					if (sead2space) {
						localContext = SEADAuthenticator.UPAuthenticate(server);
					} else {
						localContext = SEADAuthenticator.authenticate(server);
					}
					if (localContext == null) {
						println("Authentication failure - exiting.");
						System.exit(0);
					}
				} else {
					if (importRO) {
						// Should be a URL
						try {
							URL oremapURL = new URL(arg);
							importRO(oremapURL);
						} catch (MalformedURLException mfue) {
							println("Unable to interpret: " + arg
									+ " as a URL to an OREMap.");
							System.exit(0);
						}
					} else {
						// It's a local path to a file or dir
						Resource file = new FileResource(arg);
						if (!excluded(file.getName())) {
							String tagId = null;
							if (merge) {
								tagId = itemExists("/", file);
							}

							if (file.isDirectory()) {

								String newUri = uploadCollection(file, "",
										null, tagId);

								if (newUri != null) {
									println("              " + file.getPath()
											+ " CREATED as: " + newUri);
								} else if ((tagId == null) && !listonly) {
									println("Error processing: "
											+ file.getPath());
								}

							} else {

								if (globalFileCount < max) {
									String newUri = uploadDataset(file, null,
											tagId);
									if (newUri != null) {
										println("              UPLOADED as: "
												+ newUri);
									} else if ((tagId == null) && !listonly) {
										println("Error processing: "
												+ file.getPath());
									}
								}
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

	@SuppressWarnings("unchecked")
	private static void importRO(URL oremapURL) {
		rf = new ResourceFactory(oremapURL);
		PublishedResource dataset = rf.getParentResource();
		String tagId = null;
		// remove the name and final / char from the absolute path
		String rootPath = dataset.getAbsolutePath().substring(
				0,
				dataset.getAbsolutePath().length() - dataset.getName().length()
						- 1);

		if (merge) {

			tagId = itemExists(rootPath + "/", dataset);
		}

		String newUri = uploadCollection(dataset, rootPath, null, tagId);

		if (newUri != null) {
			println("              " + dataset.getPath() + " CREATED as: "
					+ newUri);
		} else if ((tagId == null) && !listonly) {
			println("Error processing: " + dataset.getPath());
		}

		JSONObject rels = new JSONObject(
				PublishedResource.getAllRelationships());
		/*
		 * println("Rels to translate"); println(rels.toString(2));
		 * println(roCollIdToNewId.toString());
		 * println(roDataIdToNewId.toString());
		 */
		if (!listonly) {
			for (String relSubject : (Set<String>) rels.keySet()) {
				JSONObject relationships = rels.getJSONObject(relSubject);
				String newSubject = null;
				String type = "collections";
				newSubject = roCollIdToNewId
						.get(findGeneralizationOf(relSubject));

				if (newSubject == null) {
					newSubject = roDataIdToNewId
							.get(findGeneralizationOf(relSubject));
					type = "datasets";
				} else if (roFolderProxy.containsKey(newSubject)) {
					newSubject = roFolderProxy.get(newSubject);
					type = "datasets";
				}
				if ((newSubject != null) && (relationships.length() != 0)) {

					if (sead2space) {

						JSONObject content = new JSONObject();

						JSONObject agent = new JSONObject();
						JSONObject me = get2me();
						agent.put("name", me.getString("fullName"));
						agent.put("@type", "cat:user");
						agent.put("user_id",
								server + "/api/users/" + me.getString("id"));

						for (String predLabel : (Set<String>) relationships
								.keySet()) {
							String newObject = roCollIdToNewId
									.get(relationships.getString(predLabel));
							if (newObject != null) {
								if (newObject.equals(sead2datasetId)) {
									newObject = server + "/datasets/"
											+ newObject;
								} else {
									newObject = server + "/datasets/"
											+ sead2datasetId + "#folderId"
											+ newObject;
								}
							} else {
								newObject = roDataIdToNewId.get(relationships
										.getString(predLabel));
								if (newObject != null) {
									newObject = server + "/files/" + newObject;
								}
							}

							content.put(predLabel, newObject);

						}
						JSONObject context = new JSONObject();

						// Create flattened context for 2.0
						for (String key : ((Set<String>) content.keySet())) {
							String pred = rf.getURIForContextEntry(key);
							if (pred != null) {
								context.put(key, pred);
							}
						}
						if (type.equals("datasets")
								|| newSubject.equals(sead2datasetId)) {
							CloseableHttpClient httpclient = HttpClients
									.createDefault();

							String uri = server
									+ "/api/"
									+ (type.equals("datasets") ? "files/"
											: "datasets/") + newSubject
									+ "/metadata.jsonld";
							postMetadata(httpclient, uri, newSubject, content,
									context, agent);
						} else {
							println("Folder: Would've written: " + newSubject
									+ ": " + content.toString());

						}

					} else {
						CloseableHttpClient httpclient = HttpClients
								.createDefault();
						try {
							// Now post data
							String urlString = server + "/resteasy/" + type
									+ "/"
									+ URLEncoder.encode(newSubject, "UTF-8")
									+ "/metadata";
							HttpPost httppost = new HttpPost(urlString);

							MultipartEntityBuilder meb = MultipartEntityBuilder
									.create();
							for (String predLabel : (Set<String>) relationships
									.keySet()) {
								String newObject = roCollIdToNewId
										.get(relationships.getString(predLabel));
								if (newObject == null) {
									newObject = roDataIdToNewId
											.get(relationships
													.getString(predLabel));
								}
								addURIMetadata(meb,
										rf.getURIForContextEntry(predLabel),
										newObject);
							}
							HttpEntity reqEntity = meb.build();

							httppost.setEntity(reqEntity);

							CloseableHttpResponse response = httpclient
									.execute(httppost, localContext);
							try {
								if (response.getStatusLine().getStatusCode() == 200) {
									EntityUtils.consumeQuietly(response
											.getEntity());
								} else {
									println("Error response while adding relationships for: "
											+ newSubject
											+ " : "
											+ response.getStatusLine()
													.getReasonPhrase());
								}
							} finally {
								response.close();
							}
						} catch (IOException e) {
							println("Error processing relationships for "
									+ newSubject + " : " + e.getMessage());
						} finally {
							try {
								httpclient.close();
							} catch (IOException e) {
								println("Couldn't close connection after adding relationships for: "
										+ newSubject + " : " + e.getMessage());
							}
						}
					}
				}
			}
		}
	}

	private static boolean excluded(String name) {

		for (String s : excluded) {
			if (name.matches(s)) {
				println("Excluding: " + name);
				return true;
			}
		}
		return false;
	}

	protected static String uploadCollection(Resource dir, String path,
			String parentId, String collectionId) {

		Set<String> existingDatasetChildren = null;
		Set<String> existingCollectionChildren = null;

		Set<String> childDatasetUris = new HashSet<String>();
		Set<String> childCollectionUris = new HashSet<String>();

		println("\nPROCESSING(C): " + dir.getPath());
		if (collectionId != null) {
			println("              Found as: " + collectionId);
		} else {
			println("              Does not yet exist on server.");
		}
		if (path != null) {
			path += "/" + dir.getName();
		} else {
			path = "/" + dir.getName();
		}

		boolean created = false;
		if (sead2space) {
			// SEAD2 - create the dataset or folder first before processing
			// children
			if (!listonly) {
				if (collectionId == null) {
					if (parentId == null) {
						collectionId = create2Dataset(dir, path);
						sead2datasetId = collectionId;
					} else {
						collectionId = create2Folder(parentId, sead2datasetId,
								path, dir);
					}
					created = true;
				} else {
					// We already have the dataset uploaded so record it's id
					if (parentId == null) {
						sead2datasetId = collectionId;
					}
				}
			} else {
				if (collectionId != null && parentId == null) {
					sead2datasetId = collectionId;
				}
			}
			if (sead2datasetId != null) {
				println("Dataset ID: " + sead2datasetId);
			}
		}
		try {
			for (Resource file : dir.listResources()) {
				if (excluded(file.getName())) {
					continue;
				}
				String existingUri = null;
				String newUri = null;

				if (merge) {
					existingUri = itemExists(path + "/", file);

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
							println("FINALIZING(C): " + file.getPath()
									+ " CREATED as: " + newUri);
						}
					} else {

						// fileStats[1] += file.length();
						newUri = uploadDataset(file, path, existingUri);

						// At this point, For 1.x, dataset is added but not
						// linked to parent, for 2.0 file is in dataset, but not
						// in a subfolder
						if (sead2space) {
							if (existingUri == null) { // file didn't exist
														// before
								if ((collectionId != null)
										&& (!sead2datasetId
												.equals(collectionId))) { // it's
																			// in
																			// a
																			// folder
																			// and
																			// not
																			// the
																			// dataset
									if (newUri != null) { // and it was just
															// created
										moveFileToFolder(newUri, collectionId,
												file);
									}
								}
							} else { // the file existed
								// FixMe - need to check if it is already in the
								// folder or not...
								if (!sead2datasetId.equals(existingUri)) {

									CloseableHttpClient httpclient = HttpClients
											.createDefault();

									HttpGet httpget = new HttpGet(server
											+ "/api/datasets/" + sead2datasetId
											+ "/listFiles");

									CloseableHttpResponse getResponse = httpclient
											.execute(httpget, localContext);
									try {
										if (getResponse.getStatusLine()
												.getStatusCode() == 200) {
											JSONArray files = new JSONArray(
													EntityUtils
															.toString(getResponse
																	.getEntity()));
											for (int i = 0; i < files.length(); i++) {
												if (files.getJSONObject(i)
														.getString("id")
														.equals(existingUri)) {
													// File is in dataset
													// directly, not in a
													// folder, so move it if
													// needed
													if ((collectionId != null)
															&& (!sead2datasetId
																	.equals(collectionId))) { // it's

														moveFileToFolder(
																existingUri,
																collectionId,
																file);
													}
													break;
												}
											}
										} else {
											println("Error response when listing files "
													+ dir.getAbsolutePath()
													+ " : "
													+ getResponse
															.getStatusLine()
															.getReasonPhrase());
											println("Details: "
													+ EntityUtils
															.toString(getResponse
																	.getEntity()));

										}
									} finally {
										EntityUtils.consumeQuietly(getResponse
												.getEntity());
										getResponse.close();
									}
								}
							}
						}
						if ((existingUri == null) && (newUri != null)) {
							globalFileCount++;
							totalBytes += file.length();
							println("               UPLOADED as: " + newUri);
							println("CURRENT TOTAL: " + globalFileCount
									+ " files :" + totalBytes + " bytes");
						}
					}
				}
				if (!sead2space) {
					if (!listonly) {
						// If we're trying to write (w or w/o merge), a null
						// means
						// an error
						// If listOnly, we don't need to do anything with
						// childUris
						// and don't care about returned value (null if !merge,
						// id
						// or null if merge)

						if (existingUri == null) {
							if (newUri != null) { // Child didn't exist, now it
													// does
								if (file.isDirectory()) {
									childCollectionUris.add(newUri);
								} else {
									childDatasetUris.add(newUri);
								}
							} else { // Child didn't exist and still doesn't
								if (globalFileCount < max) { // and it should
									throw (new IOException(
											"Did not process item correctly: "
													+ file.getAbsolutePath()));
								} else {
									println("Reached file limit before processing: "
											+ file);
								}
							}
						} else {
							// Need to check existing children (if collection
							// already exists) and add any missing
							// colls and datasets that aren't new (those were
							// already added but were not children of the
							// current
							// collection )
							if (existingDatasetChildren == null) {
								if (collectionId != null) {
									existingDatasetChildren = getDatasetChildren(collectionId);
								} else {
									// collection doesn't exist yet - get an
									// empty
									// list
									existingDatasetChildren = new HashSet<String>();
								}
							}
							if (existingCollectionChildren == null) {
								if (collectionId != null) {
									existingCollectionChildren = getCollectionChildren(collectionId);
								} else {
									// collection doesn't exist yet - get an
									// empty
									// list
									existingCollectionChildren = new HashSet<String>();
								}
							}

							if (file.isDirectory()) {
								if (!existingCollectionChildren
										.contains(existingUri)) {
									childCollectionUris.add(existingUri);
								}
							} else {
								if (!existingDatasetChildren
										.contains(existingUri)) {
									childDatasetUris.add(existingUri);
								}
							}

						}
					}
				}
			}
			if (!listonly) { // We're potentially making changes
				if (collectionId == null) { // a collection for path not on
											// server or we
											// don't care - we have to write
											// the
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

						// Add metadata for published resources

						String tagValues = addResourceMetadata(meb, dir);
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
									created = true;
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

						// Add tags
						if (tagValues != null) {
							addTags(httpclient, dir, collectionId, tagValues);
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
				} else if (!sead2space) {
					// Just need to add any new children - addsubCollcall
					CloseableHttpClient httpclient = HttpClients
							.createDefault();
					if (!childDatasetUris.isEmpty()
							|| !childCollectionUris.isEmpty()) {
						println("Adding child relationships to existing collection:");
					}
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
							collectionId = null;
							httpclient.close();
						} catch (IOException e) {
							println("Couldn't close connection for file: "
									+ dir.getAbsolutePath() + " : "
									+ e.getMessage());
						}
					}

				}
				if ((collectionId != null) && importRO) {
					String id = findGeneralizationOf(((PublishedResource) dir)
							.getIdentifier());

					roCollIdToNewId.put(id, collectionId);
				}
				if (!created) { // item existed before
					// don't report collection as new
					collectionId = null;
				}
			} else {
				collectionId = null; // listonly - report no changes
			}

		} catch (IOException io) {
			println("error: " + io.getMessage());// One or more files not
													// uploaded correctly - stop
													// processing...
		}
		return collectionId;
	}

	private static void moveFileToFolder(String newUri, String parentId,
			Resource file) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {

			HttpPost httppost = new HttpPost(server + "/api/datasets/"
					+ sead2datasetId + "/moveFile/" + parentId + "/" + newUri);

			StringEntity se = new StringEntity("{}");
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
					"application/json"));
			httppost.setEntity(se);

			CloseableHttpResponse response = httpclient.execute(httppost,
					localContext);
			HttpEntity resEntity = null;
			try {
				if (response.getStatusLine().getStatusCode() == 200) {
					EntityUtils.consume(response.getEntity());
				} else {
					println("Error response when processing "
							+ file.getAbsolutePath() + " : "
							+ response.getStatusLine().getReasonPhrase());
					println("Details: "
							+ EntityUtils.toString(response.getEntity()));
				}
			} finally {
				EntityUtils.consumeQuietly(resEntity);
				response.close();
			}

			// FixMe Add tags
			/*
			 * if (tagValues != null) { addTags(httpclient, dir, collectionId,
			 * tagValues); }
			 */
		} catch (IOException e) {
			println("Error processing " + file.getAbsolutePath() + " : "
					+ e.getMessage());
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				println("Couldn't close connection for file: "
						+ file.getAbsolutePath() + " : " + e.getMessage());
			}
		}
	}

	private static String create2Folder(String parentId, String sead2datasetId,
			String path, Resource dir) {
		String collectionId = null;
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {

			HttpPost httppost = new HttpPost(server + "/api/datasets/"
					+ sead2datasetId + "/newFolder");

			JSONObject jo = new JSONObject();
			jo.put("name", dir.getName());
			jo.put("parentId", parentId);
			jo.put("parentType", ((parentId == sead2datasetId) ? "dataset"
					: "folder"));

			StringEntity se = new StringEntity(jo.toString());
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
					"application/json"));
			httppost.setEntity(se);

			CloseableHttpResponse response = httpclient.execute(httppost,
					localContext);
			HttpEntity resEntity = null;
			try {
				if (response.getStatusLine().getStatusCode() == 200) {
					EntityUtils.consume(response.getEntity());
					// Now query to get the new folder's id
					// path should be of the form
					// "/<datasetname>/[<parentfolder(s)>/]<thisfolder>" for
					// file uploads and
					// "<ro_id>/data/<datasetname>/[<parentfolder(s)>/]<thisfolder>"
					// for imported ROs
					// and we need to strip to get only the folder path part
					String folderPath = path;
					if (importRO) {
						folderPath = folderPath.substring(folderPath.substring(
								1).indexOf("/") + 1);
						folderPath = folderPath.substring(folderPath.substring(
								1).indexOf("/") + 1);
					}
					folderPath = folderPath.substring(folderPath.substring(1)
							.indexOf("/") + 1);

					HttpGet httpget = new HttpGet(server + "/api/datasets/"
							+ sead2datasetId + "/folders");

					CloseableHttpResponse getResponse = httpclient.execute(
							httpget, localContext);
					try {
						if (getResponse.getStatusLine().getStatusCode() == 200) {
							JSONArray folders = new JSONArray(
									EntityUtils.toString(getResponse
											.getEntity()));
							for (int i = 0; i < folders.length(); i++) {
								if (folders.getJSONObject(i).getString("name")
										.equals(folderPath)) {
									collectionId = folders.getJSONObject(i)
											.getString("id");
									break;
								}
							}
						} else {
							println("Error response when processing "
									+ dir.getAbsolutePath()
									+ " : "
									+ getResponse.getStatusLine()
											.getReasonPhrase());
							println("Details: "
									+ EntityUtils.toString(getResponse
											.getEntity()));
						}
					} finally {
						EntityUtils.consumeQuietly(getResponse.getEntity());
						getResponse.close();
					}
				} else {
					println("Error response when processing "
							+ dir.getAbsolutePath() + " : "
							+ response.getStatusLine().getReasonPhrase());
					println("Details: "
							+ EntityUtils.toString(response.getEntity()));
				}
			} finally {
				EntityUtils.consumeQuietly(resEntity);
				response.close();
			}

			// Add metadata for imported folders
			// FixMe - Add Metadata to folder directly
			// Assume we only write a metdata file if collection is newly
			// created and we're importing
			if (importRO && collectionId != null) {
				Resource mdFile = new PublishedFolderProxyResource(
						(PublishedResource) dir, collectionId);
				String mdId = uploadFileToSead2Dataset(mdFile, path + "/"
						+ mdFile.getName());
				// By default, we are in a folder and need to move the file
				// (sead2datasetId != collectionId))
				if (mdId != null) { // and it was just
									// created
					moveFileToFolder(mdId, collectionId, mdFile);
					roFolderProxy.put(collectionId, mdId);
				} else {
					println("Unable to write metadata file for folder: "
							+ collectionId);
				}
			}
		} catch (IOException e) {
			println("Error processing " + dir.getAbsolutePath() + " : "
					+ e.getMessage());
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				println("Couldn't close connection for file: "
						+ dir.getAbsolutePath() + " : " + e.getMessage());
			}
		}
		return collectionId;

	}

	private static String create2Dataset(Resource dir, String path) {
		String datasetId = null;
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {

			HttpPost httppost = new HttpPost(server
					+ "/api/datasets/createempty");
			JSONObject jo = new JSONObject();
			jo.put("name", dir.getName());
			if (importRO) {
				String abs = ((PublishedResource) dir).getAndRemoveAbstract();
				if (abs != null) {
					jo.put("description", abs);
				}
			}

			StringEntity se = new StringEntity(jo.toString());
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
					"application/json"));
			httppost.setEntity(se);

			CloseableHttpResponse response = httpclient.execute(httppost,
					localContext);
			HttpEntity resEntity = null;
			try {
				resEntity = response.getEntity();
				if (response.getStatusLine().getStatusCode() == 200) {
					if (resEntity != null) {
						datasetId = new JSONObject(
								EntityUtils.toString(resEntity))
								.getString("id");
					}
				} else {
					println("Error response when processing "
							+ dir.getAbsolutePath() + " : "
							+ response.getStatusLine().getReasonPhrase());
					println("Details: " + EntityUtils.toString(resEntity));
				}
			} finally {
				EntityUtils.consumeQuietly(resEntity);
				response.close();
			}
			if (datasetId != null) {

				// FixMe - add Metadata
				/*
				 * addLiteralMetadata(meb, FRBR_EO, path);
				 * 
				 * // Add metadata for published resources
				 */

				JSONObject content = new JSONObject();
				JSONObject context = new JSONObject();
				JSONObject agent = new JSONObject();
				content.put("Upload Path", path);
				List<String> comments = new ArrayList<String>();
				String tagValues = add2ResourceMetadata(content, context, agent, comments, path, dir);
				
				postMetadata(httpclient, server + "/api/datasets/" + datasetId
						+ "/metadata.jsonld", dir.getAbsolutePath(), content,
						context, agent);

				// FixMe Add tags
				if (tagValues != null) {
					HttpPost tagPost = new HttpPost(server + "/api/datasets/"
							+ datasetId + "/tags");
					JSONObject tags = new JSONObject();

					String[] tagArray = tagValues.split(",");
					JSONArray tagList = new JSONArray(tagArray);
					tags.put("tags", tagList);

					StringEntity se3 = new StringEntity(tags.toString());
					se3.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
							"application/json"));
					tagPost.setEntity(se3);

					CloseableHttpResponse tagResponse = httpclient.execute(
							tagPost, localContext);
					resEntity = null;
					try {
						resEntity = tagResponse.getEntity();
						if (tagResponse.getStatusLine().getStatusCode() != 200) {
							println("Error response when processing "
									+ dir.getAbsolutePath()
									+ " : "
									+ tagResponse.getStatusLine()
											.getReasonPhrase());
							println("Details: "
									+ EntityUtils.toString(resEntity));
						}
					} finally {
						EntityUtils.consumeQuietly(resEntity);
						tagResponse.close();
					}
				}
				if (comments.size() > 0) {
					Collections.sort(comments);
					for (String text : comments.toArray(new String[comments
							.size()])) {
						HttpPost commentPost = new HttpPost(server
								+ "/api/datasets/" + datasetId + "/comment");

						JSONObject comment = new JSONObject();
						comment.put("text", text);

						StringEntity se3 = new StringEntity(comment.toString());
						se3.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
								"application/json"));
						commentPost.setEntity(se3);

						CloseableHttpResponse commentResponse = httpclient
								.execute(commentPost, localContext);
						resEntity = null;
						try {
							resEntity = commentResponse.getEntity();
							if (commentResponse.getStatusLine().getStatusCode() != 200) {
								println("Error response when processing "
										+ dir.getAbsolutePath()
										+ " : "
										+ commentResponse.getStatusLine()
												.getReasonPhrase());
								println("Details: "
										+ EntityUtils.toString(resEntity));
							}
						} finally {
							EntityUtils.consumeQuietly(resEntity);
							commentResponse.close();
						}
					}
				}
			}
		} catch (IOException e) {
			println("Error processing " + dir.getAbsolutePath() + " : "
					+ e.getMessage());
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				println("Couldn't close connection for file: "
						+ dir.getAbsolutePath() + " : " + e.getMessage());
			}
		}
		return datasetId;
	}

	@SuppressWarnings("unchecked")
	private static String add2ResourceMetadata(JSONObject content,
			JSONObject context, JSONObject agent, List<String> comments,
			String path, Resource item) {
		Object tags = null;

		JSONObject metadata = item.getMetadata();  //Empty for file resources
		if (metadata.has("Metadata on Original")) {
			JSONObject original = metadata
					.getJSONObject("Metadata on Original");
			// Gray list metadata should be used (and removed) from
			// this
			// field or passed in as is

			if (original.has("Keyword")) {
				tags = original.get("Keyword");
				original.remove("Keyword");
			}
			if (original.has("GeoPoint")) {
				JSONObject point = original.getJSONObject("GeoPoint");
				metadata.put("Geolocation", "Lat/Long from SEAD 1.5 GeoPoint");
				metadata.put("Latitude", point.getString("lat"));
				metadata.put("Longitude", point.getString("long"));
				original.remove("GeoPoint");

			}
			if (original.has("Comment")) {
				Object comObject = original.get("Comment");
				if (comObject instanceof JSONArray) {
					for (int i = 0; i < ((JSONArray) comObject).length(); i++) {
						JSONObject comment = ((JSONArray) comObject)
								.getJSONObject(i);
						comments.add(getComment(comment));
					}
				} else {
					comments.add(getComment(((JSONObject) comObject)));
				}
				original.remove("Comment");
			}
		}
		// Convert all vals to Strings
		for (String key : (Set<String>) metadata.keySet()) {
			String newKey = key;
			if (ResourceFactory.graySwaps.containsKey(key)) {
				newKey = ResourceFactory.graySwaps.get(key);
			}
			if (metadata.get(key) instanceof JSONArray) {
				// split values and handle them separately
				JSONArray valArray = (JSONArray) metadata.get(key);
				JSONArray newVals = new JSONArray();
				for (int i = 0; i < valArray.length(); i++) {
					String val = valArray.get(i).toString();
					newVals.put(val);
				}
				content.put(newKey, newVals);
			} else {
				content.put(newKey, metadata.get(key).toString());
			}
		}
		// create tag(s) string
		String tagValues = null;
		if (tags != null) {
			if (tags instanceof JSONArray) {
				tagValues = "";
				JSONArray valArray = (JSONArray) tags;
				for (int i = 0; i < valArray.length(); i++) {
					tagValues = tagValues + valArray.get(i).toString();
					if (valArray.length() > 1 && i != valArray.length() - 1) {
						tagValues = tagValues + ",";
					}
				}
			} else {
				tagValues = ((String) tags);
			}
		}
		content.put("Upload Path", path);

		// Flatten context for 2.0

		for (String key : ((Set<String>) content.keySet())) {
			if(rf!=null) { //importRO == true
			String pred = rf.getURIForContextEntry(key);
			if (pred != null) {
				context.put(key, pred);
			}
			} else {
				if(key.equals("Upload Path")) {
					context.put(key, SEADUploader.FRBR_EO);
				} else { //shouldn't happen 
					println("Unrecognized Metadata Entry: " + key);
				}
			}
		}
		JSONObject me = get2me();
		agent.put("name", me.getString("fullName"));
		agent.put("@type", "cat:user");
		agent.put("user_id", server + "/api/users/" + me.getString("id"));

		return tagValues;
	}

	private static String getComment(JSONObject comment) {
		StringBuilder sb = new StringBuilder();
		sb.append("Imported Comment: ");
		sb.append(comment.getString("comment_date"));
		sb.append(", Author: ");
		String comAuth = comment.getString("comment_author");
		sb.append(comAuth.substring(comAuth.lastIndexOf("/") + 1));
		sb.append(": ");
		sb.append(comment.getString("comment_body"));
		return sb.toString();
	}

	static JSONObject me = null;

	private static JSONObject get2me() {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		if (me == null) {
			try {
				String serviceUrl = server + "/api/me";

				HttpGet httpget = new HttpGet(serviceUrl);
				CloseableHttpResponse response = httpclient.execute(httpget,
						localContext);
				try {
					if (response.getStatusLine().getStatusCode() == 200) {
						HttpEntity resEntity = response.getEntity();
						if (resEntity != null) {
							me = new JSONObject(EntityUtils.toString(resEntity));
						}
					} else {
						println("Error response when retrieving user details: "
								+ response.getStatusLine().getReasonPhrase());

					}
				} finally {
					response.close();
				}
			} catch (IOException e) {
				println("Error processing get user request: " + e.getMessage());
			} finally {
				try {
					httpclient.close();
				} catch (IOException e) {
					println("Couldn't close httpclient: " + e.getMessage());
				}
			}
			// me.put("fullName", "SEAD 1.5 Importer");
		}
		return me;
	}

	private static Set<String> getDatasetChildren(String collectionId) {
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
					println("Error response when checking for dataset children of "
							+ collectionId
							+ " : "
							+ response.getStatusLine().getReasonPhrase());

				}
			} finally {
				response.close();
			}
		} catch (IOException e) {
			println("Error processing dataset children check on "
					+ collectionId + " : " + e.getMessage());
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				println("Couldn't close httpclient: " + e.getMessage());
			}
		}
		return (tagIds);
	}

	private static Set<String> getCollectionChildren(String collectionId) {
		Set<String> tagIds = new HashSet<String>();
		CloseableHttpClient httpclient = HttpClients.createDefault();

		try {
			String serviceUrl = server + "/resteasy/collections/"
					+ URLEncoder.encode(collectionId, "UTF-8") + "/"
					+ "collections";

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
					println("Error response when checking for collection children of "
							+ collectionId
							+ " : "
							+ response.getStatusLine().getReasonPhrase());

				}
			} finally {
				response.close();
			}
		} catch (IOException e) {
			println("Error processing collection children check on "
					+ collectionId + " : " + e.getMessage());
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
				e.printStackTrace();
			}
		}
	}

	public static String uploadDataset(Resource file, String path, String dataId) {
		long startTime = System.currentTimeMillis();
		println("\nPROCESSING(D): " + file.getPath());
		if (path != null) {
			path += "/" + file.getName();
		} else {
			path = "/" + file.getName();
		}

		if (dataId != null) {
			println("              Found as: " + dataId);
		} else {
			if (verify) {
				if (hashIssues.containsKey(path)) {
					println("               " + hashIssues.get(path));
				}
			}
			println("               Does not yet exist on server.");
		}

		boolean created = false;
		if (!listonly) {
			if (dataId == null) { // doesn't exist or we don't care (!merge)
				if (sead2space) {
					dataId = uploadFileToSead2Dataset(file, path);
					created = true;
				} else {
					CloseableHttpClient httpclient = HttpClients
							.createDefault();

					try {
						// To support long uploads, request a key to allow the
						// upload to complete even if the session has timed out
						String uploadKey = null;
						println("Getting Key");
						HttpGet httpget = new HttpGet(server
								+ "/resteasy/datasets/uploadKey");
						CloseableHttpResponse response = httpclient.execute(
								httpget, localContext);
						println("Getting Key Response");

						try {
							if (response.getStatusLine().getStatusCode() == 200) {
								HttpEntity resEntity = response.getEntity();
								if (resEntity != null) {
									String json = EntityUtils
											.toString(resEntity);
									Map<String, Object> mapObject = new ObjectMapper()
											.readValue(
													json,
													new TypeReference<Map<String, Object>>() {
													});
									uploadKey = (String) mapObject
											.get("uploadkey");
									println("Got upload Key for "
											+ file.getAbsolutePath() + " : "
											+ uploadKey);

								}
							} else {
								println("Unable to get upload Key for "
										+ file.getAbsolutePath()
										+ " : "
										+ response.getStatusLine()
												.getReasonPhrase());

							}
						} finally {
							response.close();
						}
						// Now post data
						String urlString = server + "/resteasy/datasets";
						if (uploadKey != null) {
							urlString = urlString + "?uploadkey=" + uploadKey;
						}
						HttpPost httppost = new HttpPost(urlString);

						ContentBody bin = file.getContentBody();

						MultipartEntityBuilder meb = MultipartEntityBuilder
								.create();
						meb.addPart("datablob", bin);

						addLiteralMetadata(meb, FRBR_EO, path);

						String tagValues = addResourceMetadata(meb, file);

						HttpEntity reqEntity = meb.build();
						httppost.setEntity(reqEntity);

						response = httpclient.execute(httppost, localContext);
						try {
							if (response.getStatusLine().getStatusCode() == 200) {
								HttpEntity resEntity = response.getEntity();
								if (resEntity != null) {
									dataId = EntityUtils.toString(resEntity);
									created = true;
								}
							} else {
								println("Error response when processing "
										+ file.getAbsolutePath()
										+ " : "
										+ response.getStatusLine()
												.getReasonPhrase());

							}
						} finally {
							EntityUtils.consumeQuietly(response.getEntity());
						}

						// Now post tags
						if (tagValues != null) {
							addTags(httpclient, file, dataId, tagValues);
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
				}
			} else {

				// FixMe - if dataId exists, we could check sha1 and upload a
				// version if file changes

			}
		} else {
			// Increment count if we would have uploaded (dataId==null)
			if (dataId == null) {
				globalFileCount++;
			}
		}
		if ((dataId != null) && importRO) {
			String id = findGeneralizationOf(((PublishedResource) file)
					.getIdentifier());
			roDataIdToNewId.put(id, dataId);
		}
		if (!created) { // Don't report as new (existed or listonly mode)
			dataId = null;
		}

		// If this took a while, try to reauthenticate
		// 30 minutes - a session started by google auth is currently good for 1
		// hour with SEAD, but the JSESSION cookie will timeout if no activity
		// for 30 minutes
		// so we check both for session inactivity > 30 min and google token
		// expiration > 60 min
		// these should always catch any potential timeout and reuathenticate
		// Current values give the Uploader a 100 second window for the next
		// upload to start
		if (sead2space) {
			localContext = SEADAuthenticator.UPReAuthenticateIfNeeded(server,
					startTime);
		} else {
			localContext = SEADAuthenticator.reAuthenticateIfNeeded(server,
					startTime);
		}
		if (localContext == null) {
			println("Authentication failure - exiting.");
			System.exit(0);
		}
		return dataId;
	}

	private static String uploadFileToSead2Dataset(Resource file, String path) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String dataId = null;
		try {
			// FixMe: requires update to 2.0 ... To support long uploads,
			// request a key to allow the
			// upload to complete even if the session has timed out

			// Now post data
			String urlString = server + "/api/uploadToDataset/"
					+ sead2datasetId;
			HttpPost httppost = new HttpPost(urlString);

			ContentBody bin = file.getContentBody();

			MultipartEntityBuilder meb = MultipartEntityBuilder.create();
			meb.addPart("files[]", bin);

			// FixMe
			// addLiteralMetadata(meb, FRBR_EO, path);

			// FixMe
			// String tagValues = addResourceMetadata(meb, file);

			HttpEntity reqEntity = meb.build();
			httppost.setEntity(reqEntity);

			CloseableHttpResponse response = httpclient.execute(httppost,
					localContext);
			HttpEntity resEntity = response.getEntity();

			try {
				if (response.getStatusLine().getStatusCode() == 200) {
					if (resEntity != null) {
						dataId = new JSONObject(EntityUtils.toString(resEntity))
								.getString("id");
					}
				} else {
					println("Error response when processing "
							+ file.getAbsolutePath() + " : "
							+ response.getStatusLine().getReasonPhrase());
					println("Details: " + EntityUtils.toString(resEntity));
				}
			} finally {
				EntityUtils.consumeQuietly(response.getEntity());
				response.close();
			}
			if (dataId != null) {

				// FixMe - add Metadata
				/*
				 * addLiteralMetadata(meb, FRBR_EO, path);
				 * 
				 * // Add metadata for published resources
				 * 
				 * String tagValues = addResourceMetadata(meb, dir); HttpEntity
				 * reqEntity = meb.build();
				 */

				JSONObject content = new JSONObject();
				List<String> comments = new ArrayList<String>();
				JSONObject context = new JSONObject();
				JSONObject agent = new JSONObject();
				String tagValues = add2ResourceMetadata(content, context,
						agent, comments, path, file);
				String abs = null;
				if (content.has("Abstract")) {
					abs = content.getString("Abstract").toString();
					content.remove("Abstract");
					context.remove("Abstract");
				}
				postMetadata(httpclient, server + "/api/files/" + dataId
						+ "/metadata.jsonld", file.getAbsolutePath(), content,
						context, agent);

				if (abs != null) {
					HttpPut descPut = new HttpPut(server + "/api/files/"
							+ dataId + "/updateDescription");
					JSONObject desc = new JSONObject();

					desc.put("description", abs);

					StringEntity descSE = new StringEntity(desc.toString());
					descSE.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
							"application/json"));
					descPut.setEntity(descSE);

					CloseableHttpResponse descResponse = httpclient.execute(
							descPut, localContext);
					resEntity = null;
					try {
						resEntity = descResponse.getEntity();
						if (descResponse.getStatusLine().getStatusCode() != 200) {
							println("Error response when processing "
									+ file.getAbsolutePath()
									+ " : "
									+ descResponse.getStatusLine()
											.getReasonPhrase());
							println("Details: "
									+ EntityUtils.toString(resEntity));
						}
					} finally {
						EntityUtils.consumeQuietly(resEntity);
						descResponse.close();
					}
				}
				// FixMe Add tags
				if (tagValues != null) {
					HttpPost tagPost = new HttpPost(server + "/api/files/"
							+ dataId + "/tags");
					JSONObject tags = new JSONObject();

					String[] tagArray = tagValues.split(",");
					JSONArray tagList = new JSONArray(tagArray);
					tags.put("tags", tagList);

					StringEntity se3 = new StringEntity(tags.toString());
					se3.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
							"application/json"));
					tagPost.setEntity(se3);

					CloseableHttpResponse tagResponse = httpclient.execute(
							tagPost, localContext);
					resEntity = null;
					try {
						resEntity = tagResponse.getEntity();
						if (tagResponse.getStatusLine().getStatusCode() != 200) {
							println("Error response when processing "
									+ file.getAbsolutePath()
									+ " : "
									+ tagResponse.getStatusLine()
											.getReasonPhrase());
							println("Details: "
									+ EntityUtils.toString(resEntity));
						}
					} finally {
						EntityUtils.consumeQuietly(resEntity);
						tagResponse.close();
					}

				}
				if (comments.size() > 0) {
					Collections.sort(comments);
					for (String text : comments.toArray(new String[comments
							.size()])) {
						HttpPost commentPost = new HttpPost(server
								+ "/api/files/" + dataId + "/comment");

						JSONObject comment = new JSONObject();
						comment.put("text", text);

						StringEntity se4 = new StringEntity(comment.toString());
						se4.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
								"application/json"));
						commentPost.setEntity(se4);

						CloseableHttpResponse commentResponse = httpclient
								.execute(commentPost, localContext);
						resEntity = null;
						try {
							resEntity = commentResponse.getEntity();
							if (commentResponse.getStatusLine().getStatusCode() != 200) {
								println("Error response when processing "
										+ file.getAbsolutePath()
										+ " : "
										+ commentResponse.getStatusLine()
												.getReasonPhrase());
								println("Details: "
										+ EntityUtils.toString(resEntity));
							}
						} finally {
							EntityUtils.consumeQuietly(resEntity);
							commentResponse.close();
						}
					}
				}

			}

		} catch (IOException e) {
			println("Error processing " + file.getAbsolutePath() + " : "
					+ e.getMessage());
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				println("Couldn't close connection for file: "
						+ file.getAbsolutePath() + " : " + e.getMessage());
			}
		}
		return dataId;
	}

	@SuppressWarnings("unchecked")
	private static void postMetadata(CloseableHttpClient httpclient,
			String uri, String path, JSONObject content, JSONObject context,
			JSONObject agent) {
		Set<String> keys = new HashSet<String>();
		keys.addAll(((Set<String>) content.keySet()));
		if (keys.contains("Geolocation")) {
			keys.remove("Latitude");
			keys.remove("Longitude");
		}

		for (String key : keys) {
			try {

				JSONObject singleContent = new JSONObject().put(key,
						content.get(key));
				JSONObject singleContext = new JSONObject().put(key,
						context.get(key));
				// Geolocation stays together with lat and long to mirror
				// how the Clowder GUI works
				if (key.equals("Geolocation")) {
					singleContent.put("Latitude", content.get("Latitude"));
					singleContent.put("Longitude", content.get("Longitude"));
					singleContext.put("Latitude", context.get("Latitude"));
					singleContext.put("Longitude", context.get("Longitude"));
				}
				// Clowder expects flat "Creator"s - might as well flatten all
				// values...
				if (singleContent.get(key) instanceof JSONArray) {
					for (int i = 0; i < ((JSONArray) singleContent
							.getJSONArray(key)).length(); i++) {
						JSONObject flatContent = new JSONObject();
						flatContent.put(key, ((JSONArray) singleContent
								.getJSONArray(key)).get(i).toString());
						postSingleMetadata(flatContent, singleContext, agent,
								uri, httpclient);
					}
				} else {
					postSingleMetadata(singleContent, singleContext, agent,
							uri, httpclient);
				}

			} catch (IOException e) {
				println("Error processing " + path + " : " + e.getMessage());
				break;
			}
		}

	}

	private static void postSingleMetadata(JSONObject singleContent,
			JSONObject singleContext, JSONObject agent, String uri,
			CloseableHttpClient httpclient) throws IOException {
		HttpEntity resEntity = null;
		try {
			JSONObject meta = new JSONObject();
			meta.put("content", singleContent);
			meta.put("@context", singleContext);
			meta.put("agent", agent);

			StringEntity se2 = new StringEntity(meta.toString());
			se2.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
					"application/json"));

			HttpPost metadataPost = new HttpPost(uri);

			metadataPost.setEntity(se2);

			CloseableHttpResponse mdResponse = httpclient.execute(metadataPost,
					localContext);

			resEntity = mdResponse.getEntity();
			if (mdResponse.getStatusLine().getStatusCode() != 200) {
				println("Error response when processing key="
						+ singleContent.keys().next() + " : "
						+ mdResponse.getStatusLine().getReasonPhrase());
				println("Details: " + EntityUtils.toString(resEntity));
				throw new IOException("Non 200 response");
			}
		} finally {
			EntityUtils.consumeQuietly(resEntity);
		}

	}

	private static void addTags(CloseableHttpClient httpclient,
			Resource resource, String id, String tagValues)
			throws ClientProtocolException, IOException {
		String type = "datasets";
		if (resource.isDirectory()) {
			type = "collections";
		}
		String tagUrlString = server + "/resteasy/" + type + "/" + id + "/tags";
		HttpPost tagpost = new HttpPost(tagUrlString);

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("tags", tagValues));
		tagpost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));

		CloseableHttpResponse response = httpclient.execute(tagpost,
				localContext);
		try {
			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity resEntity = response.getEntity();
				if (resEntity != null) {
					String tagResponse = EntityUtils.toString(resEntity);
					println(tagResponse);
				}
			} else {
				println("Error response when adding tags to  "
						+ resource.getAbsolutePath() + " : "
						+ response.getStatusLine().getReasonPhrase());

			}
		} finally {
			EntityUtils.consumeQuietly(response.getEntity());
		}

	}

	@SuppressWarnings("unchecked")
	private static String addResourceMetadata(MultipartEntityBuilder meb,
			Resource file) {
		Object tags = null;
		JSONObject metadata = file.getMetadata();
		if (metadata.has("Metadata on Original")) {
			JSONObject original = metadata
					.getJSONObject("Metadata on Original");
			// Gray list metadata should be used (and removed) from
			// this
			// filed or passed in as is

			if (original.has("Keyword")) {
				tags = original.get("Keyword");
				original.remove("Keyword");
			}
		}
		for (String key : (Set<String>) metadata.keySet()) {
			if (metadata.get(key) instanceof JSONArray) {
				// split values and handle them separately
				JSONArray valArray = (JSONArray) metadata.get(key);
				for (int i = 0; i < valArray.length(); i++) {
					String val = valArray.get(i).toString();
					addMetadata(meb, key, val);
				}
			} else {
				addMetadata(meb, key, metadata.get(key).toString());
			}
		}
		// create tag(s) string
		String tagValues = null;
		if (tags != null) {
			if (tags instanceof JSONArray) {
				tagValues = "";
				JSONArray valArray = (JSONArray) tags;
				for (int i = 0; i < valArray.length(); i++) {
					tagValues = tagValues + valArray.get(i).toString();
					if (valArray.length() > 1 && i != valArray.length() - 1) {
						tagValues = tagValues + ",";
					}
				}
			} else {
				tagValues = ((String) tags);
			}
		}
		return tagValues;
	}

	private static void addMetadata(MultipartEntityBuilder meb, String key,
			String val) {

		println("Adding metadata:" + key + "/" + rf.getURIForContextEntry(key)
				+ " : " + val);
		System.out.flush();
		boolean isUri = false;
		if (val.startsWith("http") || val.startsWith("tag:")
				|| val.startsWith("urn:")) {
			isUri = true;
		}
		if (isUri) {
			addURIMetadata(meb, rf.getURIForContextEntry(key), val);
		} else {
			addLiteralMetadata(meb, rf.getURIForContextEntry(key), val);
		}

	}

	private static String findGeneralizationOf(String id) {
		// 1.5 appends a /v<N> to create the id of a published entity whose live
		// 'generalization' may be referred to in relationships...
		if (id.matches(".*/v[0-9]*$")) {
			id = id.substring(0, id.lastIndexOf("/v"));
		}
		return id;
	}

	static HashMap<String, String> existingItems = new HashMap<String, String>();;
	static boolean fileMDRetrieved = false;
	static boolean datasetMDRetrieved = false;

	static String itemExists(String path, Resource item) {
		String tagId = null;

		if (sead2space) {
			String relPath = path;
			if (importRO) {
				// remove the '/<ro_id>/data' prefix on imported paths to make
				// it match the file upload paths
				relPath = relPath
						.substring(relPath.substring(1).indexOf("/") + 1);
				relPath = relPath
						.substring(relPath.substring(1).indexOf("/") + 1);
			}
			if (relPath.equals("/")) {
				// It's a dataset
				CloseableHttpClient httpclient = HttpClients.createDefault();
				String sourcepath = path + item.getName();
				if (!datasetMDRetrieved) {
					try {

						String serviceUrl = server + "/api/datasets/";

						HttpGet httpget = new HttpGet(serviceUrl);

						CloseableHttpResponse response = httpclient.execute(
								httpget, localContext);
						JSONArray datasetList = null;
						try {
							if (response.getStatusLine().getStatusCode() == 200) {
								HttpEntity resEntity = response.getEntity();
								if (resEntity != null) {
									datasetList = new JSONArray(
											EntityUtils.toString(resEntity));
								}
							} else {
								println("Error response when checking for existing item at "
										+ sourcepath
										+ " : "
										+ response.getStatusLine()
												.getReasonPhrase());

							}
						} finally {
							response.close();
						}
						if (datasetList != null) {
							for (int i = 0; i < datasetList.length(); i++) {
								String id = datasetList.getJSONObject(i)
										.getString("id");
								serviceUrl = server + "/api/datasets/" + id
										+ "/metadata.jsonld";

								httpget = new HttpGet(serviceUrl);

								response = httpclient.execute(httpget,
										localContext);

								try {
									if (response.getStatusLine()
											.getStatusCode() == 200) {
										HttpEntity resEntity = response
												.getEntity();
										if (resEntity != null) {
											JSONArray mdList = new JSONArray(
													EntityUtils
															.toString(resEntity));
											for (int j = 0; j < mdList.length(); j++) {
												if (mdList
														.getJSONObject(j)
														.getJSONObject(
																"content")
														.has("Upload Path")) {

													existingItems
															.put(mdList
																	.getJSONObject(
																			j)
																	.getJSONObject(
																			"content")
																	.getString(
																			"Upload Path"),
																	id);
													break;
												}
											}
										}
									} else {
										println("Error response when getting metadata for dataset: "
												+ id
												+ " : "
												+ response.getStatusLine()
														.getReasonPhrase());

									}
								} finally {
									response.close();
								}
							}

						}

					} catch (IOException e) {
						println("Error processing check on " + sourcepath
								+ " : " + e.getMessage());
					} finally {
						try {
							datasetMDRetrieved = true;
							httpclient.close();
						} catch (IOException e) {
							println("Couldn't close httpclient: "
									+ e.getMessage());
						}
					}
				}
				if (existingItems.containsKey(sourcepath)) {
					tagId = existingItems.get(sourcepath);
				}

			} else if (item.isDirectory()) {
				/*
				 * /We're looking for a folder Since folders in 2 have no
				 * metadata and can't be moved, we will assume for now that if
				 * the dataset exists and the foldere's relative path in the
				 * dataset matches, we've found the folder.
				 */
				if (sead2datasetId != null) { // Can't be in a dataset if it
												// wasn't found/created already
					CloseableHttpClient httpclient = HttpClients
							.createDefault();
					String sourcepath = relPath + item.getName();
					sourcepath = sourcepath.substring(sourcepath.substring(1)
							.indexOf("/") + 1);

					try {
						String serviceUrl = server + "/api/datasets/"
								+ sead2datasetId + "/folders";

						HttpGet httpget = new HttpGet(serviceUrl);

						CloseableHttpResponse response = httpclient.execute(
								httpget, localContext);
						try {
							if (response.getStatusLine().getStatusCode() == 200) {
								HttpEntity resEntity = response.getEntity();
								if (resEntity != null) {
									JSONArray folders = new JSONArray(
											EntityUtils.toString(resEntity));
									for (int i = 0; i < folders.length(); i++) {
										if (folders.getJSONObject(i)
												.getString("name")
												.equals(sourcepath)) {
											tagId = folders.getJSONObject(i)
													.getString("id");

											break;
										}
									}
								}
							} else {
								println("Error response when checking for existing item at "
										+ sourcepath
										+ " : "
										+ response.getStatusLine()
												.getReasonPhrase());

							}
						} finally {
							response.close();
						}
					} catch (IOException e) {
						println("Error processing check on " + sourcepath
								+ " : " + e.getMessage());
					} finally {
						try {
							httpclient.close();
						} catch (IOException e) {
							println("Couldn't close httpclient: "
									+ e.getMessage());
						}
					}
				}
			} else {
				// A file
				String sourcepath = path + item.getName();

				if (sead2datasetId != null && !fileMDRetrieved) {
					// One-time retrieval of all file id/Upload Path info

					CloseableHttpClient httpclient = HttpClients
							.createDefault();
					try {
						String serviceUrl = server + "/api/datasets/"
								+ sead2datasetId + "/listAllFiles";

						HttpGet httpget = new HttpGet(serviceUrl);

						CloseableHttpResponse response = httpclient.execute(
								httpget, localContext);
						JSONArray fileList = null;
						try {
							if (response.getStatusLine().getStatusCode() == 200) {
								HttpEntity resEntity = response.getEntity();
								if (resEntity != null) {
									fileList = new JSONArray(
											EntityUtils.toString(resEntity));
								}
							} else {
								println("Error response when checking for existing item at "
										+ sourcepath
										+ " : "
										+ response.getStatusLine()
												.getReasonPhrase());

							}
						} finally {
							response.close();
						}
						if (fileList != null) {
							for (int i = 0; i < fileList.length(); i++) {
								String id = fileList.getJSONObject(i)
										.getString("id");
								serviceUrl = server + "/api/files/" + id
										+ "/metadata.jsonld";

								httpget = new HttpGet(serviceUrl);

								response = httpclient.execute(httpget,
										localContext);

								try {
									if (response.getStatusLine()
											.getStatusCode() == 200) {
										HttpEntity resEntity = response
												.getEntity();
										if (resEntity != null) {
											JSONArray mdList = new JSONArray(
													EntityUtils
															.toString(resEntity));
											for (int j = 0; j < mdList.length(); j++) {
												if (mdList
														.getJSONObject(j)
														.getJSONObject(
																"content")
														.has("Upload Path")) {

													existingItems
															.put(mdList
																	.getJSONObject(
																			j)
																	.getJSONObject(
																			"content")
																	.getString(
																			"Upload Path"),
																	id);
													break;
												}
											}
										}
									} else {
										println("Error response when getting metadata for file: "
												+ id
												+ " : "
												+ response.getStatusLine()
														.getReasonPhrase());

									}
								} finally {
									response.close();
								}
							}

						}

					} catch (IOException e) {
						println("Error processing check on " + sourcepath
								+ " : " + e.getMessage());
					} finally {
						try {
							fileMDRetrieved = true;
							httpclient.close();
						} catch (IOException e) {
							println("Couldn't close httpclient: "
									+ e.getMessage());
						}
					}
				}
				if (existingItems.containsKey(sourcepath)) {
					tagId = existingItems.get(sourcepath);
				}
			}
		} else {

			CloseableHttpClient httpclient = HttpClients.createDefault();
			String sourcepath = path + item.getName();

			String servicePrefix = server + "/resteasy/";
			if (item.isDirectory()) {
				servicePrefix = servicePrefix + "collections/metadata/";
			} else {
				servicePrefix = servicePrefix + "datasets/metadata/";
			}
			try {
				String serviceUrl = servicePrefix
						+ URLEncoder.encode(FRBR_EO, "UTF-8") + "/"
						+ "literal/" + URLEncoder.encode(sourcepath, "UTF-8");
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
		}
		if (verify && (tagId != null) && (!item.isDirectory())) {
			tagId = verifyDataByHash(tagId, path, item);
		}
		return (tagId);
	}

	static HashMap<String, String> hashIssues = new HashMap<String, String>();

	private static String verifyDataByHash(String tagId, String path,
			Resource item) {

		String serviceUrl;
		CloseableHttpClient httpclient = HttpClients.createDefault();

		try {
			if (sead2space) {
				// sha1: "http://www.w3.org/2001/04/xmldsig-more#sha1"
				serviceUrl = server + "/api/files/"
						+ URLEncoder.encode(tagId, "UTF-8")
						+ "/metadata.jsonld";
				HttpGet httpget = new HttpGet(serviceUrl);

				CloseableHttpResponse response = httpclient.execute(httpget,
						localContext);
				try {
					if (response.getStatusLine().getStatusCode() == 200) {
						HttpEntity resEntity = response.getEntity();
						if (resEntity != null) {
							String json = EntityUtils.toString(resEntity);
							JSONArray metadata = new JSONArray(json);
							String remoteHash = null;
							for (int i = 0; i < metadata.length(); i++) {
								JSONObject content = metadata.getJSONObject(i)
										.getJSONObject("content");
								if (content != null) {
									if (content.has("sha1")) {
										remoteHash = content.getString("sha1");
										break;
									}
								}
							}
							if (remoteHash != null) {
								if (!remoteHash.equals(item.getSHA1Hash())) {
									hashIssues.put(path + item.getName(),
											"!!!: A different version of this item exists with ID: "
													+ tagId);
									return null;
								} // else it matches!
							} else {
								hashIssues.put(path + item.getName(),
										"Remote Hash does not exist for "
												+ item.getAbsolutePath()
												+ " - cannot verify it");
								return null;
							}
						}
					} else {
						println("Error response while verifying "
								+ item.getAbsolutePath() + " : "
								+ response.getStatusLine().getReasonPhrase());

					}
				} finally {
					response.close();
				}

			} else {
				serviceUrl = server + "/resteasy/datasets/"
						+ URLEncoder.encode(tagId, "UTF-8") + "/biblio";
				HttpGet httpget = new HttpGet(serviceUrl);
				CloseableHttpResponse response = httpclient.execute(httpget,
						localContext);
				try {
					if (response.getStatusLine().getStatusCode() == 200) {
						HttpEntity resEntity = response.getEntity();
						if (resEntity != null) {
							String json = EntityUtils.toString(resEntity);
							JSONObject data = new JSONObject(json);
							if (data.has("SHA1 Hash")) {
								String remoteHash = data.getString("SHA1 Hash");
								if (!remoteHash.equals(item.getSHA1Hash())) {
									println("!!!: A different version of this item exists with ID: "
											+ tagId);
									return null;
								}
							} else {
								println("Remote Hash does not exist for "
										+ item.getAbsolutePath()
										+ " - cannot verify it");
								return null;
							}
							// else check for other algorithms
						}
					} else {
						println("Error response while verifying "
								+ item.getAbsolutePath() + " : "
								+ response.getStatusLine().getReasonPhrase());

					}
				} finally {
					response.close();
				}
			}
		} catch (UnsupportedEncodingException e1) {

			e1.printStackTrace();

		} catch (IOException e) {
			println("Error processing verify on " + item.getAbsolutePath()
					+ " : " + e.getMessage());
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				println("Couldn't close httpclient: " + e.getMessage());
			}
		}
		return tagId;
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

	public static void println(String s) {
		System.out.println(s);
		System.out.flush();
		if (pw != null) {
			pw.println(s);
			pw.flush();
		}
		return;
	}
}