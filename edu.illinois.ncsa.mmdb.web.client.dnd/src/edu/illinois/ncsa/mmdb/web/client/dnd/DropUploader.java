/*******************************************************************************
 *  University of Illinois/NCSA
 *  Open Source License
 *  
 *  Copyright (c) 2010, NCSA.  All rights reserved.
 *  
 *  Developed by:
 *  Cyberenvironments and Technologies (CET)
 *  http://cet.ncsa.illinois.edu/
 *  
 *  National Center for Supercomputing Applications (NCSA)
 *  http://www.ncsa.illinois.edu/
 *  
 *  Permission is hereby granted, free of charge, to any person obtaining
 *  a copy of this software and associated documentation files (the 
 *  "Software"), to deal with the Software without restriction, including
 *  without limitation the rights to use, copy, modify, merge, publish,
 *  distribute, sublicense, and/or sell copies of the Software, and to
 *  permit persons to whom the Software is furnished to do so, subject to
 *  the following conditions:
 *  
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimers.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimers in the
 *    documentation and/or other materials provided with the distribution.
 *  - Neither the names of CET, University of Illinois/NCSA, nor the names
 *    of its contributors may be used to endorse or promote products
 *    derived from this Software without specific prior written permission.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 *  ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 *  CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *******************************************************************************/
package edu.illinois.ncsa.mmdb.web.client.dnd;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.FileNameMap;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JPanel;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.HttpsURL;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.protocol.Protocol;

@SuppressWarnings("serial")
public class DropUploader extends JApplet implements DropTargetListener {
	// upload queue management
	ConcurrentMap<File, Collection> collections = new ConcurrentHashMap<File, Collection>();
	ConcurrentLinkedQueue<FileUpload> uploadQueue = new ConcurrentLinkedQueue<FileUpload>();
	Set<FileUpload> uploading = new ConcurrentSkipListSet<FileUpload>();
	Object queueLock = new Object();
	int index = 0;

	// UI
	private JPanel mainCards;
	JSObject window = null;
	public DropTarget dropTarget;

	public static final String VERSION = "1801";

	// ersatz logging

	void log(String message) {
		System.out.println(message);
	}

	void log(String message, Throwable x) {
		log(message);
		x.printStackTrace(System.out);
	}

	// initial setup

	@SuppressWarnings("deprecation")
	public void init() {
		System.out.println("Drop Uploader version " + VERSION);

		setSize(150, 100);

		ImageIcon dropIcon = getIcon("Load.png", "Upload");
		ImageIcon doneIcon = getIcon("Green_check.png", "Done");
		ImageIcon errorIcon = getIcon("dialog-error.png", "Error");

		// the drop target
		JLabel dropLabel = new JLabel(dropIcon);
		dropLabel.setOpaque(true);

		JLabel doneLabel = new JLabel(doneIcon);
		JLabel errorLabel = new JLabel(errorIcon);

		// now layout the components in a two-card card layout
		mainCards = new JPanel(new CardLayout());
		mainCards.add(dropLabel, "drop");
		mainCards.add(doneLabel, "done");
		mainCards.add(errorLabel, "error");

		// set the background colors of the components
		try {
			String bgColor = getParameter("background");
			log("background color = " + bgColor);
			if (bgColor != null && !bgColor.equals("")) {
				Color c = Color.decode(bgColor);
				mainCards.setBackground(c);
				dropLabel.setBackground(c);
				doneLabel.setBackground(c);
				errorLabel.setBackground(c);
			}
		} catch (Exception x) {
			// fall through
			x.printStackTrace();
		}

		// check auth
		String auth = getParameter("credentials");
		log("credentials = " + auth);

		// accept self-signed certificates
		Protocol.registerProtocol("https", new Protocol("https",
				new EasySSLProtocolSocketFactory(), 443));
		log("Accepting self-signed certificates");

		mainCards.setBounds(0, 0, 150, 100);
		// This class will handle the drop events
		dropTarget = new DropTarget(mainCards, this);

		getContentPane().add(mainCards, BorderLayout.CENTER);

		setVisible(true);

		window = JSObject.getWindow(this); // will this work?
		if (window == null) {
			log("warn: window is null in init()");
		}

		startUploadThread();
		log("started upload thread ...");

		startProgressThread();
		log("started progress thread ...");

		log("successful init");
	}

