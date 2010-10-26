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

package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.PreviewDocumentBean;
import edu.uiuc.ncsa.cet.bean.PreviewImageBean;
import edu.uiuc.ncsa.cet.bean.PreviewPyramidBean;
import edu.uiuc.ncsa.cet.bean.PreviewThreeDimensionalBean;
import edu.uiuc.ncsa.cet.bean.PreviewVideoBean;
import edu.uiuc.ncsa.cet.bean.preview.PreviewAudioBean;

/**
 * Show one or more preview related to each PreviewBean
 * 
 * @author Luigi Marini, Luis Mendez
 * 
 */

public class PreviewPanel extends Composite {

    /** maximum width of a preview image */
    private static final long   MAX_WIDTH       = 600;
    /** maximum height of a preview image */
    private static final long   MAX_HEIGHT      = 600;

    private AbsolutePanel       previewPanel;
    private PreviewBean         currentPreview;
    //private final FlowPanel     leftColumn;

    public int                  getPolys;
    public double               getVerts;

    private static final String BLOB_URL        = "api/image/";
    private static final String NOREFERENCE_URL = "api/image/";
    private static final String EXTENSION_URL   = "api/dataset/";
    private static final String PYRAMID_URL     = "pyramid/";

    public PreviewPanel() {
    }

    public void unload() {
        hideSeadragon();
        hideWebGL();
        hideHTML5();
    }

    //@Override
    protected void onUnload() {
        super.onUnload();
        unload();
    }

    //TODO Clean up code to allow two different instances:
    //     1) Multiple beans : one preview per bean
    //     2) One bean : multiple previews per bean
    public void drawPreview(final GetDatasetResult result, FlowPanel leftColumn, String uri) {
        // find best preview bean
        // best preview is that that is closest to width of column
        int maxwidth = leftColumn.getOffsetWidth();

        // list of known best previews
        PreviewImageBean bestImage = null;
        PreviewVideoBean bestVideo = null;
        PreviewThreeDimensionalBean best3D = null;
        PreviewDocumentBean bestDoc = null;
        PreviewPyramidBean bestPyramid = null;
        PreviewAudioBean bestAudio = null;

        // loop through all previews finding the best options
        for (PreviewBean pb : result.getPreviews() ) {
            if (pb instanceof PreviewImageBean) {
                PreviewImageBean pib = (PreviewImageBean) pb;
                if (bestImage == null) {
                    bestImage = pib;
                } else if (Math.abs(maxwidth - pib.getWidth()) < Math.abs(maxwidth - bestImage.getWidth())) {
                    bestImage = pib;
                }
            } else if (pb instanceof PreviewVideoBean) {
                PreviewVideoBean pvb = (PreviewVideoBean) pb;
                if (bestVideo == null) {
                    bestVideo = pvb;
                } else if (Math.abs(maxwidth - pvb.getWidth()) < Math.abs(maxwidth - bestVideo.getWidth())) {
                    bestVideo = pvb;
                }
            } else if (pb instanceof PreviewAudioBean) {
                bestAudio = (PreviewAudioBean) pb;
            } else if (pb instanceof PreviewThreeDimensionalBean) {
                PreviewThreeDimensionalBean p3Db = (PreviewThreeDimensionalBean) pb;

                if (best3D == null) {
                    best3D = p3Db;
                } else if (Math.abs(maxwidth - p3Db.getWidth()) < Math.abs(maxwidth - best3D.getWidth())) {
                    best3D = p3Db;
                }
            } else if (pb instanceof PreviewDocumentBean) {
                PreviewDocumentBean pdb = (PreviewDocumentBean) pb;

                if (bestDoc == null) {
                    bestDoc = pdb;
                } else if (Math.abs(maxwidth - pdb.getWidth()) < Math.abs(maxwidth - bestDoc.getWidth())) {
                    bestDoc = pdb;
                }
            } else if (pb instanceof PreviewPyramidBean) {
                bestPyramid = (PreviewPyramidBean) pb;
            } else {
                GWT.log("Unknown preview type : " + pb);
            }
        }

        // preview options
        final FlowPanel previewsPanel = new FlowPanel();
        previewsPanel.addStyleName("datasetActions");
        leftColumn.add(previewsPanel);

        // space for the preview/video/zoom
        currentPreview = null;
        previewPanel = new AbsolutePanel();
        previewPanel.addStyleName("previewPanel");
        leftColumn.add(previewPanel);

        // add previews, this order is important, add beans in order
        // in case of multiple options first bean listed will win.
        PreviewBean shown = null;

        if (bestDoc != null) {
            shown = addAnchor(bestDoc, "Text", previewsPanel, shown);
        }
        if (best3D != null) {
            shown = addAnchor(best3D, "3D (java)", previewsPanel, 0, shown);
            shown = addAnchor(best3D, "3D (WebGL)", previewsPanel, 1, shown);
            shown = addAnchor(best3D, "3D (HTML5)", previewsPanel, 2, shown);
        }
        if (bestVideo != null) {
            shown = addAnchor(bestVideo, "Video", previewsPanel, shown);
        }
        if (bestAudio != null) {
            shown = addAnchor(bestAudio, "Audio", previewsPanel, shown);
        }
        if (bestImage != null) {
            shown = addAnchor(bestImage, "Image", previewsPanel, shown);
        }
        if (bestPyramid != null) {
            shown = addAnchor(bestPyramid, "Zoom", previewsPanel, shown);
        }
        if (shown == null) {
            previewPanel.add(new PreviewWidget(uri, GetPreviews.LARGE, null));
        }
    }

