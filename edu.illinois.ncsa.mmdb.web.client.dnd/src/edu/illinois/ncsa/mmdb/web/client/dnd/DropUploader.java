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
import java.io.Reader;
import java.io.StringWriter;
import java.net.FileNameMap;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import netscape.javascript.JSObject;

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
import org.apache.commons.httpclient.methods.multipart.StringPart;

public class DropUploader extends JApplet implements DropTargetListener {
	public DropTarget dropTarget;
	private JPanel mainCards;
	private static final long serialVersionUID = 9000;
	
	@Override
	public void init() {
		try {
			duInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	ImageIcon getIcon(String name, String label) {
		URL iconUrl = getClass().getResource("/edu/illinois/ncsa/mmdb/web/client/dnd/"+name);
		return new ImageIcon(iconUrl, label);
	}
	
	public void duInit() throws Exception {
		setSize(150, 100);

		ImageIcon dropIcon = getIcon("Load.png", "Upload");
		ImageIcon doneIcon = getIcon("Green_check.png", "Done");
		ImageIcon errorIcon = getIcon("dialog-error.png","Error");
		
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
			log("background color = "+bgColor);
			if(bgColor != null && !bgColor.equals("")) {
				Color c = Color.decode(bgColor);
				mainCards.setBackground(c);
				dropLabel.setBackground(c);
				doneLabel.setBackground(c);
				errorLabel.setBackground(c);
			}
		} catch(Exception x) {
			// fall through
			x.printStackTrace();
		}
		
		// check auth
		String auth = getParameter("credentials");
		log("credentials = "+auth);
		
		mainCards.setBounds(0, 0, 150, 100);
		// This class will handle the drop events
		dropTarget = new DropTarget(mainCards, this);
		
		getContentPane().add(mainCards, BorderLayout.CENTER);

		setVisible(true);
		
		// MMDB-576 applet to javascript communication
//		callJavascript("Applet started");
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
		//ta.setText(s);
		System.out.println(s);
	}
	
	void droppedFile(URI uri, List<File> files) {
		if(uri != null && uri.isAbsolute() && uri.getScheme().equals("file")) {
			droppedFile(new File(uri.getPath()), files);
		}
	}
    void droppedFile(File f, List<File> files) {
		if(f != null) {
			log("dropped file "+f);
			files.add(f);
		}
	}
	
	public void drop(DropTargetDropEvent dtde) {
		try {
			dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
			// O.k., get the dropped object and try to figure out what it is:
			Transferable tr = dtde.getTransferable();
			List<File> files = new LinkedList<File>();
			for(DataFlavor flavor : tr.getTransferDataFlavors()) {
				URI uri = null;
				if (flavor.isFlavorJavaFileListType()) {
					for(File file : (List<File>) tr.getTransferData(flavor)) {
						droppedFile(file,files);
					}
					break;
				} else if(flavor.isFlavorTextType()) {
					BufferedReader br = new BufferedReader(flavor.getReaderForText(tr));
					String s = null;
					while((s = br.readLine()) != null) {
						uri = new URI(s);
						droppedFile(uri, files);
						break;
					}
					break;
				} else if(flavor.isMimeTypeEqual("application/x-java-url")) {
					URI url = URI.create(tr.getTransferData(flavor)+"");
					droppedFile(url, files);
					break;
				} else {
					log("unknown "+flavor);
				}
			}
			if(files.size()==0) {
				log("no files dropped! " + dtde);
			} else {
				// FIXME use a better way of determining collection name than selecting from first file
				String collectionName = null;
				if(files.get(0).isDirectory()) {
					collectionName = files.get(0).getName();
					log("collection name = "+collectionName);
				}
				files = expandDirectories(files,false); // expand directories 
				for(File file : files) {
					JSObject window = JSObject.getWindow(DropUploader.this);
					window.call("dndAppletFileDropped",new Object[] { file.getName() });
				}
				//ta.setText(files.size()+" file(s) dropped: "+files);
				uploadFiles(files,collectionName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dtde.dropComplete(true);
		}
	}

	List<File> expandDirectories(List<File> files, boolean recursive) {
		List<File> expanded = new LinkedList<File>();
		for(File f : files) {
			if(f.isDirectory()) {
				if(recursive) {
					log("recursively expanding directory "+f);
					expanded.addAll(expandDirectories(Arrays.asList(f.listFiles()),true));
				} else {
					log("listing directory "+f);
					for(File kid : f.listFiles()) {
						if(!kid.isDirectory()) {
							log("adding file "+kid);
							expanded.add(kid);
						}
					}
				}
			} else {
				log("adding file "+f);
				expanded.add(f);
			}
		}
		log("added "+expanded.size()+" file(s)");
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
		// now set credentials
		String creds = getParameter("credentials");
		if(creds != null && !creds.equals("")) {
			method.addRequestHeader("Cookie","sessionKey="+creds);
		} else {
			showErrorCard();
		}
	}

	String getSessionKey() throws HttpException, IOException {
		GetMethod get = new GetMethod();
		setUrl(get);
		HttpClient client = new HttpClient();
		log("requesting session key ...");
		client.executeMethod(get);
		String s = get.getResponseBodyAsString();
		String sessionKey = s.replaceFirst(".*\"([0-9a-f]+)\".*","$1"); // FIXME hack to parse JSON
		log("got session key "+sessionKey);
		return sessionKey;
	}

	String getRedirectUrl(String queryUrl) throws HttpException, IOException {
		GetMethod get = new GetMethod();
		get.setURI(new HttpURL(queryUrl));
		HttpClient client = new HttpClient();
		log("requesting redirect url ...");
		client.executeMethod(get);
		String s = get.getResponseBodyAsString();
		log("got redirect url "+s);
		return s;
	}

	public void update() {
	}
	
	/**
	 * Simple function called from javascript for testing. 
	 */
	public void poke() {
		mainCards.add(new JLabel("Poke4!"), "poke");
		((CardLayout)mainCards.getLayout()).show(mainCards, "poke");
		repaint();
		(new Thread() {
			@Override
			public void run() {
				try {
					sleep(2500);
				} catch(InterruptedException x) {
				} finally {
					log("poking applet");
					showCard("drop");
				}
			}
		}).start();
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
	
	void showCard(String name) {
		((CardLayout)mainCards.getLayout()).show(mainCards, name);
		repaint();
	}
	
	ProgressThread startProgressThread(String sessionKey) {
		ProgressThread progressThread = new ProgressThread(this, sessionKey);
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
				} catch(InterruptedException x) {
				} finally {
					log("showing drop card");
					showCard("drop");
				}
			}
		}).start();
	}

