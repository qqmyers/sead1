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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
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
import edu.illinois.ncsa.mmdb.web.client.presenter.HelpPresenter;
import edu.illinois.ncsa.mmdb.web.client.view.HelpDialogView;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * A standalone page to upload files.
 * 
 * @author Luigi Marini
 * 
 */
public class UploadPage extends Page {

    private static final String  TITLE                  = "Upload";
    private UploadWidget         uploadWidget;
    private FlexTable            tableLayout;
    private static VerticalPanel appletStatusPanel;
    private static FlexTable     uploadedDatasetsTable;
    Timer                        safariWakeupTimer;

    public static final String   DND_ENABLED_PREFERENCE = "dndAppletEnabled";

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

        final HorizontalPanel hp = new HorizontalPanel();
        hp.add(new Label("Select the file you want to upload or click and drag a file or folder. "));
        Image helpButton = new Image("./images/help-browser.png");
        helpButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                HelpDialogView view = new HelpDialogView("Upload help");
                HelpPresenter presenter = new HelpPresenter(view);
                presenter.bind();
                presenter.addContent(new Label("[put help content here]"));
            }
        });
        hp.add(helpButton);
        tableLayout.setWidget(0, 0, hp);

        tableLayout.getFlexCellFormatter().setColSpan(0, 0, 2);

        uploadWidget = new UploadWidget(false);
        uploadWidget.addDatasetUploadedHandler(new DatasetUploadedHandler() {
            public void onDatasetUploaded(DatasetUploadedEvent event) {
                History.newItem("dataset?id=" + event.getDatasetUri());
            }
        });

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
                    deployDndApplet(MMDB.getSessionState().getSessionKey());
                } else {
                    addStyleName("hidden");
                    dndTooltip.addClickHandler(new ClickHandler() {
                        public void onClick(ClickEvent event) {
                            if (MMDB.getSessionState().getCurrentUser() == null) {
                                Window.confirm("Upload not permitted. Please log in");
                            } else {
                                removeStyleName("hidden");
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
        dndPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        dndApplet.addStyleName("dragAndDrop");
        dndPanel.addStyleName("dndContainer");
        dndPanel.add(dndApplet);
        dndPanel.add(dndTooltip);

        tableLayout.setWidget(1, 1, dndPanel);

        tableLayout.setWidget(1, 0, uploadWidget);

        // wake the applet up periodically, so it doesn't block on javascript calls
        safariWakeupTimer = new Timer() {
            public void run() {
                pokeApplet(DOM.getElementById("dragdropApplet"));
            }
        };
        safariWakeupTimer.scheduleRepeating(500);

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
     * "poke" the applet. this keeps it awake in WebKit browsers where
     * javascript callbacks can block it
     * 
     * @param applet
     * @param parameter
     */
    public static native void pokeApplet(Element applet) /*-{
        if ((applet != null) && (applet.isActive)) {
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
        layout.setWidget(row, 1, new Label(ds.getDate() + ""));
        return layout;
    }

    // FIXME encapsulate these two state variables into a delegate
    static int         nUploaded          = 0;
    static ProgressBar currentProgressBar = null;

    /** Called by the applet after a file is uploaded. */
    public static void fileUploaded(final String uri) {
        GWT.log("applet says " + uri + " uploaded");
        showFileUploaded(uri);
    }

    public static void showFileUploaded(final String uri) {
        final int row = nUploaded;
        nUploaded++;
        while (uploadedDatasetsTable.getRowCount() < nUploaded) {
            uploadedDatasetsTable.insertRow(uploadedDatasetsTable.getRowCount());
        }
        Anchor anchor = new Anchor("View", "#dataset?id=" + uri);
        anchor.setTarget("_blank");
        uploadedDatasetsTable.setWidget(row, 0, anchor);
        uploadedDatasetsTable.setWidget(row, 2, new Label("Complete"));
        currentProgressBar = null;
        showUploadedDatasetInfo(uri, row);
    }

    public static void showUploadedDatasetInfo(final String uri, final int row) {
        MMDB.dispatchAsync.execute(new GetDataset(uri), new AsyncCallback<GetDatasetResult>() {
            public void onFailure(Throwable caught) {
                Window.alert("fileUploaded dispatch failed: " + caught.getMessage()); // FIXME
            }

            public void onSuccess(GetDatasetResult result) {
                PreviewWidget preview = new PreviewWidget(uri, GetPreviews.SMALL, "dataset?id=" + uri);
                uploadedDatasetsTable.setWidget(row, 0, preview);
                uploadedDatasetsTable.setWidget(row, 1, editableDatasetInfo(result.getDataset()));
                TagsWidget tags = new TagsWidget(uri, MMDB.dispatchAsync, false);
                uploadedDatasetsTable.setWidget(row, 2, tags);
                Anchor hideAnchor = new Anchor("Hide");
                hideAnchor.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        uploadedDatasetsTable.getRowFormatter().addStyleName(row, "hidden");
                    }
                });
                uploadedDatasetsTable.setWidget(row, 3, hideAnchor);
            }
        });
    }

    /** Called by the applet for each file dropped */
    public static void fileDropped(String filename, String sizeString) {
        nUploaded = 0;
        GWT.log("applet says " + filename + " dropped");
        //appletStatusPanel.add(new Label("applet says " + filename + " (" + sizeString + ") dropped")); // FIXME debug
        final int row = uploadedDatasetsTable.getRowCount();
        if (row == 0) {
            uploadedDatasetsTable.insertRow(0);
        }
        Image image = new Image(PreviewWidget.GRAY_URL.get(GetPreviews.SMALL));
        uploadedDatasetsTable.setWidget(row, 0, image);
        int size = -1;
        try {
            size = Integer.parseInt(sizeString);
        } catch (NumberFormatException x) {
            // fall through
        }
        uploadedDatasetsTable.setWidget(row, 1, new Label("Uploading \"" + filename + "\" (" + TextFormatter.humanBytes(size) + ") ..."));
    }

    /**
     * Called by the applet to report progress on current batch
     * 
     * @param percent
     */
    public static void fileProgress(int percent) {
        //appletStatusPanel.add(new Label("applet says progress is " + percent)); // FIXME debug
        if (currentProgressBar == null) {
            currentProgressBar = new ProgressBar(1);
            uploadedDatasetsTable.setWidget(nUploaded, 2, currentProgressBar);
        }
        currentProgressBar.setProgress(percent);
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
        archive:'dnd/DropUploader-1006.jar,dnd/lib/commons-codec-1.2.jar,dnd/lib/commons-httpclient-3.0.1.jar,dnd/lib/commons-httpclient-contrib-ssl-3.1.jar,dnd/lib/commons-logging-1.0.4.jar',
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
