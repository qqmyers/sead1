/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010, NCSA.  All rights reserved.
 *
 * Developed by:
 * Cyberenvironments and Technologies (CET)
 * http://cet.ncsa.illinois.edu/
 *
 * National Center for Supercomputing Applications (NCSA)
 * http://www.ncsa.illinois.edu/
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the names of CET, University of Illinois/NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *******************************************************************************/
/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.UploadWidget;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUploadedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUploadedHandler;
import edu.illinois.ncsa.mmdb.web.client.presenter.BatchOperationPresenter;
import edu.illinois.ncsa.mmdb.web.client.presenter.HelpPresenter;
import edu.illinois.ncsa.mmdb.web.client.presenter.UploadStatusPresenter;
import edu.illinois.ncsa.mmdb.web.client.view.BatchOperationView;
import edu.illinois.ncsa.mmdb.web.client.view.HelpDialogView;
import edu.illinois.ncsa.mmdb.web.client.view.UploadStatusView;

/**
 * A standalone page to upload files.
 * 
 * @author Luigi Marini
 * 
 */
public class UploadPage extends Page {

    private static final String            TITLE                  = "Upload";
    private UploadWidget                   uploadWidget;
    private FlexTable                      tableLayout;
    private static VerticalPanel           appletStatusPanel;
    private static UploadStatusPresenter   uploadStatusPresenter;
    private static BatchOperationPresenter batchOperationPresenter;
    private static BatchOperationView      batchOperationView;
    private static UploadStatusView        uploadStatusView;
    Timer                                  safariWakeupTimer;

    public static final String             DND_ENABLED_PREFERENCE = "dndAppletEnabled";

    public UploadPage() {
        super();
        setPageTitle(TITLE);
    }

    public UploadPage(MyDispatchAsync dispatchasync) {
        super(TITLE, dispatchasync);
    }

