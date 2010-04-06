package edu.illinois.ncsa.mmdb.web.client;

import java.util.Date;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.ui.PreviewWidget;

public class DatasetTableCoverFlowView extends DatasetTableView {
    int n = 0;

    public DatasetTableCoverFlowView() {
        super();
        setWidth("715px");
    }

    @Override
    public void removeAllRows() {
        super.removeAllRows();
        n = 0;
    }

    @Override
    public void addRow(String id, String title, String mimeType, Date date, String previewUri, String size, String authorsId) {
        VerticalPanel panel = new VerticalPanel();
        PreviewWidget preview = null;
        Label titleLabel = new Label(title);
        if (n++ == 1) {
            preview = new PreviewWidget(id, GetPreviews.LARGE, "dataset?id=" + id);
            preview.setWidth("400px");
            preview.setMaxWidth(400);
            getCellFormatter().addStyleName(0, n, "flowPreviewLarge");
            titleLabel.addStyleName("flowLabelLarge");
        } else {
            preview = new PreviewWidget(id, GetPreviews.SMALL, "dataset?id=" + id);
            preview.setMaxWidth(150);
            preview.setWidth("150px");
            getCellFormatter().addStyleName(0, n, "flowPreviewSmall");
            titleLabel.addStyleName("flowLabelSmall");
        }
        panel.add(preview);
        panel.add(titleLabel);
        this.setWidget(0, n, panel);
    }

    public void doneAddingRows() {
    }

    @Override
    public int getPageSize() {
        // TODO Auto-generated method stub
        return 1;
    }

    @Override
    public Widget asWidget() {
        // TODO Auto-generated method stub
        return this;
    }

    @Override
    public void insertRow(int position, String id, String title, String mimeType, Date date, String previewUri, String size, String authorsId) {
        // TODO Auto-generated method stub

    }

}
