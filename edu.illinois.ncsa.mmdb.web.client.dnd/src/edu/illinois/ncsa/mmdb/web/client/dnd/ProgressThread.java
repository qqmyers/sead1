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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;

public class ProgressThread extends Thread {
	DropUploader applet;
	String sessionKey = null;
	boolean stop = false;
	int progress = 0;
	List<String> urisUploaded = new LinkedList<String>();
	int offset = 0; // offset into file list

	void log(String s) {
		// System.out.println(s);
	} // FIXME debug

	public ProgressThread(DropUploader applet, String sessionKey, int offset) {
		this.applet = applet;
		this.offset = offset;
		setSessionKey(sessionKey);
	}

	@Override
	public void run() {
		try {
			while (!stop) {
				updateProgress();
				if (progress >= 0) {
					// System.out.println("progress thread reporting " +
					// progress);
					applet.call("dndAppletProgressIndex", new Object[] {
							progress, offset });
				}
				sleep(250);
			}
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	void updateProgress() throws HttpException, IOException {
		if (sessionKey == null) {
			progress = -1;
		}
		try {
			GetMethod get = new GetMethod();
			applet.setUrl(get, sessionKey);
			HttpClient client = new HttpClient();
			log("requesting progress ...");
			client.executeMethod(get);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					get.getResponseBodyAsStream()));
			String line = "";
			int percentComplete = 0;
			while ((line = br.readLine()) != null) {
				log(line);
				System.out.println("server reported " + line); // FIXME trace
				if (line.contains("percentComplete")) {
					String pc = line.replaceFirst(
							".*\"percentComplete\":([0-9]+).*", "$1"); // FIXME
																		// hack
																		// to
																		// parse
																		// JSON
					percentComplete = Integer.parseInt(pc);
				}
				if (urisUploaded.size() == 0 && line.contains("uris\":[\"")
						&& line.contains("\"isFinished\":true")) {
					line = line.replaceFirst(".*\"uris\":\\[\"([^\\]]*)\\].*",
							"$1");
					log("looking for uris in " + line); // FIXME debug
					for (String uri : line.split("\",?\"?")) {
						urisUploaded.add(uri);
						applet.call("dndAppletFileUploaded", new Object[] {
								uri, offset + "" });//
						stopShowingProgress();
					}
				}
			}
			log("got progress " + percentComplete + " " + urisUploaded);
			progress = percentComplete;
		} catch (Exception x) {
			log("no progress, or progress not available: " + x.getMessage());
		}
	}

	public void stopShowingProgress() {
		try {
			updateProgress();
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stop = true;
	}

	public void setSessionKey(String sk) {
		sessionKey = sk;
	}

	public List<String> getUrisUploaded() {
		return urisUploaded;
	}
}