    @Override
    public void layout() {

        tableLayout = new FlexTable() {
            @Override
            protected void onDetach() {
                super.onDetach();
                if (uploadStatusPresenter != null) {
                    uploadStatusPresenter.unbind();
                }
                if (batchOperationPresenter != null) {
                    batchOperationPresenter.unbind();
                }
            }
        };

        tableLayout.addStyleName("uploadPageLayout");

        mainLayoutPanel.add(tableLayout);

        VerticalPanel singleUpload = new VerticalPanel();
        final HorizontalPanel hp = new HorizontalPanel();
        hp.add(new Label("Select a file you want to upload:"));
        Image helpButton = new Image("./images/help-browser.png");
        helpButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                HelpDialogView view = new HelpDialogView("Upload help");
                HelpPresenter presenter = new HelpPresenter(view);
                presenter.bind();
                view.setWidth("300px");
                view.setPopupPosition(360, 70);
                String helpText = "To upload a single file, use the file chooser on the left. " +
                        "To upload multiple files or a folder, click in the box to the right to load the upload applet. " +
                        "Once the applet is loaded, you can drag a folder or multiple files from your desktop and drop them on " +
                        "the disk icon to upload. As files upload you will see information about the upload progress below.";
                presenter.addContent(new Label(helpText));
            }
        });
        pageTitle.addEast(helpButton);
        singleUpload.add(hp);

        uploadWidget = new UploadWidget(false);
        uploadWidget.addDatasetUploadedHandler(new DatasetUploadedHandler() {
            public void onDatasetUploaded(DatasetUploadedEvent event) {
                History.newItem("dataset?id=" + event.getDatasetUri());
            }
        });
        singleUpload.add(uploadWidget);
        tableLayout.setWidget(0, 0, singleUpload);

        VerticalPanel dndPanel = new VerticalPanel();

        final boolean dndEnabled = MMDB.getSessionPreference(DND_ENABLED_PREFERENCE) != null;
        final String disabledMsg = "Click here to upload multiple files. You may be asked by your web browser to accept a security exception.";
        final String enabledMsg = "Drop files and folders here";
        final Label dndTooltip = new Label(dndEnabled ? enabledMsg
                : disabledMsg);

        final FlowPanel dndApplet = new FlowPanel() {
            protected void onAttach() {
                // TODO Auto-generated method stub
                super.onAttach();
                if (dndEnabled) {
                    removeStyleName("hidden");
                    dndTooltip.removeStyleName("dndTooltip");
                    deployDndApplet(MMDB.getSessionState().getSessionKey());
                } else {
                    addStyleName("hidden");
                    dndTooltip.addClickHandler(new ClickHandler() {
                        public void onClick(ClickEvent event) {
                            if (MMDB.getSessionState().getCurrentUser() == null) {
                                Window.confirm("Upload not permitted. Please log in");
                            } else {
                                removeStyleName("hidden");
                                dndTooltip.removeStyleName("dndTooltip");
                                deployDndApplet(MMDB.getSessionState().getSessionKey());
                                dndTooltip.setText(enabledMsg);
                                MMDB.setSessionPreference(DND_ENABLED_PREFERENCE, "true");
                            }
                        }
                    });
                }
            }
        };
        dndApplet.setWidth("150px");
        dndApplet.setHeight("100px");
        dndApplet.getElement().setId("dndAppletId");
        dndPanel.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
        dndApplet.addStyleName("dragAndDrop");
        dndPanel.addStyleName("dndContainer");
        dndPanel.add(dndApplet);
        dndTooltip.addStyleName("dndTooltip");
        dndPanel.add(dndTooltip);

        Label or = new Label("OR");
        or.addStyleName("uploadOrLabel");
        tableLayout.setWidget(0, 1, or);
        tableLayout.setWidget(0, 2, dndPanel);
        tableLayout.getCellFormatter().setHorizontalAlignment(0, 0, HasAlignment.ALIGN_CENTER);
        tableLayout.getCellFormatter().addStyleName(0, 0, "uploadPageLargeCell");
        tableLayout.getCellFormatter().addStyleName(0, 2, "uploadPageLargeCell");
        tableLayout.getCellFormatter().setHorizontalAlignment(0, 2, HasAlignment.ALIGN_CENTER);

        // wake the applet up periodically, so it doesn't block on javascript calls
        safariWakeupTimer = new Timer() {
            public void run() {
                pokeApplet(DOM.getElementById("dragdropApplet"));
            }
        };
        safariWakeupTimer.scheduleRepeating(500);

        // batch actions
        batchOperationView = new BatchOperationView();
        batchOperationView.addStyleName("hidden");
        batchOperationPresenter = new BatchOperationPresenter(MMDB.dispatchAsync, MMDB.eventBus, batchOperationView);
        batchOperationPresenter.bind();
        mainLayoutPanel.add(batchOperationView);

        // applet status
        appletStatusPanel = new VerticalPanel();

        uploadStatusView = new UploadStatusView();
        uploadStatusPresenter = new UploadStatusPresenter(MMDB.dispatchAsync, MMDB.eventBus, uploadStatusView);
        uploadStatusPresenter.bind();

        appletStatusPanel.add(uploadStatusView);
        mainLayoutPanel.add(appletStatusPanel);

        // publish js methods outside of gwt code
        publishMethods();
    }

    /**
     * "poke" the applet. this keeps it awake in WebKit browsers where
     * javascript callbacks can block it
     * 
     * @param applet
     * @param parameter
     */
    public static native void pokeApplet(Element applet) /*-{
        if ((applet != null) && (applet.isActive())) {
        applet.poke();
        }
    }-*/;

    /**
     * Publish local methods as page level js methods so that the applet can
     * call them.
     */
    private native void publishMethods() /*-{
        $wnd.dndAppletPoke = @edu.illinois.ncsa.mmdb.web.client.ui.UploadPage::appletPoke();
        $wnd.dndAppletFileDropped = @edu.illinois.ncsa.mmdb.web.client.ui.UploadPage::fileDropped(Ljava/lang/String;Ljava/lang/String;);
        $wnd.dndAppletFileUploaded = @edu.illinois.ncsa.mmdb.web.client.ui.UploadPage::fileUploaded(Ljava/lang/String;);
        $wnd.dndAppletProgress = @edu.illinois.ncsa.mmdb.web.client.ui.UploadPage::fileProgress(I);
    }-*/;

    // on applet callbacks we need to post-process values with an identity transformations to work around applet-to-Javascript issues

    /** Called by the applet after a file is uploaded. */
    public static void fileUploaded(String uriUploaded) {
        String uri = uriUploaded + ""; // identity transform required, do not remove
        GWT.log("applet says " + uri + " uploaded");
        uploadStatusPresenter.onComplete(uri);
    }

    /** Called by the applet for each file dropped */
    public static void fileDropped(String filename, String sizeString) {
        batchOperationView.removeStyleName("hidden");
        batchOperationView.addStyleName("titlePanelRightElement");
        uploadStatusPresenter.onDropped(filename + "", sizeString + ""); // identity transforms required, do not remove
    }

    /**
     * Called by the applet to report progress on current batch
     * 
     * @param percent
     */
    public static void fileProgress(int percent) {
        uploadStatusPresenter.onProgress(percent + 0); // identity transform required, do not remove
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
        archive:'dnd/DropUploader-1545.jar,dnd/lib/commons-codec-1.2.jar,dnd/lib/commons-httpclient-3.0.1.jar,dnd/lib/commons-httpclient-contrib-ssl-3.1.jar,dnd/lib/commons-logging-1.0.4.jar',
        width:150,
        height:100
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

    @Override
    protected void onDetach() {
        super.onDetach();
        if (safariWakeupTimer != null) {
            safariWakeupTimer.cancel();
        }
    }

}
