package edu.illinois.ncsa.mmdb.web.client.view;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.TextFormatter;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetProperty;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetPropertyResult;
import edu.illinois.ncsa.mmdb.web.client.presenter.UploadStatusPresenter.Display;
import edu.illinois.ncsa.mmdb.web.client.ui.EditableLabel;
import edu.illinois.ncsa.mmdb.web.client.ui.PreviewWidget;
import edu.illinois.ncsa.mmdb.web.client.ui.ProgressBar;
import edu.illinois.ncsa.mmdb.web.client.ui.TagsWidget;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

public class UploadStatusView extends Composite implements Display {
    VerticalPanel          thePanel;
    HorizontalPanel        progressPanel;
    FlexTable              statusTable;
    Map<Integer, CheckBox> selectionCheckboxes;

    public UploadStatusView() {
        thePanel = new VerticalPanel();
        progressPanel = new HorizontalPanel();
        thePanel.add(progressPanel);
        statusTable = new FlexTable();
        selectionCheckboxes = new HashMap<Integer, CheckBox>();
        thePanel.add(statusTable);
        initWidget(thePanel);
    }

    @Override
    public void clear() {
        statusTable.removeAllRows();
    }

    @Override
    public HasValue<Boolean> getSelectionControl(int ix) {
        if (ix >= selectionCheckboxes.size()) {
            GWT.log("error: uploadstatus presenter asked for a nonexistent selection control");
            return null;
        }
        return selectionCheckboxes.get(ix);
    }

    @Override
    public void onComplete(int ix, String uri, int total) {
        Anchor anchor = new Anchor("View", "#dataset?id=" + uri);
        anchor.setTarget("_blank");
        CheckBox selectionCheckbox = new CheckBox();
        selectionCheckboxes.put(ix, selectionCheckbox);
        statusTable.setWidget(ix, 0, selectionCheckbox);
        statusTable.setWidget(ix, 1, anchor);
        statusTable.setWidget(ix, 3, new Label("Complete"));
        //
        if (total > 0) {
            int n = ix + 1;
            progressPanel.clear();
            progressPanel.add(new ProgressBar((int) ((float) (n * 100) / (float) total)));
            progressPanel.add(new Label((ix + 1) + " of " + total + " file(s) uploaded"));
            progressPanel.setSpacing(20);
        }
    }

    @Override
    public void onPostComplete(final int ix, DatasetBean dataset) {
        PreviewWidget preview = new PreviewWidget(dataset.getUri(), GetPreviews.SMALL, "dataset?id=" + dataset.getUri());
        statusTable.setWidget(ix, 1, preview);
        statusTable.setWidget(ix, 2, editableDatasetInfo(dataset));
        TagsWidget tags = new TagsWidget(dataset.getUri(), MMDB.dispatchAsync, false);
        statusTable.setWidget(ix, 3, tags);
        Anchor hideAnchor = new Anchor("Hide");
        hideAnchor.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                statusTable.getRowFormatter().addStyleName(ix, "hidden");
            }
        });
        statusTable.setWidget(ix, 4, hideAnchor);
        GWT.log("onPostComplete " + ix);
    }

    @Override
    public void onDropped(int ix, String filename, String sizeString) {
        Image image = new Image(PreviewWidget.GRAY_URL.get(GetPreviews.SMALL));
        statusTable.setWidget(ix, 1, image);
        int size = -1;
        try {
            size = Integer.parseInt(sizeString);
        } catch (NumberFormatException x) {
            // FIXME we don't have support for longs, so now what?
        }
        statusTable.setWidget(ix, 2, new Label("Uploading \"" + filename + "\" (" + TextFormatter.humanBytes(size) + ") ..."));
    }

    @Override
    public void onProgress(int ix, int percent) {
        statusTable.setWidget(ix, 3, new ProgressBar(percent));
    }

    static Widget editableDatasetInfo(final DatasetBean ds) {
        FlexTable layout = new FlexTable();
        int row = 0;
        layout.setWidget(row, 0, new Label("Title:"));
        final EditableLabel titleLabel = new EditableLabel(ds.getTitle());
        // FIXME this contains dispatching logic, and so should be moved to a presenter
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

}