	public static final int BATCH_SIZE=1; // FIXME changing to >1 would break stuff
	
	class BatchPostThread extends Thread {
		public List<File> files;
		public String collectionName;
		String collectionUri;
		HttpClient client;
		String postBatch(List<File> batch, int offset, int nFiles) throws Exception {
			// acquire the session key and start tracking progress
			String sessionKey = getSessionKey();
			ProgressThread progressThread = startProgressThread(sessionKey);
			// set up the POST batch
			PostMethod post = new PostMethod();
			setUrl(post,sessionKey);
			List<Part> parts = new LinkedList<Part>();
			int i = 1;
			for(File file : batch) {
				FileNameMap fileNameMap = URLConnection.getFileNameMap();
				String mimeType = fileNameMap.getContentTypeFor(file.getName());
				FilePart part = new FilePart("f"+i, file, mimeType, null);
				parts.add(part);
				i++;
			}
			if(collectionUri != null) {
				log("adding collection uri part "+collectionUri);
				parts.add(new StringPart("collectionUri",collectionUri));
			} else if(collectionName != null) {
				log("adding collection name part "+collectionName);
				parts.add(new StringPart("collection",collectionName));
			}
			post.setRequestEntity(new MultipartRequestEntity(parts.toArray(new Part[]{}), post.getParams()));
			client.executeMethod(post);
			if(post.getStatusCode() != 200) {
				log("post failed! "+post.getStatusLine());
				progressThread.stopShowingProgress();
				showErrorCard();
				throw new Exception("post failed");
			} else if(collectionName != null && collectionUri == null) {
				collectionUri = post.getResponseBodyAsString();
				log("got collection uri from server: "+collectionUri);
			}
			Thread.sleep(2);
			// now figure out which uri's were uploaded
			for(String uri : progressThread.getUrisUploaded()) {
				log("got uris for uploaded files = "+uri); // FIXME
				JSObject window = JSObject.getWindow(DropUploader.this);
				window.call("dndAppletFileUploaded",new Object[] { uri });
			}
			progressThread.stopShowingProgress();
			return sessionKey;
		}
		@Override
		public void run() {
			client = new HttpClient();
			try {
				List<File> batch = new LinkedList<File>();
				int i = 0;
				for(File file : files) {
					batch.add(file);
					if(batch.size()==BATCH_SIZE) {
						postBatch(batch,i,files.size());
						batch = new LinkedList<File>();
					}
					i++;
				}
				// remember to do the last batch
				if(batch.size() > 0) {
					postBatch(batch,i,files.size());
				}
				// we're done
			} catch(Exception x) {
				showErrorCard();
			}
		}
	}
	
	BatchPostThread postThread;
	
	void uploadFiles(List<File> files) throws HttpException, IOException {
		uploadFiles(files,null);
	}
	void uploadFiles(List<File> files, String collectionName) throws HttpException, IOException {
		if(postThread != null) {
			if(!postThread.isAlive()) { postThread = null; }
			else { return; }
		} // can't post while posting
		// redirect the browser to start checking progress
		//getAppletContext().showDocument(new URL(getStatusPage()+"#upload?session="+sessionKey));
		//getAppletContext().showDocument(new URL("javascript:uploadAppletCallback('"+sessionKey+"')"));
		try {
			// post the data
			postThread = new BatchPostThread();
			postThread.files = files;
			postThread.collectionName = collectionName;
			log("posting data for "+files.size()+" file(s)");
			postThread.start();
		} catch(Exception x) {
			showErrorCard();
		} finally {
			//showDropTarget();
		}
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