	ImageIcon getIcon(String name, String label) {
		URL iconUrl = getClass().getResource(
				"/edu/illinois/ncsa/mmdb/web/client/dnd/" + name);
		return new ImageIcon(iconUrl, label);
	}

	// DND implementation

	public void dragEnter(DropTargetDragEvent arg0) {
		// TODO Auto-generated method stub
	}

	public void dragExit(DropTargetEvent arg0) {
		// TODO Auto-generated method stub
	}

	public void dragOver(DropTargetDragEvent arg0) {
		// TODO Auto-generated method stub
	}

	public void dropActionChanged(DropTargetDragEvent arg0) {
		// TODO Auto-generated method stub
	}

	File getFileForFileUri(URI uri) {
		if (uri != null && uri.isAbsolute() && uri.getScheme().equals("file")) {
			return new File(uri.getPath());
		} else {
			return null;
		}
	}

	void addFile(File f, List<File> files) {
		if (f != null && !files.contains(f)) {
			log("dropped file " + f);
			files.add(f);
		}
	}

	public void drop(DropTargetDropEvent dtde) {
		try {
			dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
			// O.k., get the dropped object and try to figure out what it is:
			Transferable tr = dtde.getTransferable();
			List<File> files = new LinkedList<File>();
			for (DataFlavor flavor : tr.getTransferDataFlavors()) {
				log("DataFlavor = " + flavor);
				if (flavor.isFlavorJavaFileListType()) {
					for (File file : (List<File>) tr.getTransferData(flavor)) {
						addFile(file, files);
					}
				} else if (flavor.isFlavorTextType()) {
					BufferedReader br = new BufferedReader(
							flavor.getReaderForText(tr));
					String s = null;
					while ((s = br.readLine()) != null) {
						File file = getFileForFileUri(new URI(s));
						addFile(file, files);
					}
				} else if (flavor.isMimeTypeEqual("application/x-java-url")) {
					File file = getFileForFileUri(URI.create(tr
							.getTransferData(flavor) + ""));
					addFile(file, files);
				} else {
					log("unknown flavor = " + flavor);
				}
			}
			if (files.size() == 0) {
				log("no files dropped! " + dtde);
				dtde.dropComplete(true);
			} else {
				expandDirectories(files); // expand directories
				dtde.dropComplete(true);
			}
		} catch (Exception e) {
			log("Exception during drop", e);
			dtde.dropComplete(false);
		}
	}

	void expandDirectories(List<File> files) {
		for (File f : files) {
			if (f.isDirectory()) {
				log("listing directory " + f);
				for (File kid : f.listFiles()) {
					if (!kid.isDirectory()) {
						log("adding file " + kid + " from directory " + f);
						droppedFile(kid, f);
					}
				}
			} else {
				log("adding file " + f);
				droppedFile(f, null);
			}
		}
	}

	// queue management

	// add dropped file to queue
	void droppedFile(File file, File collectionFolder) {
		FileUpload upload = new FileUpload(file);
		// is there a collection folder associated with this file?
		if (collectionFolder != null) {
			Collection collection = collections.get(collectionFolder);
			if (collection == null) {
				collection = new Collection(collectionFolder);
				collections.put(collectionFolder, collection);
			}
			upload.setCollection(collection);
		}
		synchronized (this) {
			upload.setIndex(index);
			index++;
		}
		uploadQueue.add(upload);
		upload.setState(UploadState.PENDING);
		log("Queued " + upload);
		call("dndAppletFileDropped",
				new Object[] { file.getName(), file.length() + "" });
		// wake up the upload thread
		synchronized (queueLock) {
			queueLock.notifyAll();
		}
	}

