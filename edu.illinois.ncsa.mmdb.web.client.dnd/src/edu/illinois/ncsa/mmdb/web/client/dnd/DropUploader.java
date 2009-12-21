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
	public DropTarget dropTarget;
	private JTextArea ta;
	private JPanel mainCards;
	private ProgressPie progressPie;
	private static final long serialVersionUID = 9000;
	
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
		setSize(64, 64);

		ImageIcon dropIcon = getIcon("Load.png", "Upload");
		ImageIcon doneIcon = getIcon("Green_check.png", "Done");
		ImageIcon errorIcon = getIcon("error.png","Error");
		
		// the drop target
		JLabel dropLabel = new JLabel(dropIcon);
		dropLabel.setOpaque(true);
		// This class will handle the drop events
		dropTarget = new DropTarget(dropLabel, this);
		
		// the progress pie
		progressPie = new ProgressPie();
		
		JLabel doneLabel = new JLabel(doneIcon);
		JLabel errorLabel = new JLabel(errorIcon);
		
		// now layout the components in a two-card card layout
		mainCards = new JPanel(new CardLayout());
		mainCards.add(dropLabel, "drop");
		mainCards.add(progressPie, "progress");
		mainCards.add(doneLabel, "done");
		mainCards.add(errorLabel, "error");
		
		// set the background colors of the components
		try {
			String bgColor = getParameter("background");
			if(bgColor != null && !bgColor.equals("")) {
				Color c = Color.decode(bgColor);
				mainCards.setBackground(c);
				dropLabel.setBackground(c);
				doneLabel.setBackground(c);
				errorLabel.setBackground(c);
				progressPie.setBackground(c);
			}
		} catch(Exception x) {
			// fall through
			x.printStackTrace();
		}
		
		getContentPane().add(mainCards, BorderLayout.CENTER);

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
		//ta.setText(s);
		System.out.println(s);
	}
	
	void droppedFile(URI uri, List<File> files) {
		if(uri != null && uri.isAbsolute() && uri.getScheme().equals("file")) {
			File f = new File(uri.getPath());
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
						log("dropped file "+file);
						files.add(file);
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
				files = expandDirectories(files,false); // expand directories 
				//ta.setText(files.size()+" file(s) dropped: "+files);
				uploadFiles(files);
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

	int getProgress(String sessionKey) throws HttpException, IOException {
		try {
			GetMethod get = new GetMethod();
			setUrl(get,sessionKey);
			HttpClient client = new HttpClient();
			log("requesting progress ...");
			client.executeMethod(get);
			BufferedReader br = new BufferedReader(new InputStreamReader(get.getResponseBodyAsStream()));
			String line = "";
			int percentComplete = 0;
			while((line = br.readLine()) != null) {
				log(line);
				if(line.contains("percentComplete")) {
					String pc = line.replaceFirst(".*\"percentComplete\":([0-9]+).*","$1"); // FIXME hack to parse JSON
					percentComplete = Integer.parseInt(pc);
				}
			}
			log("got progress "+percentComplete);
			return percentComplete;
		} catch(Exception x) {
			log("no progress, or progress not available: "+x.getMessage());
			return 0;
		}
	}
	
	class FakeProgressThread extends Thread {
		int fakeProgress = 0;
		boolean stop = false;
		public void run() {
			try {
				while(!stop) {
					progressPie.setProgress(fakeProgress);
					Thread.sleep(500);
					fakeProgress = (fakeProgress + 17) % 100;
				}
			} catch(Exception x) {
			}
		}
		public void stopShowingProgress() {
			stop = true;
		}
	}
	
	class ProgressThread extends Thread {
		String sessionKey;
		boolean stop = false;
		public void run() {
			try {
				while(!stop) {
					int progress = getProgress(sessionKey);
					progressPie.setProgress(progress);
					Thread.sleep(500);
				}
			} catch(Exception x) {
			}
		}
		public void stopShowingProgress() {
			stop = true;
		}
		public void setSessionKey(String sessionKey) {
			this.sessionKey= sessionKey;
		}
	}
	
	ProgressThread progressThread;

	void showCard(String name) {
		((CardLayout)mainCards.getLayout()).show(mainCards, name);
		repaint();
	}
	
	void stopProgressThread() {
		if(progressThread != null) {
			progressThread.stopShowingProgress();
		}
	}
	
	void showProgressPie() {
		showCard("progress");
	}
	
	void showDropTarget() {
		showCard("drop");
		stopProgressThread();
	}
	
	void showDoneCard() {
		showCard("done");
		stopProgressThread();
	}
	
	class PostThread extends Thread {
		public PostMethod post;
		public void run() {
			try {
				HttpClient client = new HttpClient();
				client.executeMethod(post);
				if(post.getStatusCode() != 200) {
					log("post failed! "+post.getStatusLine());
					getAppletContext().showDocument(new URL("javascript:uploadCompleteCallback()"));
					showCard("error");
				} else {
					log("post complete with status "+post.getStatusLine());
					getAppletContext().showDocument(new URL("javascript:uploadCompleteCallback()"));
					showCard("done");
				}
				progressThread.stopShowingProgress();
			} catch(Exception x) {
				showCard("error");
			}
		}
	}
	PostThread postThread;
	
	void uploadFiles(List<File> files) throws HttpException, IOException {
		if(postThread != null) {
			if(!postThread.isAlive()) { postThread = null; }
			else { return; }
		} // can't post while posting
		postThread = new PostThread();
		// redirect the browser to start checking progress
		//getAppletContext().showDocument(new URL(getStatusPage()+"#upload?session="+sessionKey));
		//getAppletContext().showDocument(new URL("javascript:uploadAppletCallback('"+sessionKey+"')"));
		try {
			// acquire the session key
			String sessionKey = getSessionKey();
			stopProgressThread();
			showProgressPie();
			progressThread = new ProgressThread();
			progressThread.setSessionKey(sessionKey);
			progressThread.start();
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
			postThread.post = post;
			post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams())); 
			log("posting data for "+files.size()+" file(s)");
			postThread.start();
		} catch(Exception x) {
			showCard("error");
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
