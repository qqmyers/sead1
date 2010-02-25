/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.UploadWidget;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUploadedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUploadedHandler;

/**
 * A standalone page to upload files.
 * 
 * @author Luigi Marini
 *
 */
public class UploadPage extends Page {

	private static final String TITLE = "Upload";
	private UploadWidget uploadWidget;
	private boolean dndEnabled;
	private FlexTable tableLayout;

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
		
		final FlowPanel dndApplet = new FlowPanel();
		dndApplet.setHeight("60px");
		dndApplet.setWidth("60px");
		dndApplet.getElement().setId("dndAppletId");
		dndPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		dndPanel.add(dndApplet);
		if (!dndEnabled) {
			dndApplet.addStyleName("hidden");
		}
		final String disabledMsg = "Click here to enable drag and drop.";
		final String enabledMsg = "Drop files and folders here";
		final Label dndTooltip = new Label(dndEnabled ? enabledMsg
				: disabledMsg);
		dndPanel.add(dndTooltip);
		
		
		tableLayout.setWidget(1, 1, dndPanel);
		
		tableLayout.setWidget(1, 0, uploadWidget);

		if (dndEnabled) {
			dndApplet.removeStyleName("hidden");
			deployDndApplet(MMDB.sessionKey);
		} else {
			dndTooltip.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					if(MMDB.sessionKey == null) {
						Window.confirm("Upload not permitted. Please log in");
					} else {
						boolean doit = true;
						if (!dndEnabled) {
							doit = Window
							.confirm("You will be asked to accept a security exception to allow our drag-and-drop upload tool to access your local files. If you don't wish to accept that security exception, press cancel.");
						}
						if (doit) {
							dndApplet.removeStyleName("hidden");
							deployDndApplet(MMDB.sessionKey);
							dndTooltip.setText(enabledMsg);
							dndEnabled = true;
						}
					}
				}
			});
		}
	}
	
	/**
	 * 
	 * @param credentials
	 */
	private native void deployDndApplet(String credentials) /*-{
		var attributes = {
		code:'edu.illinois.ncsa.mmdb.web.client.dnd.DropUploader',
		archive:'dnd/DropUploader-490.jar,dnd/lib/commons-codec-1.2.jar,dnd/lib/commons-httpclient-3.0.1.jar,dnd/lib/commons-httpclient-contrib-ssl-3.1.jar,dnd/lib/commons-logging-1.0.4.jar',
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