	void startUploadThread() {
		Thread uploadThread = new Thread() {
			public void run() {
				while (true) {
					synchronized (queueLock) {
						try {
							queueLock.wait();
							log("Woken up and will consume queue: "
									+ uploadQueue);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					// upload all files in the queue
					while (!uploadQueue.isEmpty()) {
						FileUpload upload = uploadQueue.poll();
						if (upload != null) {
							uploading.add(upload);
							upload(upload);
						}
					}
				}
			}
		};
		uploadThread.start();
	}

	void startProgressThread() {
		Thread progressThread = new Thread() {
			public void run() {
				while (true) {
					try {
						sleep(250);
						for (FileUpload upload : uploading) {
							try {
								onProgress(upload);
							} catch (HttpException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		progressThread.start();
	}

	// applet-to-Javascript communication

	public void call(String functionName, Object[] args) {
		if (window == null) {
			window = JSObject.getWindow(this);
		}
		if (window == null) {
			log("error: unable to call javascript " + functionName
					+ " (JSObject.getWindow(this) returned null)");
			return;
		}
		synchronized (window) {
			try {
				// FIXME trace
				StringWriter trace = new StringWriter();
				trace.append("JavaScript: ").append(functionName);
				for (Object arg : args) {
					trace.append(" ").append(arg.toString());
				}
				System.out.println(trace);
				window.call(functionName, args);
			} catch (JSException x) {
				x.printStackTrace();
			}
		}
	}

	public void poke() {
		repaint();
	}

	// heavy lifting on upload

	String getContextUrl() {
		return getStatusPage().replaceFirst("/[^/]+$", "/");
	}

	String getStatusPage() {
		String statusPage = getParameter("statusPage");
		if (statusPage == null) {
			log("ERROR: no status page!");
			return "http://localhost:8080/mmdb.html"; // development: hosted
		} else {
			return statusPage;
		}
	}

	void setUrl(HttpMethod method) {
		setUrl(method, "");
	}

	void setUrl(HttpMethod method, String sessionKey) {
		String url = getContextUrl() + "UploadBlob"; // FIXME parameterize
		if (sessionKey != null && !sessionKey.equals("")) {
			url += "?session=" + sessionKey;
		}
		try {
			if (url.startsWith("https")) {
				log("Using https @ " + url);
				method.setURI(new HttpsURL(url));
			} else {
				log("Using http @ " + url);
				method.setURI(new HttpURL(url));
			}
		} catch (URIException e) {
			e.printStackTrace();
		}
		// now set credentials
		String creds = getParameter("credentials");
		if (creds != null && !creds.equals("")) {
			method.addRequestHeader("Cookie", "sessionKey=" + creds);
		} else {
			log("ERROR: no credentials!");
		}
	}

	String getSessionKey() throws IOException {
		GetMethod get = new GetMethod();
		setUrl(get);
		HttpClient client = new HttpClient();
		log("requesting session key ...");
		try {
			client.executeMethod(get);
		} catch (HttpException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
		String s;
		try {
			s = get.getResponseBodyAsString();
			String sessionKey = s.replaceFirst(".*\"([0-9a-f]+)\".*", "$1");
			// FIXME hack to parse JSON
			log("got session key " + sessionKey);
			return sessionKey;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}

	// upload a single file
	void upload(FileUpload upload) {
		try {
			// acquire the session key
			upload.setSessionKey(getSessionKey());
			// set up the POST
			PostMethod post = new PostMethod();
			setUrl(post, upload.getSessionKey());
			List<Part> parts = new LinkedList<Part>();
			FileNameMap fileNameMap = URLConnection.getFileNameMap();
			String mimeType = fileNameMap.getContentTypeFor(upload.getName());
			parts.add(new FilePart("f1", upload.getFile(), mimeType, null));
			// see if we need to add a collection URI and name part
			Collection collection = upload.getCollection();
			if (upload.hasCollection()) {
				String collectionUri = collection.getUri();
				if (collectionUri != null) {
					log("adding collection uri part " + collectionUri);
					parts.add(new StringPart("collectionUri", collectionUri));
				}
				String collectionName = collection.getName();
				log("adding collection name part " + collectionName);
				parts.add(new StringPart("collection", collectionName));
			}
			post.setRequestEntity(new MultipartRequestEntity(parts
					.toArray(new Part[] {}), post.getParams()));
			try {
				HttpClient client = new HttpClient();
				// FIXME trace
				upload.setState(UploadState.IN_PROGRESS);
				System.out.println("POST " + upload);
				client.executeMethod(post);
				// FIXME trace
				if (post.getStatusCode() != 200) {
					upload.setState(UploadState.FAILED);
					log(upload + " " + post.getStatusCode()
							+ post.getStatusLine());
					throw new IOException("post failed");
				}
				if (upload.hasCollection() && !collection.hasUri()) {
					String response = post.getResponseBodyAsString();
					Pattern regex = Pattern
							.compile(
									".*<\\s*li\\s*class\\s*=\\s*['\"]\\s*collection\\s*['\"]\\s*>([^<]+).*",
									Pattern.MULTILINE | Pattern.DOTALL);
					String collectionUri = regex.matcher(response)
							.replaceFirst("$1").trim().replaceAll("&amp;", "&");
					log("got collection uri from server: " + collectionUri);
					collection.setUri(collectionUri);
				}
				upload.setState(UploadState.COMPLETE);
				log(upload + "");
			} catch (Exception x) {
				upload.setState(UploadState.FAILED);
				log(upload + ", Exception during POST: " + x.getMessage(), x);
			}
			switch (upload.getState()) {
			case COMPLETE:
				onComplete(upload);
				break;
			case FAILED:
				// FIXME notify page
				break;
			default:
				// wha...?
				break;
			}
		} catch (IOException x) {
			upload.setState(UploadState.FAILED);
			log(upload + ", Exception setting up POST: " + x.getMessage(), x);
		}
	}

	void onComplete(FileUpload upload) throws HttpException, IOException {
		checkProgress(upload, true);
	}

	void onProgress(FileUpload upload) throws HttpException, IOException {
		checkProgress(upload, false);
	}

	void checkProgress(FileUpload upload, boolean checkComplete)
			throws HttpException, IOException {
		try {
			GetMethod get = new GetMethod();
			setUrl(get, upload.getSessionKey());
			HttpClient client = new HttpClient();
			log(upload + " requesting progress ...");
			client.executeMethod(get);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					get.getResponseBodyAsStream()));
			String line = "";
			int percentComplete = 0;
			while ((line = br.readLine()) != null) {
				log(line);
				System.out.println("server reported " + line); // FIXME trace
				if (!checkComplete && line.contains("percentComplete")) {
					String pc = line.replaceFirst(
							".*\"percentComplete\":([0-9]+).*", "$1");
					percentComplete = Integer.parseInt(pc);
					upload.setProgress(percentComplete);
					call("dndAppletProgressIndex", new Object[] {
							percentComplete, upload.getIndex() });
				}
				if (checkComplete && line.contains("uris\":[\"")
						&& line.contains("\"isFinished\":true")) {
					uploading.remove(upload);
					upload.setState(UploadState.COMPLETE);
					line = line.replaceFirst(".*\"uris\":\\[\"([^\\]]*)\\].*",
							"$1");
					log("looking for uris in " + line); // FIXME debug
					// there should only be one.
					String uri = null;
					for (String uriReported : line.split("\",?\"?")) {
						uri = uriReported;
					}
					upload.setUri(uri);
					call("dndAppletFileUploaded",
							new Object[] { uri, upload.getIndex() + "" });
				}
			}
		} catch (Exception x) {
			log(upload + " no progress, or progress not available: "
					+ x.getMessage());
		}
	}
}
