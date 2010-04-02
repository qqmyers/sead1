/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.TextFormatter;
import edu.illinois.ncsa.mmdb.web.client.UploadWidget;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetProperty;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetPropertyResult;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUploadedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUploadedHandler;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * A standalone page to upload files.
 * 
 * @author Luigi Marini
 *
 */
public class UploadPage extends Page {

	private static final String TITLE = "Upload";
	private UploadWidget uploadWidget;
	private FlexTable tableLayout;
	private static VerticalPanel appletStatusPanel;
	private static FlexTable uploadedDatasetsTable;

	public static final String DND_ENABLED_PREFERENCE = "dndAppletEnabled";
	
	public UploadPage() {
		super();
		setPageTitle(TITLE);
	}

	public UploadPage(MyDispatchAsync dispatchasync) {
		super(TITLE, dispatchasync);
	}
	
	@Override
	public void layout() {
		
		tableLayout = new FlexTable();
		
		tableLayout.addStyleName("uploadPageLayout");
		
		mainLayoutPanel.add(tableLayout);
		
		tableLayout.setText(0, 0, "Select the file you want to upload or click and drag a file or folder.");
		
		tableLayout.getFlexCellFormatter().setColSpan(0, 0, 2);
		
		uploadWidget = new UploadWidget(false);
		uploadWidget.addDatasetUploadedHandler(new DatasetUploadedHandler() {
			public void onDatasetUploaded(DatasetUploadedEvent event) {
				History.newItem("dataset?id=" + event.getDatasetUri());
			}
		});
		
		VerticalPanel dndPanel = new VerticalPanel();
		
		dndPanel.addStyleName("dragAndDrop");
		
		//final boolean dndEnabled = MMDB.getSessionPreference(DND_ENABLED_PREFERENCE) != null;
		final boolean dndEnabled = false; // FIXME debug
		
		final FlowPanel dndApplet = new FlowPanel();
		dndApplet.setHeight("60px");
		dndApplet.setWidth("60px");
		dndApplet.getElement().setId("dndAppletId");
		dndPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		dndPanel.add(dndApplet);
		final String disabledMsg = "Click here to enable drag and drop.";
		final String enabledMsg = "Drop files and folders here";
		final Label dndTooltip = new Label(dndEnabled ? enabledMsg
				: disabledMsg);
		dndPanel.add(dndTooltip);
		
		
		tableLayout.setWidget(1, 1, dndPanel);
		
		tableLayout.setWidget(1, 0, uploadWidget);

		if (dndEnabled) {
			dndApplet.removeStyleName("hidden");
			deployDndApplet(MMDB.getSessionState().getSessionKey());
		} else {
			dndApplet.addStyleName("hidden");
			dndTooltip.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					if(MMDB.getSessionState().getUsername() == null) {
						Window.confirm("Upload not permitted. Please log in");
					} else {
						boolean doit = true;
						if (!"true".equals(MMDB.getSessionPreference(DND_ENABLED_PREFERENCE))) {
							doit = Window
							.confirm("You will be asked to accept a security exception to allow our drag-and-drop upload tool to access your local files. If you don't wish to accept that security exception, press cancel.");
						}
						if (doit) {
							dndApplet.removeStyleName("hidden");
							deployDndApplet(MMDB.getSessionState().getSessionKey());
							dndTooltip.setText(enabledMsg);
							MMDB.setSessionPreference(DND_ENABLED_PREFERENCE,"true");
						}
					}
				}
			});
		}
		
		// call applet method
		Anchor callMethodOnApplet = new Anchor("Poke applet");
		callMethodOnApplet.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				doSomethingWithApplet(DOM.getElementById("dragdropApplet"), "foo");
			}
		});
		//tableLayout.setWidget(2, 0, callMethodOnApplet);
		
		// applet status
		appletStatusPanel = new VerticalPanel();
		// FIXME debug
		/*
		Anchor fakeButton = new Anchor("fake an upload (DEBUG!)");
		fakeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				ListDatasets list = new ListDatasets("http://purl.org/dc/elements/1.1/date",true,5,0,null);
				MMDB.dispatchAsync.execute(list, new AsyncCallback<ListDatasetsResult>() {
					public void onFailure(Throwable caught) {
					}
					public void onSuccess(ListDatasetsResult result) {
						for(DatasetBean ds : result.getDatasets()) {
							fileUploaded(ds.getUri());
						}
					}
				});
			}
		});
		appletStatusPanel.add(fakeButton);
		*/
		// FIXME end debug
		uploadedDatasetsTable = new FlexTable();
		appletStatusPanel.add(uploadedDatasetsTable);
		mainLayoutPanel.add(appletStatusPanel);
		
		// publish js methods outside of gwt code
		publishMethods();
	}
	
	/**
	 * Dummy method to show how to call a method in the applet.
	 * 
	 * @param applet
	 * @param parameter
	 */
	public static native void doSomethingWithApplet(Element applet, String param) /*-{
		if ((applet != null) && (applet.isActive)) {
			applet.poke();
		} else {
			$wnd.alert("Couldn't find applet " + applet);
		}
		
	}-*/;
	
	/**
	 * Publish local methods as page level js methods so that the applet can call them.
	 */
	private native void publishMethods() /*-{
   		$wnd.dndAppletPoke = @edu.illinois.ncsa.mmdb.web.client.ui.UploadPage::appletPoke();
   		$wnd.dndAppletFileUploaded = @edu.illinois.ncsa.mmdb.web.client.ui.UploadPage::fileUploaded(Ljava/lang/String;);
	}-*/;

	static Widget editableDatasetInfo(final DatasetBean ds) {
		FlexTable layout = new FlexTable();
		int row = 0;
		layout.setWidget(row, 0, new Label("Title:"));
		final EditableLabel titleLabel = new EditableLabel(ds.getTitle());
		titleLabel.addValueChangeHandler(new ValueChangeHandler<String>() {
			public void onValueChange(final ValueChangeEvent<String> event) {
				SetProperty change = new SetProperty(ds.getUri(), "http://purl.org/dc/elements/1.1/title", event.getValue());
				MMDB.dispatchAsync.execute(change, new AsyncCallback<SetPropertyResult>() {
					public void onFailure(Throwable caught) {
						titleLabel.cancel();
					}
					public void onSuccess(SetPropertyResult result) {
						titleLabel.setText(event.getValue());
					}
				});
			}
		});
		layout.setWidget(row, 1, titleLabel);
		row++;
		layout.setWidget(row, 0, new Label("Size:"));
		layout.setWidget(row, 1, new Label(TextFormatter.humanBytes(ds.getSize())));
		row++;
		layout.setWidget(row, 0, new Label("Type:"));
		layout.setWidget(row, 1, new Label(ds.getMimeType()));
		row++;
		layout.setWidget(row, 0, new Label("Date:"));
		layout.setWidget(row, 1, new Label(ds.getDate()+""));
		return layout;
	}
	
	/** Called by the applet after a file is uploaded. */
	public static void fileUploaded(String uri) {
		GWT.log("applet says "+uri+" uploaded");
		final int row = uploadedDatasetsTable.getRowCount();
		PreviewWidget preview = new PreviewWidget(uri, GetPreviews.SMALL, "dataset?id="+uri);
		uploadedDatasetsTable.setWidget(row, 0, preview);
		MMDB.dispatchAsync.execute(new GetDataset(uri), new AsyncCallback<GetDatasetResult>() {
			public void onFailure(Throwable caught) {
			}
			public void onSuccess(GetDatasetResult result) {
				uploadedDatasetsTable.setWidget(row, 1, editableDatasetInfo(result.getDataset()));
			}
		});
		TagsWidget tags = new TagsWidget(uri, MMDB.dispatchAsync, false);
		uploadedDatasetsTable.setWidget(row, 2, tags);
	}
	
	/**
	 * Called by the applet.
	 * 
	 * @param param
	 */
	public static void appletPoke() {
		GWT.log("Applet poked the page");
		appletStatusPanel.add(new Label("Applet poked the page"));
	}
	
	/**
	 * 
	 * @param credentials
	 */
	private native void deployDndApplet(String credentials) /*-{
		var attributes = {
			id:'dragdropApplet',
			MAYSCRIPT:'true',
		code:'edu.illinois.ncsa.mmdb.web.client.dnd.DropUploader',
		archive:'dnd/DropUploader-883.jar,dnd/lib/commons-codec-1.2.jar,dnd/lib/commons-httpclient-3.0.1.jar,dnd/lib/commons-httpclient-contrib-ssl-3.1.jar,dnd/lib/commons-logging-1.0.4.jar',
		width:60,
		height:60
		};
		var parameters = {
		jnlp_href: 'dropuploader.jnlp',
		statusPage: $wnd.document.URL,
		"credentials": credentials,
		background: "0xFFFFFF",
		};
		$wnd.deployJava.runApplet(attributes, parameters, '1.5');
		$wnd.document.getElementById('dndAppletId').innerHTML = $wnd.deployJava.getDocument();
	}-*/;
}
