/**
 *
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.client.TextFormatter;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * Small info widget for a dataset.
 *
 * @author Luigi Marini
 *
 */
public class DatasetInfoWidget extends Composite {

    private final FlowPanel mainPanel;

    public DatasetInfoWidget(DatasetBean dataset) {
        mainPanel = new FlowPanel();
        mainPanel.addStyleName("datasetInfoWidget");
        initWidget(mainPanel);

        PreviewWidget thumbnail = new PreviewWidget(dataset.getUri(), GetPreviews.SMALL, "dataset?id=" + dataset.getUri());
        thumbnail.setMaxWidth(100);
        thumbnail.addStyleName("datasetInfoThumbnail");
        mainPanel.add(thumbnail);

        FlowPanel descriptionPanel = new FlowPanel();
        descriptionPanel.addStyleName("datasetInfoDescription");
        descriptionPanel.add(new Hyperlink(dataset.getTitle(), "dataset?id=" + dataset.getUri()));
        descriptionPanel.add(new Label(dataset.getCreator().getName() + " (" + dataset.getCreator().getEmail() + ")"));
        descriptionPanel.add(new Label(DateTimeFormat.getLongDateFormat().format(dataset.getDate())));
        descriptionPanel.add(new Label(TextFormatter.humanBytes(dataset.getSize())));
        descriptionPanel.add(new Label(dataset.getMimeType()));
        mainPanel.add(descriptionPanel);

        Label clearLabel = new Label();
        clearLabel.addStyleName("clearFloat");
        mainPanel.add(clearLabel);
    }
}
