package edu.illinois.ncsa.mmdb.web.client.dnd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;

import netscape.javascript.JSObject;

public class ProgressThread extends Thread {
	DropUploader applet;
	String sessionKey = null;
	boolean stop = false;
	int progress = 0;
	List<String> urisUploaded = new LinkedList<String>();
	
	void log(String s) { System.out.println(s); } // FIXME debug
	
	public ProgressThread(DropUploader applet, String sessionKey) {
		this.applet = applet;
		setSessionKey(sessionKey);
	}
	
	@Override
	public void run() {
		try {
			while(!stop) {
				updateProgress();
				if(progress >= 0) {
					System.out.println("progress thread reporting "+progress);
					JSObject window = JSObject.getWindow(applet);
					window.call("dndAppletProgress",new Object[] { progress });
				}
				sleep(250);
			}
		} catch(Exception x) {
			x.printStackTrace();
		}
	}
	
	void updateProgress() throws HttpException, IOException {
		if(sessionKey == null) {
			progress = -1;
		}
		try {
			GetMethod get = new GetMethod();
			applet.setUrl(get,sessionKey);
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
				if(urisUploaded.size()==0 && line.contains("uris\":[\"")) {
					line = line.replaceFirst(".*\"uris\":\\[\"([^\\]]*)\\].*","$1");
					log("looking for uris in "+line); // FIXME debug
					for(String uri : line.split("\",?\"?")) {
						urisUploaded.add(uri);
					}
				}
			}
			log("got progress "+percentComplete+" "+urisUploaded);
			progress = percentComplete;
		} catch(Exception x) {
			log("no progress, or progress not available: "+x.getMessage());
		}
	}
	
	public void stopShowingProgress() {
		stop = true;
	}
	
	public void setSessionKey(String sk) {
		sessionKey = sk;
	}
	
	public List<String> getUrisUploaded() {
		return urisUploaded;
	}
}