    private PreviewBean addAnchor(final PreviewBean bean, String label, final FlowPanel previewsPanel, PreviewBean shown) {
        return addAnchor(bean, label, previewsPanel, 0, shown);
    }

    private PreviewBean addAnchor(final PreviewBean bean, String label, final FlowPanel previewsPanel, final int method, PreviewBean shown) {
        final Anchor anchor = new Anchor(label);
        anchor.addStyleName("previewActionLink");
        anchor.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                for (int i = 0; i < previewsPanel.getWidgetCount(); i++ ) {
                    currentPreview = null;
                    previewsPanel.getWidget(i).removeStyleName("deadlink");
                }
                anchor.addStyleName("deadlink");
                showPreview(bean, method);
            }
        });
        previewsPanel.add(anchor);

        if (shown == null) {
            shown = bean;
            showPreview(bean, 0);
            anchor.addStyleName("deadlink");
        }
        return shown;
    }

    // ----------------------------------------------------------------------
    // preview section
    // ----------------------------------------------------------------------

    private void showPreview(PreviewBean pb, int Preview) {

        // check to make sure this is not already showing

        if (currentPreview == pb) {
            return;

        }

        // if not same as current preview hide old preview type
        if (currentPreview == null) {
            //hideWebGL();
            previewPanel.clear();
        } else if (currentPreview.getClass() != pb.getClass()) {
            if (currentPreview instanceof PreviewPyramidBean) {
                hideSeadragon();
            }
            previewPanel.clear();
            currentPreview = null;
        }

        // if now preview type create a new one
        if (currentPreview == null) {
            if (pb instanceof PreviewImageBean) {
                Image image = new Image();
                image.getElement().setId("preview");
                previewPanel.add(image);

            } else if (pb instanceof PreviewPyramidBean) {
                Label container = new Label();
                container.addStyleName("seadragon");
                container.getElement().setId("preview");
                previewPanel.add(container);

            } else if (pb instanceof PreviewVideoBean) {
                Label container = new Label();
                container.getElement().setId("preview");
                previewPanel.add(container);

            } else if (pb instanceof PreviewAudioBean) {
                Label container = new Label();
                container.getElement().setId("preview");
                previewPanel.add(container);

            } else if (pb instanceof PreviewThreeDimensionalBean) {
                Label container = new Label();
                container.getElement().setId("preview");
                previewPanel.add(container);

            } else if (pb instanceof PreviewDocumentBean) {
                Label container = new Label();
                container.getElement().setId("preview");
                previewPanel.add(container);
            }
        }

        // replace content (either same type or new created)
        if (pb instanceof PreviewImageBean) {
            PreviewImageBean pib = (PreviewImageBean) pb;
            long w = pib.getWidth();
            long h = pib.getHeight();
            if (pib.getWidth() > pib.getHeight()) {
                if (pib.getWidth() > MAX_WIDTH) {
                    h = (long) (h * (double) MAX_WIDTH / w);
                    w = MAX_WIDTH;
                }
            } else {
                if (pib.getHeight() > MAX_HEIGHT) {
                    w = (long) (w * (double) MAX_HEIGHT / h);
                    h = MAX_HEIGHT;
                }
            }
            showImage(BLOB_URL + pb.getUri(), Long.toString(w), Long.toString(h));

        } else if (pb instanceof PreviewPyramidBean) {
            showSeadragon(PYRAMID_URL + pb.getUri() + "/xml");

        } else if (pb instanceof PreviewDocumentBean) {
            showText(NOREFERENCE_URL + pb.getUri());

        } else if (pb instanceof PreviewVideoBean) {
            PreviewVideoBean pvb = (PreviewVideoBean) pb;
            String preview = null;
            if (pvb.getPreviewImage() != null) {
                preview = BLOB_URL + pvb.getPreviewImage().getUri();
            }
            showAudioVideo(BLOB_URL + pb.getUri(), preview, "video", Long.toString(pvb.getWidth()), Long.toString(pvb.getHeight()));

        } else if (pb instanceof PreviewAudioBean) {
            PreviewAudioBean pab = (PreviewAudioBean) pb;
            String preview = null;
            long width = 320;
            long height = 240;
            if (pab.getPreviewImage() != null) {
                preview = BLOB_URL + pab.getPreviewImage().getUri();
                width = pab.getPreviewImage().getWidth();
                height = pab.getPreviewImage().getHeight();
            }
            showAudioVideo(BLOB_URL + pb.getUri(), preview, "sound", Long.toString(width), Long.toString(height));

        } else if (pb instanceof PreviewThreeDimensionalBean) {
            PreviewThreeDimensionalBean p3db = (PreviewThreeDimensionalBean) pb;
            switch (Preview) {
                case 0:
                    hideWebGL();
                    hideHTML5();
                    showjvLite(EXTENSION_URL + p3db.getUri());
                    break;
                case 1:
                    hideHTML5();
                    showWebGL(NOREFERENCE_URL + p3db.getUri());
                    break;
                case 2:
                    hideWebGL();
                    show3D(NOREFERENCE_URL + p3db.getUri());
                    break;

            }
        }

        currentPreview = pb;
    }

    // ----------------------------------------------------------------------
    // javascript
    // ----------------------------------------------------------------------

    public final native void showImage(String url, String w, String h) /*-{
        img = $doc.getElementById("preview");
        img.src=url;
        img.width=w;
        img.height=h;
    }-*/;

    public final native void showAudioVideo(String url, String preview, String type, String w, String h) /*-{
        if (url != null) {
        $wnd.player = new $wnd.SWFObject('player.swf', 'player', w, h, '9');
        $wnd.player.addParam('allowfullscreen','true');
        $wnd.player.addParam('allowscriptaccess','always');
        $wnd.player.addParam('wmode','opaque');
        $wnd.player.addVariable('file',url);
        $wnd.player.addVariable('autostart','false');
        if (preview != null) {
        $wnd.player.addVariable('image',preview);
        }            
        //            $wnd.player.addVariable('author','Joe');
        //            $wnd.player.addVariable('description','Bob');
        //            $wnd.player.addVariable('title','title');
        //            $wnd.player.addVariable('debug','console');
        $wnd.player.addVariable('provider',type);
        $wnd.player.write('preview');
        }
    }-*/;

    public final native void showSeadragon(String url) /*-{
        $wnd.Seadragon.Config.debug = true;
        $wnd.Seadragon.Config.imagePath = "img/";
        $wnd.Seadragon.Config.autoHideControls = true;

        // close existing viewer
        if ($wnd.viewer) {
        $wnd.viewer.setFullPage(false);
        $wnd.viewer.setVisible(false);
        $wnd.viewer.close();
        $wnd.viewer = null;            
        }

        // open with new url
        if (url != null) {
        $wnd.viewer = new $wnd.Seadragon.Viewer("preview");
        $wnd.viewer.openDzi(url);
        }
    }-*/;

    public final native void hideSeadragon() /*-{
        // hide the current viewer if open
        if ($wnd.viewer) {
        $wnd.viewer.setFullPage(false);
        $wnd.viewer.setVisible(false);
        $wnd.viewer.close();
        $wnd.viewer = null;            
        }
    }-*/;

    //The following Javascript functions are used in the HTML5 3D Script

    public final native void readOBJ(String fileData) /*-{
        // initialize HTML5 application
        $wnd.initialize(fileData);
    }-*/;

    public final native void hideHTML5() /*-{
        // hide the current WebGL viewer if open
        $wnd.hideFrame();
    }-*/;

    public final native int getPoly() /*-{
        // hide the current viewer if open
        return $wnd.g_testObject.getPolygons();
    }-*/;

    public final native double getVertex() /*-{
        // hide the current viewer if open
        return $wnd.g_testObject.getVertices();
    }-*/;

    public final native void changeColor(int color) /*-{
        // hide the current viewer if open
        $wnd.g_ctx.clearRect(0,0,480,360);
        $wnd.g_change_color=color;
        $wnd.RenderINT();
    }-*/;

    //The following Javascript functions are in WebGL 3D script

    public final native void readWebGL(String fileData) /*-{
        // initialize WebGL application
        $wnd.init_webGL(fileData);
    }-*/;

    public final native void hideWebGL() /*-{
        // hide the current WebGL viewer if open
        $wnd.hide_webGL();
    }-*/;

    public final native void textureWebGL(int texture) /*-{
        // hide the current WebGL viewer if open
        $wnd.texture_webGL(texture);
    }-*/;

    // ----------------------------------------------------------------------
    // html previews
    // ----------------------------------------------------------------------
    public final void showText(String uri) {

        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(GWT.getHostPageBaseURL() + uri));
        try {
            @SuppressWarnings("unused")
            Request request = builder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    // Couldn't connect to server (could be timeout, SOP violation, etc.)     
                }

                public void onResponseReceived(Request request, Response response) {

                    if (200 == response.getStatusCode()) {

                        //Read file successfully; call Javascript to initialize html5 canvas
                        textHTML(response.getText());

                    } else {
                        // Handle the error.  Can get the status text from response.getStatusText()
                        HTML html3 = new HTML();
                        html3.setHTML("Error: Could not read file from server.");
                        previewPanel.add(html3);
                    }

                }
            });
        } catch (RequestException e) {
            // Couldn't connect to server        
        }

    }

    public final void textHTML(String text) {

        text = text.replaceAll("<", "&lt;");
        text = text.replaceAll(" ", "&nbsp;");
        text = text.replaceAll("\n", "<br />");

        HTML box = new HTML();
        box.setHTML("<div class='textboxPreview'>" + text + "</div>");
        previewPanel.add(box);

    }

    // ----------------------------------------------------------------------
    // 3D previews
    // ----------------------------------------------------------------------
    public final void show3D(String uri) {

        //TODO change to correct/dynamic localhost instead of static link
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(GWT.getHostPageBaseURL() + uri));
        try {
            @SuppressWarnings("unused")
            Request request = builder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    // Couldn't connect to server (could be timeout, SOP violation, etc.)     
                }

                public void onResponseReceived(Request request, Response response) {

                    if (200 == response.getStatusCode()) {

                        HTML labels = new HTML();
                        labels.setHTML("<br>");
                        previewPanel.add(labels);

                        //Read file successfully; call Javascript to initialize html5 canvas
                        readOBJ(response.getText());

                        //TODO store to tuples (RDF) as metadata
                        getPolys = getPoly();
                        Label lbl = new Label("Number of Polygons: " + getPolys);
                        previewPanel.add(lbl);

                        getVerts = getVertex();
                        lbl = new Label("Number of Vertices: " + getVerts);
                        previewPanel.add(lbl);

                    } else {
                        // Handle the error.  Can get the status text from response.getStatusText()
                        HTML html3 = new HTML();
                        html3.setHTML("Error: Could not read file from server.");
                        previewPanel.add(html3);
                    }

                }
            });
        } catch (RequestException e) {
            // Couldn't connect to server        
        }

        HTML html5 = new HTML();
        html5.setHTML("<STYLE type='text/css'> canvas {border:solid 1px #000;}</STYLE>" +
                "<CANVAS id='canvas' width='480' height='360'><P>If you are seeing this, " +
                "your browser does not support <a href='http://www.google.com/chrome/'>" +
                "HTML5</a></P></CANVAS>");
        previewPanel.add(html5);

        Button b = new Button("Red", new ClickHandler() {
            public void onClick(ClickEvent event) {
                changeColor(1);
            }
        });
        previewPanel.add(b);

        Button green = new Button("Green", new ClickHandler() {
            public void onClick(ClickEvent event) {
                changeColor(2);
            }
        });
        previewPanel.add(green);

        Button blue = new Button("Blue", new ClickHandler() {
            public void onClick(ClickEvent event) {
                changeColor(3);
            }
        });
        previewPanel.add(blue);

        Button black = new Button("Black", new ClickHandler() {
            public void onClick(ClickEvent event) {
                changeColor(4);
            }
        });
        previewPanel.add(black);

        Button white = new Button("White", new ClickHandler() {
            public void onClick(ClickEvent event) {
                changeColor(5);
            }
        });
        previewPanel.add(white);
    }

    public final void showWebGL(String uri) {

        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(GWT.getHostPageBaseURL() + uri));

        try {
            @SuppressWarnings("unused")
            Request request = builder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    // Couldn't connect to server (could be timeout, SOP violation, etc.)     
                }

                public void onResponseReceived(Request request, Response response) {

                    if (200 == response.getStatusCode()) {

                        //Read file successfully; call Javascript to initialize html5 canvas
                        readWebGL(response.getText());

                    } else {
                        // Handle the error.  Can get the status text from response.getStatusText()
                        HTML html3 = new HTML();
                        html3.setHTML("Error: Could not read file from server.");
                        previewPanel.add(html3);
                    }

                }
            });
        } catch (RequestException e) {
            // Couldn't connect to server        
        }

        HTML webGL = new HTML();
        webGL.setHTML("<STYLE type='text/css'> canvas {border:solid 1px #000;} body{overflow:hidden;}</STYLE>" +
                "<CANVAS id='c' width='480' height='360'><P>If you are seeing this, " +
                "your browser does not support <a href='http://www.google.com/chrome/'>" +
                "HTML5</a></P></CANVAS>" + "<p id='info'></p>");
        previewPanel.add(webGL);

        Button metal = new Button("Metal", new ClickHandler() {
            public void onClick(ClickEvent event) {
                textureWebGL(1);
            }
        });
        previewPanel.add(metal);

        Button wood = new Button("Wood", new ClickHandler() {
            public void onClick(ClickEvent event) {
                textureWebGL(2);
            }
        });
        previewPanel.add(wood);

        Button grass = new Button("Grass", new ClickHandler() {
            public void onClick(ClickEvent event) {
                textureWebGL(3);
            }
        });
        previewPanel.add(grass);

    }

    //Javaview : More advanced preview for 3D files
    public final void showjvLite(String url) {
        HTML Javaview = new HTML();
        Javaview.setHTML("<APPLET name=jvLite code='jvLite.class' width=480 " +
                "height=360 archive='plugins/jvLite.jar'>" +
                "<PARAM NAME='model' VALUE='" + url + ".obj" + "'>" +
                //"<PARAM NAME='model' VALUE='images/metal.jpg'>" +
                "</APPLET>");
        previewPanel.add(Javaview);
    }

}