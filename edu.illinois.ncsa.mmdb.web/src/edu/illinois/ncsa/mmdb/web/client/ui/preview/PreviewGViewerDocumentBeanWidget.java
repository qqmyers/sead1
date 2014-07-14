/*******************************************************************************
 * Copyright 2014 University of Michigan
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetServiceToken;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetServiceTokenResult;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PreviewBean;

public class PreviewGViewerDocumentBeanWidget extends PreviewBeanWidget<PreviewGViewerDocumentBean> {
    private final Panel widget;

    public PreviewGViewerDocumentBeanWidget(HandlerManager eventBus) {
        super(eventBus);
        widget = new SimplePanel();
        widget.getElement().setId(DOM.createUniqueId());
        setWidget(widget);
    }

    @Override
    public PreviewBeanWidget<PreviewGViewerDocumentBean> newWidget() {
        return new PreviewGViewerDocumentBeanWidget(eventBus);
    }

    @Override
    public Class<? extends PreviewBean> getPreviewBeanClass() {
        return PreviewGViewerDocumentBean.class;
    }

    @Override
    protected void showSection() {
        String method = "/datasets/" + URL.encodePathSegment(getDataset().getUri()) + "/file";
        try {
            final String encodedUrl = GWT.getHostPageBaseURL() + "resteasy" + method;

            getDispatch().execute(new GetServiceToken(method), new AsyncCallback<GetServiceTokenResult>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Could not get Token", caught);
                }

                @Override
                public void onSuccess(GetServiceTokenResult result) {
                    String finalUrl;
                    finalUrl = URL.encodePathSegment(encodedUrl) + "?token=" + result.getToken();
                    //finalUrl = encodedUrl + "?token=" + result.getToken();
                    Frame f = new Frame("https://docs.google.com/viewer?url=" + finalUrl + "&embedded=true");
                    f.setSize("90%", "600px");
                    widget.clear();
                    widget.add(f);

                }
            });
        } catch (Exception e) {
            GWT.log("Bad URL when requesting Token for Google Viewer", e);
        }
    }

    @Override
    public void setSection(String section) throws IllegalArgumentException {
        throw (new IllegalArgumentException("Could not parse section."));
    }

    @Override
    public String getSection() {
        return "Document";
    }

    @Override
    public String getAnchorText() {
        return "Google Viewer";
    }

    /**
     * Decides which datasets to show the Google viewer for:
     * From:
     * https://support.google.com/drive/answer/2423485?hl=en&p=docs_viewer&rd=1
     * :
     * Image files (.JPEG, .PNG, .GIF, .TIFF, .BMP)
     * Raw Image formats
     * Video files (WebM, .MPEG4, .3GPP, .MOV, .AVI, .MPEGPS, .WMV, .FLV, .ogg)
     * Microsoft Word (.DOC and .DOCX)
     * Microsoft Excel (.XLS and .XLSX)
     * Microsoft PowerPoint (.PPT and .PPTX)
     * Adobe Portable Document Format (.PDF)
     * Tagged Image File Format (.TIFF)
     * Scalable Vector Graphics (.SVG)
     * PostScript (.EPS, .PS)
     * TrueType (.TTF)
     * XML Paper Specification (.XPS)
     * .MTS files
     * 
     * But: http://stackoverflow.com/questions/24325363/google-doc-viewer-which-
     * formats-are-really-supported
     * so - removing jpg, image,video for now...
     */
    private static Set<String> extensions = null;
    private static Set<String> mimetypes  = null;

    public static boolean isGoogleViewable(DatasetBean d) {
        if (d == null) {
            return false;
        }
        String mimeType = d.getMimeType();
        String filename = d.getFilename();
        long size = d.getSize();
        if (size > 0) {
            if (size < 1024 * 1024 * 20) {

                if (extensions == null) {
                    extensions = new HashSet<String>();
                    //extensions.add("jpg");
                    //extensions.add("jpeg");
                    //extensions.add("png");
                    //extensions.add("gif");
                    //extensions.add("tif");
                    //extensions.add("tiff");
                    //extensions.add("bmp");
                    //extensions.add("mpeg");
                    //extensions.add("mpg");
                    //extensions.add("mp4");
                    //extensions.add("m4a");
                    //extensions.add("m4p");
                    //extensions.add("3gpp");
                    //extensions.add("mov");
                    //extensions.add("avi");
                    //extensions.add("mpegps");
                    extensions.add("ps"); //mpegps and post script
                    //extensions.add("wmv");
                    //extensions.add("flv");
                    //extensions.add("ogg");
                    extensions.add("doc");
                    extensions.add("docx");
                    extensions.add("xls");
                    extensions.add("xlsx");
                    extensions.add("ppt");
                    extensions.add("pptx");
                    extensions.add("pdf");
                    extensions.add("svg");
                    extensions.add("eps");
                    extensions.add("xps");
                    extensions.add("mts");
                    extensions.add("ttf");

                    mimetypes = new HashSet<String>();
                    //Could retrieve these from MimeMap via a handler... 
                    mimetypes.add("application/msword");
                    mimetypes.add("application/pdf");
                    mimetypes.add("application/postscript");
                    mimetypes.add("application/vnd.ms-excel");
                    mimetypes.add("application/vnd.ms-powerpoint");
                    mimetypes.add("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                    mimetypes.add("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
                    mimetypes.add("application/vnd.openxmlformats-officedocument.presentationml.presentation");
                    //mimetypes.add("application/x-troff-msvideo");
                    //mimetypes.add("image/bmp");
                    //mimetypes.add("image/gif");
                    //mimetypes.add("image/jpeg");
                    //mimetypes.add("image/png");
                    //mimetypes.add("image/tiff");
                    //mimetypes.add("video/mpeg");
                    //mimetypes.add("video/quicktime");
                    //mimetypes.add("video/x-msvideo");
                    //mimetypes.add("video/avchd");
                }

                if (filename != null) {
                    int index = filename.lastIndexOf(".");
                    if (index != -1) {
                        String ext = filename.substring(index + 1);

                        for (String s : extensions ) {
                            if (s.equalsIgnoreCase(ext)) {
                                return true;
                            }
                        }
                    }
                }
                if (mimeType != null) {
                    for (String m : mimetypes ) {
                        if (m.equals(mimeType)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
