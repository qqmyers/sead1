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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.PreviewImageBean;
import edu.uiuc.ncsa.cet.bean.PreviewPyramidBean;
import edu.uiuc.ncsa.cet.bean.PreviewVideoBean;

/**
 * Show one or more preview related to each PreviewBean
 * 
 * @author Luigi Marini, Luis Mendez
 * 
 */

public class PreviewPanel {

    /** maximum width of a preview image */
    private static final long   MAX_WIDTH    = 600;
    /** maximum height of a preview image */
    private static final long   MAX_HEIGHT   = 600;

    private AbsolutePanel       previewPanel;
    private PreviewBean         currentPreview;
    //private final FlowPanel     leftColumn;

    public int                  getPolys;
    public double               getVerts;

    private static final String BLOB_URL     = "./api/image/";
    private static final String DOWNLOAD_URL = "./api/image/download/";
    private static final String PYRAMID_URL  = "./pyramid/";

    public PreviewPanel() {

    }

    //@Override
    protected void onUnload() {
        //super.onUnload();
        hideSeadragon();
    }

    public void drawPreview(final GetDatasetResult result, FlowPanel leftColumn, String uri) {

        // find best preview bean, add others
        // best image preview is that that is closest to width of column
        int maxwidth = leftColumn.getOffsetWidth();
        List<PreviewBean> previews = new ArrayList<PreviewBean>();
        // FIXME use a map to handle all previews
        PreviewImageBean bestImage = null;
        PreviewVideoBean bestVideo = null;
        //Preview3DBean best3D = null;
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
            }
            /*else if (pb instanceof Preview3DBean) {
                Preview3DBean p3Db = (Preview3DBean) pb;
                if (best3D == null) {
                    best3D = p3Db;
                } else if (Math.abs(maxwidth - p3Db.getWidth()) < Math.abs(maxwidth - best3D.getWidth())) {
                    best3D = p3Db;
                }
            }*/

