package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import java.util.Collections;
import java.util.List;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetMetadata;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetMetadataResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.Metadata;
import edu.illinois.ncsa.mmdb.web.client.ui.InfoWidget;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.PreviewThreeDimensionalBean;

public class PreviewMetadataWidget extends PreviewBeanWidget<PreviewBean> {

    private final ScrollPanel   widget;
    private final DatasetBean   dataset;
    private final DispatchAsync service;

    public PreviewMetadataWidget(HandlerManager eventBus, DatasetBean dataset, DispatchAsync service) {
        super(eventBus);
        this.dataset = dataset;
        this.service = service;
        widget = new ScrollPanel();
        VerticalPanel inner = new VerticalPanel();
        inner.add(new InfoWidget(dataset, service));
        inner.add(createExtractedPanel(dataset, service));
        widget.add(inner);
        setWidget(widget);
    }

    @Override
    public PreviewMetadataWidget newWidget() {
        return new PreviewMetadataWidget(eventBus, dataset, service);
    }

    public Class<? extends PreviewBean> getPreviewBeanClass() {
        return PreviewThreeDimensionalBean.class;
    }

    private FlexTable createExtractedPanel(final DatasetBean dataset, DispatchAsync service) {
        //Extracted Metadata

        final FlexTable informationTable = new FlexTable();
        if (dataset.getUri() != null) {
            service.execute(new GetMetadata(dataset.getUri()), new AsyncCallback<GetMetadataResult>() {
                @Override
                public void onFailure(Throwable arg0) {
                    GWT.log("Error retrieving metadata about dataset " + dataset.getUri(), null);
                }

                @Override
                public void onSuccess(GetMetadataResult arg0) {
                    List<Metadata> metadata = arg0.getMetadata();
                    Collections.sort(metadata);
                    String category = "";
                    for (Metadata tuple : metadata ) {
                        if (!category.equals(tuple.getCategory())) {
                            int row = informationTable.getRowCount() + 1;
                            category = tuple.getCategory();
                            informationTable.setHTML(row, 0, "<div style='font-size:1.4em;'>" + category + "</div>");
                            informationTable.setText(row, 1, ""); //$NON-NLS-1$

                        }
                        int row = informationTable.getRowCount();
                        informationTable.setText(row, 0, tuple.getLabel());
                        informationTable.setText(row, 1, tuple.getValue());
                    }
                }
            });
        }

        return informationTable;
    }

    @Override
    public String getAnchorText() {
        return "Metadata";
    }

    @Override
    public void setSection(String section) throws IllegalArgumentException {
        throw (new IllegalArgumentException("Could not parse section."));
    }

    @Override
    public String getSection() {
        return "Document"; //$NON-NLS-1$
    }

    @Override
    protected void showSection() {
        widget.setSize(getWidth() + "px", getHeight() + "px");

    }
}
