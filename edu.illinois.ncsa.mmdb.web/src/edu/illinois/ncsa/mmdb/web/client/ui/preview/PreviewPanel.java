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

package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.event.PreviewSectionChangedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.PreviewSectionChangedEventHandler;
import edu.illinois.ncsa.mmdb.web.client.ui.PreviewWidget;
import edu.uiuc.ncsa.cet.bean.PreviewBean;

/**
 * Show one or more preview related to each PreviewBean
 * 
 * @author Rob Kooper
 * 
 */

public class PreviewPanel extends Composite {
    /** current preview widget shown */
    private PreviewBeanWidget<? extends PreviewBean> previewWidget;

    /** Mapping from preview to widget */
    private static List<PreviewBeanWidget>           widgets;

    /** Check to see if all previews are added. */
    private static boolean                           isInitialized = false;

    /** Panel that will hold the actual prevew widget */
    private AbsolutePanel                            previewPanel;

    static public void addWidget(PreviewBeanWidget<? extends PreviewBean> widget) {
        if (widgets == null) {
            widgets = new ArrayList<PreviewBeanWidget>();
        }
        widgets.add(widget);
    }

    static private void initializePreviews(HandlerManager eventBus) {
        if (isInitialized) {
            return;
        }
        isInitialized = true;

        addWidget(new PreviewVideoBeanWidget(eventBus));
        addWidget(new PreviewAudioBeanWidget(eventBus));
        addWidget(new PreviewDocumentBeanWidget(eventBus));
        addWidget(new PreviewImageBeanWidget(eventBus));
        addWidget(new PreviewPyramidBeanWidget(eventBus));
        addWidget(new PreviewMultiImageBeanWidget(eventBus));
        addWidget(new Preview3DJavaBeanWidget(eventBus));
        addWidget(new Preview3DHTML5BeanWidget(eventBus));
        addWidget(new Preview3DWebGLBeanWidget(eventBus));
    }

    public PreviewPanel(HandlerManager eventBus) {
        initializePreviews(eventBus);
        previewWidget = null;

        eventBus.addHandler(PreviewSectionChangedEvent.TYPE, new PreviewSectionChangedEventHandler() {
            @Override
            public void onSectionChanged(PreviewSectionChangedEvent sectionChangedEvent) {
                GWT.log(sectionChangedEvent.getSection());
            }
        });
    }

    public void unload() {
        if (previewWidget != null) {
            previewWidget.hide();
        }
        previewWidget = null;
    }

    /**
     * Create ordered list of all preview beans. Each preview bean type will
     * only appear once in the list.
     * 
     * @param result
     *            unordered list of all preview beans
     * @param maxwidth
     *            the maximum widht of the panel
     * @return ordered list of all preview beans.
     */
    private List<PreviewBeanWidget> getOrderedBeans(GetDatasetResult result, int maxwidth) {
        List<PreviewBeanWidget> list = new ArrayList<PreviewBeanWidget>();

        for (PreviewBeanWidget widget : widgets ) {
            PreviewBean best = null;
            for (PreviewBean pb : result.getPreviews() ) {
                if (widget.getPreviewBeanClass() == pb.getClass()) {
                    if (best == null) {
                        best = pb;
                    } else {
                        best = widget.bestFit(best, pb, maxwidth, -1);
                    }
                }
            }
            if (best != null) {
                PreviewBeanWidget pbw = widget.newWidget();
                pbw.setPreviewBean(best);
                list.add(pbw);
            }
        }

        // all done
        return list;
    }

    /**
     * Show all the previews to the user.
     * 
     * @param result
     * @param leftColumn
     * @param uri
     */
    public void drawPreview(final GetDatasetResult result, FlowPanel leftColumn, String uri) {
        List<PreviewBeanWidget> widgets = getOrderedBeans(result, leftColumn.getOffsetWidth());

        // preview options
        FlowPanel previewsPanel = new FlowPanel();
        previewsPanel.addStyleName("datasetActions"); //$NON-NLS-1$
        leftColumn.add(previewsPanel);

        // space for the preview/video/zoom
        previewPanel = new AbsolutePanel();
        previewPanel.addStyleName("previewPanel"); //$NON-NLS-1$
        leftColumn.add(previewPanel);

        // add previews, if no preview is available fall back on PreviewWidget
        boolean showme = true;
        for (PreviewBeanWidget pbw : widgets ) {
            createAnchor(pbw, previewsPanel, showme);
            showme = false;
        }

        if (showme) {
            previewPanel.add(new PreviewWidget(uri, GetPreviews.LARGE, null));
        }
    }

    private void createAnchor(final PreviewBeanWidget pbw, final FlowPanel previewsPanel, boolean showme) {
        final Anchor anchor = new Anchor(pbw.getAnchorText());
        anchor.addStyleName("previewActionLink");
        anchor.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                for (int i = 0; i < previewsPanel.getWidgetCount(); i++ ) {
                    previewsPanel.getWidget(i).removeStyleName("deadlink");
                }
                anchor.addStyleName("deadlink");
                showPreview(pbw);
            }
        });
        previewsPanel.add(anchor);

        if (showme) {
            showPreview(pbw);
            anchor.addStyleName("deadlink");
        }
    }

    private void showPreview(PreviewBeanWidget pbw) {
        if (previewWidget == pbw) {
            return;
        }
        if (previewWidget != null) {
            previewWidget.hide();
        }
        previewPanel.clear();

        previewWidget = pbw;
        previewPanel.add(pbw.getWidget());
        pbw.show();
    }
}