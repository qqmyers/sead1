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
import java.io.Reader;
import java.io.StringWriter;
import java.net.FileNameMap;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
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

public class DropUploader extends JApplet implements DropTargetListener {
	public DropTarget dropTarget;
	private JPanel mainCards;
	private static final long serialVersionUID = 9000;
	JSObject window = null;

	public static final String VERSION = "1780";

	@Override
	public void init() {
		try {
			duInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	ImageIcon getIcon(String name, String label) {
		URL iconUrl = getClass().getResource(
				"/edu/illinois/ncsa/mmdb/web/client/dnd/" + name);
		return new ImageIcon(iconUrl, label);
	}

	@SuppressWarnings("deprecation")
	public void duInit() throws Exception {
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
		// MMDB-576 applet to javascript communication
		// callJavascript("Applet started");
		log("successful init");
	}

	public void dragEnter(DropTargetDragEvent dtde) {
		// System.out.println("Drag Enter");
	}

	public void dragExit(DropTargetEvent dte) {
		// System.out.println("Drag Exit");
	}

	public void dragOver(DropTargetDragEvent dtde) {
		// System.out.println("Drag Over");
	}

	public void dropActionChanged(DropTargetDragEvent dtde) {
		// System.out.println("Drop Action Changed");
	}

	void log(String s) {
		// System.out.println(s);
	}

	void droppedFile(URI uri, List<File> files) {
		if (uri != null && uri.isAbsolute() && uri.getScheme().equals("file")) {
			droppedFile(new File(uri.getPath()), files);
		}
	}

	void droppedFile(File f, List<File> files) {
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
				URI uri = null;
				log("DataFlavor = " + flavor);
				if (flavor.isFlavorJavaFileListType()) {
					for (File file : (List<File>) tr.getTransferData(flavor)) {
						droppedFile(file, files);
					}
				} else if (flavor.isFlavorTextType()) {
					BufferedReader br = new BufferedReader(
							flavor.getReaderForText(tr));
					String s = null;
					while ((s = br.readLine()) != null) {
						uri = new URI(s);
						droppedFile(uri, files);
					}
				} else if (flavor.isMimeTypeEqual("application/x-java-url")) {
					URI url = URI.create(tr.getTransferData(flavor) + "");
					droppedFile(url, files);
				} else {
					log("unknown " + flavor);
				}
			}
			if (files.size() == 0) {
				log("no files dropped! " + dtde);
			} else {
				// FIXME use a better way of determining collection name than
				// selecting from first file
				String collectionName = null;
				if (files.get(0).isDirectory()) {
					collectionName = files.get(0).getName();
					log("collection name = " + collectionName);
				}
				files = expandDirectories(files, false); // expand directories
				for (File file : files) {
					call("dndAppletFileDropped", new Object[] { file.getName(),
							file.length() + "" });
				}
				// ta.setText(files.size()+" file(s) dropped: "+files);
				uploadFiles(files, collectionName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dtde.dropComplete(true);
		}
	}

	List<File> expandDirectories(List<File> files, boolean recursive) {
		List<File> expanded = new LinkedList<File>();
		for (File f : files) {
			if (f.isDirectory()) {
				if (recursive) {
					log("recursively expanding directory " + f);
					expanded.addAll(expandDirectories(
							Arrays.asList(f.listFiles()), true));
				} else {
					log("listing directory " + f);
					for (File kid : f.listFiles()) {
						if (!kid.isDirectory()) {
							log("adding file " + kid);
							expanded.add(kid);
						}
					}
				}
			} else {
				log("adding file " + f);
				expanded.add(f);
			}
		}
		log("added " + expanded.size() + " file(s)");
		return expanded;
	}

	String getContextUrl() {
		return getStatusPage().replaceFirst("/[^/]+$", "/");
	}

	String getStatusPage() {
		String statusPage = getParameter("statusPage");
		if (statusPage == null) {
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
			showErrorCard();
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

	public void update() {
	}

	/**
	 * Simple function called from javascript for testing.
	 */
	public void poke() {
		repaint();
	}

	/**
	 * Test function calling function in javascript.
	 * 
	 * @param msg
	 */
	public void callJavascript(String msg) {
		poke();
		JSObject window = JSObject.getWindow(this);
		window.call("dndAppletPoke", null);
	}

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
				System.out.println(trace); // FIXME trace
				log("calling javascript " + functionName + " with args " + args);
				window.call(functionName, args);
			} catch (JSException x) {
				x.printStackTrace();
			}
		}
	}

	void showCard(String name) {
		((CardLayout) mainCards.getLayout()).show(mainCards, name);
		repaint();
	}

	ProgressThread startProgressThread(String sessionKey, int offset) {
		ProgressThread progressThread = new ProgressThread(this, sessionKey,
				offset);
		progressThread.start();
		return progressThread;
	}

	void showDropTarget() {
		showCard("drop");
	}

	void showDoneCard() {
		showCard("done");
	}

	void showErrorCard() {
		showCard("error");
		log("sleeping");
		(new Thread() {
			@Override
			public void run() {
				try {
					sleep(2500);
				} catch (InterruptedException x) {
				} finally {
					log("showing drop card");
					showCard("drop");
				}
			}
		}).start();
	}

	// TODO get rid of batch stuff. batch size MUST be 1 for javascript
	// UI stuff to work.
	public static final int BATCH_SIZE = 1;

	class BatchPostThread extends Thread {
		public List<File> files;
		public String collectionName;
		String collectionUri;
		HttpClient client;
		DropUploader applet;

		public BatchPostThread(DropUploader a) {
			applet = a;
		}

		String postBatch(List<File> batch, int offset, int nFiles)
				throws IOException, InterruptedException {
			// acquire the session key and start tracking progress
			String sessionKey = getSessionKey();
			ProgressThread progressThread = startProgressThread(sessionKey,
					offset);
			// set up the POST batch
			PostMethod post = new PostMethod();
			setUrl(post, sessionKey);
			List<Part> parts = new LinkedList<Part>();
			int i = 1;
			for (File file : batch) {
				FileNameMap fileNameMap = URLConnection.getFileNameMap();
				String mimeType = fileNameMap.getContentTypeFor(file.getName());
				FilePart part = new FilePart("f" + i, file, mimeType, null);
				parts.add(part);
				i++;
			}
			if (collectionUri != null) {
				log("adding collection uri part " + collectionUri);
				parts.add(new StringPart("collectionUri", collectionUri));
			} else if (collectionName != null) {
				log("adding collection name part " + collectionName);
				parts.add(new StringPart("collection", collectionName));
			}
			post.setRequestEntity(new MultipartRequestEntity(parts
					.toArray(new Part[] {}), post.getParams()));
			try {
				// FIXME trace
				System.out.println("POST " + offset + " " + batch);
				client.executeMethod(post);
			} catch (HttpException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw e;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw e;
			}
			if (post.getStatusCode() != 200) {
				log("post failed! " + post.getStatusLine());
				progressThread.stopShowingProgress();
				showErrorCard();
				throw new IOException("post failed");
			} else if (collectionName != null && collectionUri == null) {
				String response = post.getResponseBodyAsString();
				Pattern regex = Pattern
						.compile(
								".*<\\s*li\\s*class\\s*=\\s*['\"]\\s*collection\\s*['\"]\\s*>([^<]+).*",
								Pattern.MULTILINE | Pattern.DOTALL);
				collectionUri = regex.matcher(response).replaceFirst("$1")
						.trim().replaceAll("&amp;", "&");
				log("got collection uri from server: " + collectionUri);
			}
			System.out.println("Joining progress thread " + offset); // FIXME
																		// trace
			progressThread.join(2000);
			System.out.println("Joined progress thread " + offset); // FIXME
																	// trace
			return sessionKey;
		}

		@Override
		public void run() {
			client = new HttpClient();
			try {
				List<File> batch = new LinkedList<File>();
				int i = 0;
				for (File file : files) {
					batch.add(file);
					if (batch.size() == BATCH_SIZE) {
						postBatch(batch, i, files.size());
						batch = new LinkedList<File>();
					}
					i++;
				}
				// remember to do the last batch
				if (batch.size() > 0) {
					postBatch(batch, i, files.size());
				}
				// we're done
			} catch (Exception x) {
				x.printStackTrace();
				showErrorCard();
			}
		}
	}

	BatchPostThread postThread;

	void uploadFiles(List<File> files) throws HttpException, IOException {
		uploadFiles(files, null);
	}

	void uploadFiles(List<File> files, String collectionName)
			throws HttpException, IOException {
		if (postThread != null) {
			if (!postThread.isAlive()) {
				postThread = null;
			} else {
				return;
			}
		} // can't post while posting
			// redirect the browser to start checking progress
			// getAppletContext().showDocument(new
			// URL(getStatusPage()+"#upload?session="+sessionKey));
			// getAppletContext().showDocument(new
			// URL("javascript:uploadAppletCallback('"+sessionKey+"')"));
		try {
			// post the data
			postThread = new BatchPostThread(this);
			postThread.files = files;
			postThread.collectionName = collectionName;
			log("posting data for " + files.size() + " file(s)");
			postThread.start();
		} catch (Exception x) {
			showErrorCard();
		} finally {
			// showDropTarget();
		}
	}

	String readString(Reader r) {
		StringWriter w = new StringWriter();
		char buf[] = new char[8192];
		int n;
		try {
			while ((n = r.read(buf)) != -1) {
				w.write(buf, 0, n);
			}
			return w.toString();
		} catch (IOException x) {
			return "[error]";
		}
	}

	public static void main(String args[]) {
		if (args.length == 1 && args[0].equals("-v")) {
			System.out.println(VERSION);
		} else {
			new DropUploader();
		}
	}
}
