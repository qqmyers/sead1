package edu.illinois.ncsa.mmdb.web.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;

import edu.illinois.ncsa.mmdb.web.client.event.CancelEvent;
import edu.illinois.ncsa.mmdb.web.client.event.CancelHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUploadedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUploadedHandler;
import edu.illinois.ncsa.mmdb.web.client.ui.ProgressBar;

public class UploadWidget extends Composite {
	interface JSONCallback {
		void gotJSON(JSONObject object);
		void error(Throwable t);
	}
	
	void jsonRequest(String url, final JSONCallback callback) throws RequestException {
		RequestBuilder b = new RequestBuilder(RequestBuilder.GET, url);
		b.sendRequest(null, new RequestCallback() {
			public void onError(Request request, Throwable exception) {
				callback.error(exception);
			}
			public void onResponseReceived(Request request, Response response) {
				String jsonText = response.getText();
				GWT.log(jsonText, null);
				JSONValue value = JSONParser.parse(jsonText);
				if(value.isObject() != null) {
					callback.gotJSON(value.isObject());
				}
			}
		});
	}
	
	void jsonRequest(final String url, final JSONCallback callback, int delay) {
		(new Timer() {
			public void run() {
				try {
					jsonRequest(url,callback);
				} catch (RequestException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).schedule(delay);
	}
	
	// has a file upload form
	FormPanel uploadForm = new FormPanel();
	FileUpload fu = new FileUpload();
	// status
	Label statusLabel = new Label();
	// progress bar
	ProgressBar progressBar = new ProgressBar();
	
	String getModuleBaseUrl() {
		String baseUrl = GWT.getModuleBaseURL();
		// uncomment next line for hosted
		//baseUrl = baseUrl.replaceFirst("/[^/]+/$", "/");
		return baseUrl;
	}
	
	public UploadWidget() {
		// multiple uploads
		VerticalPanel uploadStackPanel = new VerticalPanel();
		uploadStackPanel.addStyleName("uploadMainPanel");
		HorizontalPanel uploadPanel = new HorizontalPanel();
		uploadForm.setAction("UploadBlob");
		uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
		uploadForm.setMethod(FormPanel.METHOD_POST);
		fu.setName("f1"); // upload servlet expects the data to be named f{n} starting with 1
		final HorizontalPanel formContents = new HorizontalPanel();
		uploadForm.setWidget(formContents);
		formContents.add(fu);
		uploadPanel.add(uploadForm);
		// and a cancel button
		Button cancel = new Button("Cancel");
		uploadPanel.add(cancel);
		uploadStackPanel.add(uploadPanel);
		uploadStackPanel.add(statusLabel);
		uploadStackPanel.add(progressBar);
		progressBar.setVisible(false);
		// button behavior:
		cancel.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				CancelEvent ce = new CancelEvent();
				fireEvent(ce);
			}
		});
		fu.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				final String uploadServletUrl = GWT.getModuleBaseURL() + "UploadBlob";
				try {
					jsonRequest(uploadServletUrl, new JSONCallback() {
						public void error(Throwable t) {
						}
						public void gotJSON(JSONObject object) {
							if(object.containsKey("session")) {
								String sessionKey = object.get("session").isString().stringValue();
								uploadForm.setAction("UploadBlob?session="+sessionKey);
								// now that we have a session key, submit the POST
								uploadForm.submit();
								// now show progress
								showProgress(sessionKey, uploadServletUrl);
							}
						}
					});
				} catch (RequestException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		// form behavior:
		uploadForm.addSubmitHandler(new SubmitHandler() {
			public void onSubmit(SubmitEvent event) {
				statusLabel.setText("upload submitted");
			}
		});
		uploadForm.addSubmitCompleteHandler(new SubmitCompleteHandler() {
			public void onSubmitComplete(SubmitCompleteEvent event) {
				statusLabel.setText("submit complete"); // FIXME
			}
		});
		// make it go
		initWidget(uploadStackPanel);
	}
	
	void showProgress(String sessionKey, String uploadServletUrl) {
		// now start asking for updates
		final String updateUrl = uploadServletUrl + "?session="+sessionKey;
		final JSONCallback handleUpdate = new JSONCallback() {
			int twiddle = 0;
			String dots[] = new String[] { "", ".", "..", "..." };
			String dots() { return dots[twiddle++ % 3]; }
			public void error(Throwable t) {
			}
			public void gotJSON(JSONObject dict) {
				/* will get something like this:
				 * {"serverUrl":"http://localhost:8080/tupelo",
				 * "hasStarted":true,"uris":[
				 * "http://localhost:8080/mmdb.html#dataset?id=tag:medici@uiuc.edu,2009:data_EtV6RYfBLq6D9Q6W-pJ0hg"
				 * ], "filenames":["ideaLogo.jpg"],
				 * "isUploaded":[true], "bytesRead":1397,
				 * "contentLength":1397, "isFinished":true,
				 * "percentComplete":100}
				 */
				boolean refire = true; // whether to continue checking status
				if(dict == null) {
					statusLabel.setText("error reading upload status");
					refire = false;
				} else {
					if(dict.containsKey("hasStarted") && dict.get("hasStarted").isBoolean() != null) {
						if(!dict.get("hasStarted").isBoolean().booleanValue()) {
							statusLabel.setText("waiting to upload "+dots());
						}
					}
					if(dict.containsKey("percentComplete") &&
							dict.get("percentComplete").isNumber() != null) {
						progressBar.setVisible(true);
						int percentComplete = (int) dict.get("percentComplete").isNumber().doubleValue();
						progressBar.setProgress(percentComplete);
						if(percentComplete == 100) {
							statusLabel.setText("uploaded, saving "+dots());
						} else {
							statusLabel.setText("uploading, "+percentComplete+"% complete");
						}
					}
					if(dict.containsKey("isFinished") && dict.get("isFinished").isBoolean() != null) {
						if(dict.get("isFinished").isBoolean().booleanValue()) {
							statusLabel.setText("upload complete.");
							// uri
							String uri = dict.get("uris").isArray().get(0).isString().stringValue();
							// ersatz decanonicalization! FIXME make Uri canon/decanon work on the client!
							if(uri.startsWith("http://")) {
								int ix = uri.indexOf("/api/image/");
								if(ix != -1) {
									uri = uri.substring(ix+11);
								}
							}
							DatasetUploadedEvent event = new DatasetUploadedEvent();
							event.setDatasetUri(uri);
							fireEvent(event);
							refire = false;
						}
					}
				}
				//refire = false; // FIXME
				if(refire) {
					jsonRequest(updateUrl, this, 250);
				}
			}
		}; 
		jsonRequest(updateUrl, handleUpdate, 100);
	}
	
	public void addDatasetUploadedHandler(DatasetUploadedHandler h) {
		addHandler(h, DatasetUploadedEvent.TYPE);
	}
	
	public void addCancelHandler(CancelHandler h) {
		addHandler(h, CancelEvent.TYPE);
	}
}