            else {
                previews.add(pb);
            }
        }
        if (bestImage != null) {
            previews.add(bestImage);
        }
        if (bestVideo != null) {
            previews.add(bestVideo);
        }
        /*
        if (best3D != null) {
            previews.add(best3D);
        }
        */

        // sort beans, image, zoom, video, rest
        Collections.sort(previews, new Comparator<PreviewBean>() {
            @Override
            public int compare(PreviewBean o1, PreviewBean o2)
                     {
                         // sort by type
                         if (o1.getClass() != o2.getClass()) {
                             if (o1 instanceof PreviewImageBean) {
                                 return -1;
                             }
                             if (o2 instanceof PreviewImageBean) {
                                 return +1;
                             }
                             if (o1 instanceof PreviewPyramidBean) {
                                 return -1;
                             }
                             if (o2 instanceof PreviewPyramidBean) {
                                 return +1;
                             }
                             if (o1 instanceof PreviewVideoBean) {
                                 return -1;
                             }
                             if (o2 instanceof PreviewVideoBean) {
                                 return +1;
                             }/*
                              if (o1 instanceof Preview3DBean) {
                                 return -1;
                              }
                              if (o2 instanceof Preview3DBean) {
                                 return +1;
                              }*/
                         }
                         // don't care at this point
                         return 0;
                     }
        });

        // preview options
        final FlowPanel previewsPanel = new FlowPanel();
        previewsPanel.addStyleName("datasetActions");
        for (PreviewBean pb : previews ) {
            String label;
            //|| pb instanceof Preview3DBean
            if ((pb instanceof PreviewImageBean)) {
                label = "Preview";

            } else if (pb instanceof PreviewPyramidBean) {
                label = "Zoom in";
                if (MMDB.getUsername().contains("joefutrelle@gmail.com") || MMDB.getUsername().contains("acraig@ncsa.uiuc.edu")) {
                    label = "Mega-Zoomâ„¢";
                }
            } else if (pb instanceof PreviewVideoBean) {
                label = "Play video";

            } else {
                label = "Unknown"; // FIXME maybe "other" would be more user-friendly?
                GWT.log("Unknown preview bean " + pb);
            }

            final PreviewBean finalpb = pb;
            final Anchor anchor = new Anchor(label);
            anchor.addStyleName("previewActionLink");
            anchor.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    for (int i = 0; i < previewsPanel.getWidgetCount(); i++ ) {
                        previewsPanel.getWidget(i).removeStyleName("deadlink");
                    }
                    anchor.addStyleName("deadlink");
                    showPreview(finalpb);
                }
            });
            if (bestVideo == finalpb) {
                anchor.addStyleName("deadlink");
            } else if (bestImage == finalpb) {
                anchor.addStyleName("deadlink");
            }

            /* else if (best3D == finalpb) {
                anchor.addStyleName("deadlink");
            }*/

            previewsPanel.add(anchor);
        }
        leftColumn.add(previewsPanel);

        // space for the preview/video/zoom
        currentPreview = null;
        previewPanel = new AbsolutePanel();
        previewPanel.addStyleName("previewPanel");
        leftColumn.add(previewPanel);

        if (bestVideo != null) {
            showPreview(bestVideo);
        } else if (bestImage != null) {
            showPreview(bestImage);
            //FIXME hack to allow preview based on MIME type instead of a bean
            //best3D != null || 
        } else if (result.getDataset().getMimeType().equals("application/x-tgif")) {
            //showPreview(best3D);
            show3D(uri);
        } else {
            previewPanel.add(new PreviewWidget(uri, GetPreviews.LARGE, null));
        }

    }

    // ----------------------------------------------------------------------
    // preview section
    // ----------------------------------------------------------------------

    private void showPreview(PreviewBean pb) {
        // check to make sure this is not already showing
        if (currentPreview == pb) {
            return;
        }

        // if not same as current preview hide old preview type
        if (currentPreview == null) {
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
                //image.addStyleName( "sea dragon" );
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

            } /*else if (pb instanceof Preview3DBean) {
                Label container = new Label();
                container.getElement().setId("preview");
                previewPanel.add(container);
              }
              */

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

        } else if (pb instanceof PreviewVideoBean) {
            PreviewVideoBean pvb = (PreviewVideoBean) pb;
            String preview = null;
            if (pvb.getPreviewImage() != null) {
                preview = BLOB_URL + pvb.getPreviewImage().getUri();
            }
            showFlash(BLOB_URL + pb.getUri(), preview, "video", Long.toString(pvb.getWidth()), Long.toString(pvb.getHeight()));

        } /*else if (pb instanceof Preview3DBean) {
            //show 3D preview when hack is gone
          }
          */

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

    public final native void showFlash(String url, String preview, String type, String w, String h) /*-{
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

    public final native void readOBJ(String fileData) /*-{
        // hide the current viewer if open
        $wnd.initialize(fileData);
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

    // ----------------------------------------------------------------------
    // html previews
    // ----------------------------------------------------------------------

    public final void show3D(String uri) {

        String url = "http://127.0.0.1:8888/" + DOWNLOAD_URL + uri;
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(url));
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

    ///////////////////////////////////////////////////////////////////
    // The following previews below are not in use and are being tested
    ///////////////////////////////////////////////////////////////////

    //Javaview : More advanced preview for 3D files
    public final void showjvLite(String url) {
        HTML Javaview = new HTML();
        Javaview.setHTML("<APPLET name=jvLite code='jvLite.class' codebase='/' width=480 " +
                "height=480 archive='plugins/jvLite.jar'>" +
                //"<PARAM NAME='model' VALUE='" + url + "'>" +
                "<PARAM NAME='model' VALUE='plugins/ladybird.obj'>" +
                "<PARAM NAME='border' VALUE='hide'></APPLET>");
        previewPanel.add(Javaview);
    }

    //Google Docs Viewer CONS - no RDF support, less file formats supported
    //                   PROS - Supports large file sizes
    public final void showDocs(String url) {
        HTML Docs = new HTML();
        Docs.setHTML("<iframe src='http://docs.google.com/viewer?" +
                // "url=" + BLOB_URL + url + "&embedded=true' " +
                "url=www.arl.noaa.gov/documents/reports/niagara_overview.ppt&embedded=true' " +
                "style='width:650px; height:500px;' frameborder='0'></iframe> ");

        previewPanel.add(Docs);
    }

    //ZOHO Viewer CONS - 10MB file size limit, has to include HTTP, no RDF support
    //            PROS - Large number of file formats supported  
    public final void showZOHO(String url) {
        HTML Docs = new HTML();
        Docs.setHTML("<iframe src='http://viewer.zoho.com/api/urlview.do?" +
                   "url=http://www.arl.noaa.gov/documents/reports/niagara_overview.ppt&embed=true' " +
                //"url=http://medici-demo.ncsa.illinois.edu/medici/api/image/download/tag:medici@uiuc.edu,2009:data_tWyTtfhMVY7vxZeEV4QjqQ&embed=true' " +
                "frameborder='0' width='600' height='500'> </iframe>");

        previewPanel.add(Docs);
    }
}