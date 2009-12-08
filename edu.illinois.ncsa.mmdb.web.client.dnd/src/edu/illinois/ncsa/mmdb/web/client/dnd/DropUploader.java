package edu.illinois.ncsa.mmdb.web.client.dnd;

import java.awt.BorderLayout;
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

import javax.swing.JApplet;
import javax.swing.JTextArea;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;

public class DropUploader extends JApplet implements DropTargetListener {
	public DropTarget dt;
	private JTextArea ta;
	private static final long serialVersionUID = 9000;
	
	public void init() {
		try {
			duInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void duInit() throws Exception {
		setSize(600, 300);

		ta = new JTextArea();
		ta.setText("6:Drop files here");
		ta.setBackground(Color.white);
		getContentPane().add(ta, BorderLayout.CENTER);

		// Set up our text area to receive drops...
		// This class will handle the drop events
		dt = new DropTarget(ta, this);
		setVisible(true);
	}

	public void dragEnter(DropTargetDragEvent dtde) {
		//System.out.println("Drag Enter");
	}

	public void dragExit(DropTargetEvent dte) {
		//System.out.println("Drag Exit");
	}

	public void dragOver(DropTargetDragEvent dtde) {
		//System.out.println("Drag Over");
	}

	public void dropActionChanged(DropTargetDragEvent dtde) {
		//System.out.println("Drop Action Changed");
	}

	void log(String s) {
		ta.setText(s);
		System.out.println(s);
	}
	
	public void drop(DropTargetDropEvent dtde) {
		try {
			dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
			// O.k., get the dropped object and try to figure out what it is:
			Transferable tr = dtde.getTransferable();
			List<File> files = new LinkedList<File>();
			for(DataFlavor flavor : tr.getTransferDataFlavors()) {
				if (flavor.isFlavorJavaFileListType()) {
					for(File file : (List<File>) tr.getTransferData(flavor)) {
						files.add(file);
					}
					break;
				} else if(flavor.isFlavorTextType()) {
					BufferedReader br = new BufferedReader(flavor.getReaderForText(tr));
					String s = null;
					while((s = br.readLine()) != null) {
						URI uri = new URI(s);
						if(uri.isAbsolute() && uri.getScheme().equals("file")) {
							files.add(new File(uri));
						}
					}
					break;
				} else {
					log("unknown "+flavor);
				}
			}
			dtde.dropComplete(true);
			if(files.size()==0) {
				System.out.println("Drop failed: " + dtde);
			} else {
				files = expandDirectories(files,false); // expand directories 
				ta.setText(files.size()+" file(s) dropped: "+files);
				uploadFiles(files);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	List<File> expandDirectories(List<File> files, boolean recursive) {
		List<File> expanded = new LinkedList<File>();
		for(File f : files) {
			if(f.isDirectory()) {
				if(recursive) {
					expanded.addAll(expandDirectories(Arrays.asList(f.listFiles()),true));
				} else {
					for(File kid : f.listFiles()) {
						if(!kid.isDirectory()) {
							expanded.add(kid);
						}
					}
				}
			} else {
				expanded.add(f);
			}
		}
		return expanded;
	}
	String getContextUrl() {
		return getStatusPage().replaceFirst("/[^/]+$","/");
	}
	
	String getStatusPage() {
		String statusPage = getParameter("statusPage");
		if(statusPage == null) {
			return "http://localhost:8080/mmdb.html"; // development: hosted
		} else {
			return statusPage;
		}
	}
	void setUrl(HttpMethod method) {
		setUrl(method, "");
	}
	void setUrl(HttpMethod method, String sessionKey) {
		String url = getContextUrl()+"UploadBlob"; // FIXME parameterize
		if(sessionKey != null && !sessionKey.equals("")) {
			url += "?session="+sessionKey;
		}
		try {
			method.setURI(new HttpURL(url));
		} catch (URIException e) {
			e.printStackTrace();
		}
	}

	String getSessionKey() throws HttpException, IOException {
		GetMethod get = new GetMethod();
		setUrl(get);
		HttpClient client = new HttpClient();
		client.executeMethod(get);
		String s = get.getResponseBodyAsString();
		String sessionKey = s.replaceFirst(".*\"([0-9a-f]+)\".*","$1");
		log("got session key "+sessionKey);
		return sessionKey;
	}
	
	void uploadFiles(List<File> files) throws HttpException, IOException {
		// acquire the session key
		String sessionKey = getSessionKey();
		// redirect the browser to start checking progress
		getAppletContext().showDocument(new URL(getStatusPage()+"#upload?session="+sessionKey)); // FIXME parameterize
		// post the data
		PostMethod post = new PostMethod();
		setUrl(post,sessionKey);
		Part parts[] = new Part[files.size()];
		for(int i = 0; i < files.size(); i++) {
			File file = files.get(i);
			FileNameMap fileNameMap = URLConnection.getFileNameMap();
			String mimeType = fileNameMap.getContentTypeFor(file.getName());
			FilePart part = new FilePart("f"+(i+1), file, mimeType, null);
			parts[i] = part;
		}
		post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));
		HttpClient client = new HttpClient();
		log("posting data for "+files.size()+" file(s)");
		client.executeMethod(post);
		log("post complete with status "+post.getStatusLine());
	}
	
	String readString(Reader r) {
		StringWriter w = new StringWriter();
		char buf[] = new char[8192];
		int n;
		try {
			while((n = r.read(buf)) != -1) {
				w.write(buf, 0, n);
			}
			return w.toString();
		} catch(IOException x) {
			return "[error]";
		}
	}
	public static void main(String args[]) {
		new DropUploader();
	}
}
