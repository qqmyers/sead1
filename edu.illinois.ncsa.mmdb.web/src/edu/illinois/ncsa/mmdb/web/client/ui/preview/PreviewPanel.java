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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollectionResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.event.PreviewSectionChangedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.PreviewSectionChangedEventHandler;
import edu.illinois.ncsa.mmdb.web.client.event.PreviewSectionShowEvent;
import edu.illinois.ncsa.mmdb.web.client.event.PreviewSectionShowEventHandler;
import edu.illinois.ncsa.mmdb.web.client.ui.PreviewWidget;
import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
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
    private static List<PreviewBeanWidget>           registeredWidgets;

    /** Check to see if all previews are added. */
    private static boolean                           isInitialized       = false;

    /** Check to see if this Panel is initialized to display collection previews */
    private boolean                                  collectionPreviewer = false;

    /** Panel that will hold the actual preview widget */
    private AbsolutePanel                            previewPanel;

    /** List of widgets used to show dataset */
    private List<PreviewBeanWidget>                  widgets;

    private DatasetBean                              dataset;

    /** Mapping from widget to corresponding anchor */
    private final Map<PreviewBeanWidget, Anchor>     anchors             = new HashMap<PreviewBeanWidget, Anchor>();

    private final DispatchAsync                      dispatchAsync;
    private final HandlerManager                     eventBus;

    /** Width and height values only use in embedded widet */
    private static boolean                           isEmbedded;
    private final int                                width;
    private final int                                height;
    private static final int                         MAX_WIDTH           = 600;
    private HorizontalPanel                          anchorTabs;

    static public void addWidget(PreviewBeanWidget<? extends PreviewBean> widget) {
        if (registeredWidgets == null) {
            registeredWidgets = new ArrayList<PreviewBeanWidget>();
        }
        registeredWidgets.add(widget);
    }

    static private void initializePreviews(HandlerManager eventBus, boolean collectionPreviewer) {
        if (isInitialized) {
            return;
        }
        isInitialized = true;

        if (!collectionPreviewer) {
            //register dataset specific previewers here.
            addWidget(new PreviewMultiVideoBeanWidget(eventBus));
            addWidget(new PreviewVideoBeanWidget(eventBus));
            addWidget(new PreviewAudioBeanWidget(eventBus));
            addWidget(new PreviewDocumentBeanWidget(eventBus));
            addWidget(new PreviewTabularDataBeanWidget(eventBus));
            addWidget(new PreviewImageBeanWidget(eventBus));
            addWidget(new PreviewPyramidBeanWidget(eventBus));
            addWidget(new Preview3DJavaBeanWidget(eventBus));
            addWidget(new Preview3DHTML5BeanWidget(eventBus));
            addWidget(new Preview3DWebGLBeanWidget(eventBus));
            addWidget(new PreviewPTMBeanWidget(eventBus));
            addWidget(new PreviewGeoserverBeanWidget(eventBus));
            //The following previews currently not supported in embedded previewer
            if (!isEmbedded) {
                addWidget(new PreviewMultiImageBeanWidget(eventBus));
                addWidget(new PreviewMultiTabularDataBeanWidget(eventBus));
            }
        } else {
            //register collection specific previewers here.
            addWidget(new PreviewImageBeanWidget(eventBus));
            addWidget(new PreviewCollectionMultiImageBeanWidget(eventBus));
            addWidget(new PreviewGeoPointBeanWidget(eventBus));
        }
    }

    public PreviewPanel(DispatchAsync dispatchAsync, HandlerManager eventBus) {
        this(dispatchAsync, eventBus, false);
    }

    public PreviewPanel(DispatchAsync dispatchAsync, HandlerManager eventBus, boolean collectionPreviewer) {
        this(dispatchAsync, eventBus, false, 500, 500, collectionPreviewer);
    }

    public PreviewPanel(DispatchAsync dispatchAsync, HandlerManager eventBus, boolean isEmbedded, int width, int height) {
        this(dispatchAsync, eventBus, isEmbedded, width, height, false);
    }

    public PreviewPanel(DispatchAsync dispatchAsync, HandlerManager eventBus, boolean isEmbedded, int width, int height, boolean collectionPreviewer) {
        this.dispatchAsync = dispatchAsync;
        this.isEmbedded = isEmbedded;
        this.collectionPreviewer = collectionPreviewer;
        initializePreviews(eventBus, collectionPreviewer);

        previewWidget = null;

        eventBus.addHandler(PreviewSectionChangedEvent.TYPE, new PreviewSectionChangedEventHandler() {
            @Override
            public void onSectionChanged(PreviewSectionChangedEvent sectionChangedEvent) {
                GWT.log(sectionChangedEvent.getSection());
            }
        });

        eventBus.addHandler(PreviewSectionShowEvent.TYPE, new PreviewSectionShowEventHandler() {
            @Override
            public void onSectionShow(PreviewSectionShowEvent sectionShowEvent) {
                GWT.log(sectionShowEvent.getSection());
                showSection(sectionShowEvent.getSection());
            }
        });

        this.width = width - 2;
        this.height = height - 52;
        this.eventBus = eventBus;

    }

    public void showSection(String section) {
        for (PreviewBeanWidget widget : widgets ) {
            try {
                widget.setSection(section);
                clickEvent(widget);
            } catch (IllegalArgumentException exc) {
                //GWT.log(widget + " could not parse section " + section, exc);
            }
        }
    }

    public void unload() {
        if (previewWidget != null) {
            previewWidget.hide();
        }
        previewWidget = null;
    }

    /**
     * Show all the previews to the user.
     * 
     * @param result
     * @param leftColumn
     * @param uri
     */
    public void drawPreview(final GetDatasetResult result, FlowPanel leftColumn, String uri) {
        dataset = result.getDataset();
        widgets = getOrderedBeans(result, leftColumn.getOffsetWidth());
        anchors.clear();

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

        if (isEmbedded) {
            anchorTabs = new HorizontalPanel();
            anchorTabs.addStyleName("anchorTabs");
            previewsPanel.add(anchorTabs);
        }

        for (PreviewBeanWidget pbw : widgets ) {
            if (isEmbedded) {
                final FocusPanel tab = new FocusPanel();
                tab.add(createAnchor(pbw, showme));
                tab.setStyleName("tabPreview");
                anchorTabs.add(tab);
            } else {
                previewsPanel.add(createAnchor(pbw, showme));
            }

            showme = false;
        }

        if (showme) {
            previewPanel.add(new PreviewWidget(uri, GetPreviews.LARGE, null, dispatchAsync));
        }

        //Add Metadata Preview to Embedded Widget
        if (isEmbedded) {
            FocusPanel metadataTab = new FocusPanel();
            metadataTab.addStyleName("tabPreview");
            PreviewMetadataWidget metadataWidget = new PreviewMetadataWidget(eventBus, dataset, dispatchAsync);
            metadataWidget.setWidth(width);
            metadataWidget.setHeight(height);
            metadataTab.add(createAnchor(metadataWidget, false));
            anchorTabs.add(metadataTab);
        }

    }

    /**
     * Shows the previews associated with the GetCollectionResult collection.
     * 
     * @param result
     * @param leftColumn
     * @param uri
     * @throws IllegalStateExecption
     *             if this PreviewPanel was not initialized to handle
     *             collection previews.
     */
    public void drawPreview(GetCollectionResult result, FlowPanel leftColumn, String uri) {
        if (!collectionPreviewer) {
            throw new IllegalStateException("The PreviewPanel was initialized as a Dataset previewer," +
                    "but was asked to draw Collection previews.");
        }

        CollectionBean collection = result.getCollection();

        // Mock up a DatasetBean to pass to the drawPreview method.
        DatasetBean fakeDataset = new DatasetBean();

        fakeDataset.setCreator(collection.getCreator());
        fakeDataset.setDate(collection.getCreationDate());
        fakeDataset.setDescription(collection.getDescription());
        fakeDataset.setLabel(collection.getLabel());
        fakeDataset.setTitle(collection.getTitle());
        fakeDataset.setUri(collection.getUri());

        GetDatasetResult datasetResult = new GetDatasetResult(fakeDataset, result.getPreviews());
        drawPreview(datasetResult, leftColumn, uri);
    }

    /**
     * Calculate best fit ratio for embedded previews
     * 
     */
    public float getSizeRatio() {

        float ratio = 1;
        boolean showme = true;

        for (PreviewBeanWidget pbw : widgets ) {
            if (showme) {
                GWT.log("Preview Width: " + pbw.getWidth());
                GWT.log("Preview height: " + pbw.getHeight());
                ratio = (float) pbw.getWidth() / (float) pbw.getHeight();
            }
            showme = false;
        }
        return ratio;
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
        boolean hasMultiVideo = false;

        List<PreviewBeanWidget> list = new ArrayList<PreviewBeanWidget>();

        for (PreviewBeanWidget widget : registeredWidgets ) {
            if ((widget instanceof PreviewVideoBeanWidget) && hasMultiVideo) {
                GWT.log("Skipping " + widget.getClass());
                continue;
            }
            PreviewBean best = null;
            for (PreviewBean pb : result.getPreviews() ) {
                if (widget.getPreviewBeanClass() == pb.getClass()) {
                    if (best == null) {
                        best = pb;
                    } else {
                        if (isEmbedded) {
                            //needed for embedded widget so large widget always chosen
                            maxwidth = MAX_WIDTH;
                        }
                        best = widget.bestFit(best, pb, maxwidth, -1);
                    }
                }
            }
            if (best != null) {
                if (widget instanceof PreviewMultiVideoBeanWidget) {
                    hasMultiVideo = true;
                }
                PreviewBeanWidget pbw = widget.newWidget();
                pbw.setPreviewBean(best);
                pbw.setDatasetBean(dataset);
                pbw.setDispatch(dispatchAsync);
                pbw.setEmbedded(isEmbedded);
                if (isEmbedded) {
                    pbw.setWidth(width);
                    pbw.setHeight(height);
                }
                list.add(pbw);
            }
        }

        // all done
        return list;
    }

    private Anchor createAnchor(final PreviewBeanWidget pbw, boolean showme) {
        Anchor anchor = new Anchor(pbw.getAnchorText());
        anchor.addStyleName("previewActionLink");
        anchor.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                clickEvent(pbw);
            }
        });

        if (showme) {
            showPreview(pbw);
            anchor.addStyleName("deadlink");
        }

        anchors.put(pbw, anchor);

        return anchor;
    }

    private void clickEvent(PreviewBeanWidget pbw) {
        for (Anchor x : anchors.values() ) {
            x.removeStyleName("deadlink");
        }
        anchors.get(pbw).addStyleName("deadlink");
        showPreview(pbw);
    }

    private void showPreview(PreviewBeanWidget pbw) {
        if (previewWidget != pbw) {
            if (previewWidget != null) {
                previewWidget.hide();
            }
            previewPanel.clear();

            previewWidget = pbw;
            previewPanel.add(pbw.getWidget());
        }
        pbw.show();
    }
}