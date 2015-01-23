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

import net.customware.gwt.dispatch.client.DispatchAsync;

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
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.UploadWidget;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetRelationship;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetRelationshipResult;
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
    private boolean                        showhtml5;

    private static String                  ancestor;
    private static String                  derivationPredicate;
    private final static String            versionPredicate       = "http://www.w3.org/ns/prov#wasRevisionOf";
    private final static String            derivedFromPredicate   = "http://www.w3.org/ns/prov#wasDerivedFrom";
    private static UploadPage              activeUploadPage;

    public static final String             DND_ENABLED_PREFERENCE = "dndAppletEnabled";

    /* DispatchAysnc required? UploadPage() appears unused
    public UploadPage() {
        super();
        setPageTitle(TITLE);
        ancestor = null;
    }
    */

    public UploadPage(DispatchAsync dispatchasync) {
        super(TITLE, dispatchasync);
        ancestor = null;
        activeUploadPage = this;
    }

    @Override
    public void layout() {
        showhtml5 = true;
        if (getUserAgent().contains("msie")) {
            if (getUserAgent().matches(".*msie [0-9].[0-9].*")) {
                showhtml5 = false;
            }
        }
        if (getUserAgent().contains("opera") || getUserAgent().contains("firefox/23.0")) {
            showhtml5 = false;
        }

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

        final VerticalPanel singleUpload = new VerticalPanel();
        final HorizontalPanel hp = new HorizontalPanel();
        Label fileUploadLabel = new Label("Select files you want to upload:");
        fileUploadLabel.addStyleName("importTitle");
        hp.add(fileUploadLabel);
        //Only shown eith DnD applet?
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
        singleUpload.add(hp);

        uploadWidget = new UploadWidget(false);
        uploadWidget.addDatasetUploadedHandler(new DatasetUploadedHandler() {
            public void onDatasetUploaded(DatasetUploadedEvent event) {
                History.newItem("dataset?id=" + event.getDatasetUri());
            }
        });
        singleUpload.add(uploadWidget);

        final VerticalPanel dndPanel = new VerticalPanel();

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

        // HTML5 Upload Widgets
        VerticalPanel html5Panel = new VerticalPanel();
        HTML html5Upload = new HTML();
        html5Upload.setHTML("<div id='box'><div id='drop'><br><br>Drag and drop files here!</div></div> <div id='list'></div>");
        html5Panel.add(html5Upload);

        VerticalPanel html5Form = new VerticalPanel();
        HTML formMultiple = new HTML();
        formMultiple.setHTML("<br><br><input type='file' id='files' name='files[]' multiple='multiple' />");
        html5Form.add(hp);
        html5Form.add(formMultiple);

        final HorizontalPanel switchUploader = new HorizontalPanel();
        switchUploader.addStyleName("pagingButton");
        Label basicLabel = new Label("For uploading large numbers of files, contact SEAD about our desktop Upload tool.");
        /*
         * Anchor basicUploader = new Anchor("Java uploader.");
        basicUploader.addStyleName("addTagsLink");
        basicUploader.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                tableLayout.setWidget(0, 0, singleUpload);
                tableLayout.setWidget(0, 2, dndPanel);
                tableLayout.remove(switchUploader);
            }
        });
        */
        switchUploader.add(basicLabel);
        //switchUploader.add(basicUploader);

        Label or = new Label("OR");
        or.addStyleName("uploadOrLabel");
        tableLayout.setWidget(0, 1, or);

        final FormPanel form = new FormPanel();
        form.setAction("./resteasy/datasets/copy");
        form.setMethod(FormPanel.METHOD_POST);
        final VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        form.setWidget(panel);

        panel.add(new Label("")); //Spacer
        Label importLabel = new Label("Import Open Data from another project:");
        importLabel.addStyleName("importTitle");
        panel.add(importLabel);

        // Create a TextBox, giving it a name so that it will be submitted.
        final TextBox tb = new TextBox();
        tb.setName("url");
        tb.setWidth("100%");
        panel.add(tb);
        Label details = new Label("[To import - copy the URL of the datapage in the box above. (Data must be publicly visible at this URL.)]");
        details.addStyleName("smallText");
        panel.add(details);

        // Add a 'submit' button.
        panel.add(new Button("Copy Dataset", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (tb.getText().contains("dataset?id=")) {
                    form.submit();
                } else {
                    Window.alert("Not a valid Medici URL");
                }
            }
        }));

        panel.add(new Label("")); //Spacer
        Label captureLabel = new Label("Capture Data From the Web");
        captureLabel.addStyleName("importTitle");
        panel.add(captureLabel);
        Label captureDetails = new Label("To capture: save the following link as a bookmark in your browser. Then, when you're viewing the data, " +
                "or on a page with a download link for it, click the bookmark.  Note: The data must be public.");
        captureDetails.addStyleName("smallText");
        panel.add(captureDetails);

        /* Uncompressed script: Loads 4 files, waits for jquery and main import script to load and calls createPanel to get started:
         * 
         * (function(){
         *    var headID=document.getElementsByTagName("head")[0];
         *    var script=document.createElement("script");
         *    script.type="text/javascript";
         *    script.src="//code.jquery.com/jquery-1.8.1.min.js";
         *    headID.appendChild(script);
         *    
         *    var cssNode=document.createElement("link");
         *    cssNode.type="text/css";cssNode.rel="stylesheet";
         *    cssNode.href="<Your server>css/seadimport.css";
         *    cssNode.media="screen";
         *    headID.appendChild(cssNode);
         *    
         *    var bootstrapNode=document.createElement("link");
         *    bootstrapNode.type="text/css";
         *    bootstrapNode.rel="stylesheet";
         *    bootstrapNode.href="<Your server>css/bootstrap.sead-scope.css";
         *    bootstrapNode.media="screen";
         *    headID.appendChild(bootstrapNode);
         *    
         *    var attemptCount=0;
         *    var undef="undefined";
         *    function waitForJQuery() {
         *       attemptCount++;
         *       if ((typeof jQuery) != undef) {
         *         init();
         *         return;
         *       }
         *       if (attemptCount < 100) {
         *         setTimeout(waitForJQuery, 100);
         *       }
         *       return;
         *    }
         *    
         *    waitForJQuery();
         *    
         *    function init() { 
         *      $.getScript("<Your server>js/seadimport.js", function(){
         *         createPanel("<Your server>","<Project Name");
         *      });
         *    }
         *  })();
         */
        panel.add(new Anchor(MMDB._projectName + " Data Import",

                "javascript:(function(){function o(){i++;if(typeof jQuery!=s){u();return}if(i<100){setTimeout(o,100)}return}" +
                        "function u(){$.getScript(\"" + GWT.getHostPageBaseURL() + "js/seadimport.js\",function(){" +
                        "createPanel(\"" + GWT.getHostPageBaseURL() + "\",\"" + MMDB._projectName + "\",t,n,r);})}var e=document.getElementsByTagName(\"head\")[0];" +
                        "var t=document.createElement(\"script\");t.type=\"text/javascript\";t.src=\"//code.jquery.com/jquery-1.8.1.min.js\";" +
                        "e.appendChild(t);var n=document.createElement(\"link\");n.type=\"text/css\";n.rel=\"stylesheet\";" +
                        "n.href=\"" + GWT.getHostPageBaseURL() + "css/seadimport.css\";n.media=\"screen\";e.appendChild(n);" +
                        "var r=document.createElement(\"link\");r.type=\"text/css\";r.rel=\"stylesheet\";" +
                        "r.href=\"" + GWT.getHostPageBaseURL() + "css/bootstrap.sead-scope.css\";r.media=\"screen\";" +
                        "e.appendChild(r);var i=0;var s=\"undefined\";o()})()"));

        // Add an event handler to the form.
        form.addSubmitCompleteHandler(new SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(SubmitCompleteEvent event) {
                String id = tb.getText().replaceAll(".*dataset\\?id=", "");
                tb.setText("");
                panel.add(new Hyperlink(id, "dataset?id=" + id));
            }
        });

        if (showhtml5) {
            tableLayout.setWidget(0, 0, html5Form);
            tableLayout.setWidget(0, 2, html5Panel);
            tableLayout.setWidget(1, 0, switchUploader);
            tableLayout.setWidget(2, 0, form);
        } else {
            pageTitle.addEast(helpButton);
            tableLayout.setWidget(0, 0, singleUpload);
            tableLayout.setWidget(0, 2, dndPanel);
            tableLayout.setWidget(2, 0, form);
        }

        tableLayout.getCellFormatter().setHorizontalAlignment(0, 0, HasAlignment.ALIGN_CENTER);
        tableLayout.getCellFormatter().addStyleName(0, 0, "uploadPageLargeCell");
        tableLayout.getCellFormatter().setHorizontalAlignment(1, 0, HasAlignment.ALIGN_CENTER);
        tableLayout.getCellFormatter().addStyleName(1, 0, "uploadPageLargeCell");
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
        batchOperationPresenter = new BatchOperationPresenter(dispatchAsync, MMDB.eventBus, batchOperationView, false);
        batchOperationPresenter.bind();
        mainLayoutPanel.add(batchOperationView);

        // applet status
        appletStatusPanel = new VerticalPanel();

        uploadStatusView = new UploadStatusView(dispatchAsync);
        uploadStatusPresenter = new UploadStatusPresenter(dispatchAsync, MMDB.eventBus, uploadStatusView);
        uploadStatusPresenter.bind();

        appletStatusPanel.add(uploadStatusView);
        mainLayoutPanel.add(appletStatusPanel);

        // publish js methods outside of gwt code
        publishMethods();
    }

    private final native void initUploader() /*-{
		// initialize HTML5 uploader
		$wnd.initUploader();
    }-*/;

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
        $wnd.dndAppletFileUploaded = @edu.illinois.ncsa.mmdb.web.client.ui.UploadPage::fileUploaded(Ljava/lang/String;Ljava/lang/String;);
        $wnd.dndAppletProgressIndex = @edu.illinois.ncsa.mmdb.web.client.ui.UploadPage::fileProgressIndex(II);
    }-*/;

    // on applet callbacks we need to post-process values with an identity transformations to work around applet-to-Javascript issues

    /** Called by the applet after a file is uploaded. */
    public static void fileUploaded(String uriUploaded, String offset) {
        String uri = uriUploaded + ""; // identity transform required, do not remove
        GWT.log("applet says " + uri + " uploaded");
        addDerviationRelationshipIfNeeded(uri);
        uploadStatusPresenter.onComplete(uri, Integer.parseInt(offset + "")); // identity transform required, do not remove
    }

    /** Called by the applet for each file dropped */
    public static void fileDropped(String filename, String sizeString) {
        batchOperationView.removeStyleName("hidden");
        batchOperationView.addStyleName("titlePanelRightElement");
        uploadStatusPresenter.onDropped(filename + "", sizeString + ""); // identity transforms required, do not remove
    }

    /**
     * Called by the html5 upload widget to report progress on current batch
     * 
     * @param percent
     *            , index
     */
    public static void fileProgressIndex(int index, int percent) {
        uploadStatusPresenter.onProgressIndex(index, percent + 0); // identity transform required, do not remove
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
		$wnd.LazyLoad
				.js(
						'js/deployJava.js',
						function() {
							var attributes = {
								id : 'dragdropApplet',
								MAYSCRIPT : 'true',
								code : 'edu.illinois.ncsa.mmdb.web.client.dnd.DropUploader',
								archive : 'dnd/DropUploader-1801.jar,dnd/lib/commons-codec-1.2.jar,dnd/lib/commons-httpclient-3.0.1.jar,dnd/lib/commons-httpclient-contrib-ssl-3.1.jar,dnd/lib/commons-logging-1.0.4.jar',
								width : 150,
								height : 100
							};
							var parameters = {
								jnlp_href : 'dropuploader.jnlp',
								statusPage : $wnd.document.URL,
								"credentials" : credentials,
								background : "0xFFFFFF",
							};
							$wnd.deployJava.runApplet(attributes, parameters,
									'1.5');
							$wnd.document.getElementById('dndAppletId').innerHTML = $wnd.deployJava
									.getDocument();
						});
    }-*/;

    @Override
    protected void onDetach() {
        super.onDetach();
        if (safariWakeupTimer != null) {
            safariWakeupTimer.cancel();
        }
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        if (showhtml5) {
            initUploader();
        }
    }

    public static native String getUserAgent() /*-{
		return navigator.userAgent.toLowerCase();
    }-*/;

    public void deriveFromDataset(DispatchAsync service, String datasetUri) {
        setPageTitle(TITLE + " Derived Data");

        ancestor = datasetUri;

        RadioButton version = new RadioButton("relationGroup", "A New Version");
        RadioButton derived = new RadioButton("relationGroup", "Derived Data Product(s)");

        // Check 'version' by default.
        version.setValue(true);
        derivationPredicate = versionPredicate;

        version.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> e) {
                if (e.getValue() == true) {
                    derivationPredicate = versionPredicate;
                }
            }

        });

        derived.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> e) {
                if (e.getValue() == true) {
                    derivationPredicate = derivedFromPredicate;
                }
            }

        });

        Label rel = new Label("The new data being uploaded is");
        Label target = new Label(" of Dataset: ");

        FlowPanel fp = new FlowPanel();
        fp.setStyleName("callout");

        final HorizontalPanel dp = new HorizontalPanel();

        VerticalPanel relPanel = new VerticalPanel();
        relPanel.setStyleName("derivechoice");
        relPanel.add(rel);
        relPanel.add(version);
        relPanel.add(derived);
        relPanel.add(target);

        dp.add(relPanel);
        fp.add(dp);

        mainLayoutPanel.insert(fp, 1);
        service.execute(new GetDataset(datasetUri, MMDB.getUsername()), new AsyncCallback<GetDatasetResult>() {

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error getting dataset", null);
            }

            @Override
            public void onSuccess(GetDatasetResult result) {
                //FixMe - use DynamicTableView with 1 row?
                dp.add(new DatasetInfoWidget(result.getDataset(), dispatchAsync));
            }
        });

    }

    //Adds a derivation relationship for the newly uploaded data if there's an ancestor 
    //(i.e. called from dataset page  with a #upload?id=<ancestor> history tag)
    private static void addDerviationRelationshipIfNeeded(final String uri) {
        if (ancestor != null) {
            getDispatchAsync().execute(new SetRelationship(uri, derivationPredicate, ancestor, MMDB.getUsername()),
                    new AsyncCallback<SetRelationshipResult>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            GWT.log("Error creating relationship", caught);
                            Window.alert("Data uploaded but could not write derivation relationship: " + uri + " " + derivationPredicate + " " + ancestor);
                        }

                        public void onSuccess(SetRelationshipResult result) {
                        }
                    });
        }
    }

    private static DispatchAsync getDispatchAsync() {
        return activeUploadPage.dispatchAsync;
    }
}
