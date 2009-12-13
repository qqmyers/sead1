/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

import edu.illinois.ncsa.mmdb.web.client.DownloadButton;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.PreviewImageBean;

/**
 * Show one datasets and related information about it.
 * 
 * @author Luigi Marini
 * 
 *         TODO replace VerticalPanel and HorizontalPanel with FlowPanel
 */
public class DatasetWidget extends Composite {

	private final MyDispatchAsync service;

	private final static DateTimeFormat DATE_TIME_FORMAT = DateTimeFormat
			.getShortDateTimeFormat();

	private final FlowPanel mainPanel;

	private Label titleLabel;

	private Label typeLabel;

	private Label dateLabel;

	private TagsWidget tagsWidget;

	private AnnotationsWidget annotationsWidget;

	private DownloadButton downloadButton;

	private Image image;

	private FlowPanel metadataPanel;

	private SimplePanel imageContainer;

	private SimplePanel downloadButtonPanel;

	private final FlowPanel leftColumn;

	private final FlowPanel rightColumn;

	private static final String BLOB_URL = "./api/image/";

	private static final String DOWNLOAD_URL = "./api/image/download/";

	private PersonBean creator;

	private Label authorLabel;

	private Label sizeLabel;

	/**
	 * 
	 * @param dispatchAsync
	 */
	public DatasetWidget(MyDispatchAsync dispatchAsync) {
		this.service = dispatchAsync;

		mainPanel = new FlowPanel();

		mainPanel.addStyleName("datasetMainContainer");

		initWidget(mainPanel);

		leftColumn = new FlowPanel();

		leftColumn.addStyleName("datasetMainContainerLeftColumn");

		mainPanel.add(leftColumn);

		rightColumn = new FlowPanel();

		rightColumn.addStyleName("datasetMainContainerRightColumn");

		mainPanel.add(rightColumn);

		// necessary so that the main conteinar wraps around the two columns
		SimplePanel clearFloat = new SimplePanel();

		clearFloat.addStyleName("clearFloat");

		mainPanel.add(clearFloat);
	}

	/**
	 * 
	 * @param id
	 */
	public void showDataset(String id) {
		service.execute(new GetDataset(id),
				new AsyncCallback<GetDatasetResult>() {

					@Override
					public void onFailure(Throwable caught) {
						GWT.log("Error getting dataset", null);
					}

					@Override
					public void onSuccess(GetDatasetResult result) {
						showDataset(result.getDataset(), result.getPreviews());

					}
				});
	}

	/**
	 * 
	 * @param dataset
	 * @param collection
	 */
	public void showDataset(DatasetBean dataset,
			Collection<PreviewImageBean> previews) {

		// image preview
		showPreview(dataset, previews);

		// metadata
		titleLabel = new Label(dataset.getTitle());

		titleLabel.addStyleName("datasetTitle");

		creator = dataset.getCreator();

		tagsWidget = new TagsWidget(dataset.getUri(), service);

		authorLabel = new Label("Author: ");

		authorLabel.addStyleName("metadataEntry");

		if (creator != null) {

			authorLabel.setTitle(creator.getEmail());

			authorLabel.setText("Author: " + creator.getName());
		}

		sizeLabel = new Label("Size: ");

		sizeLabel.addStyleName("metadataEntry");

		typeLabel = new Label("Type: " + dataset.getMimeType());

		typeLabel.addStyleName("metadataEntry");

		dateLabel = new Label("Date: "
				+ DATE_TIME_FORMAT.format(dataset.getDate()));

		dateLabel.addStyleName("metadataEntry");

		tagsWidget = new TagsWidget(dataset.getUri(), service);

		annotationsWidget = new AnnotationsWidget(dataset.getUri(), service);

		// TODO change to DOWNLOAD_URL once we have the proper url
		downloadButton = new DownloadButton(DOWNLOAD_URL + dataset.getUri());

		downloadButton.addStyleName("downloadButton");

		downloadButtonPanel = new SimplePanel();

		downloadButtonPanel.addStyleName("downloadButtonContainer");

		downloadButtonPanel.add(downloadButton);

		// layout
		leftColumn.add(titleLabel);

		leftColumn.add(imageContainer);

		metadataPanel = new FlowPanel();

		metadataPanel.addStyleName("metadataPreview");

		metadataPanel.addStyleName("alignRight");

		metadataPanel.add(authorLabel);

		metadataPanel.add(sizeLabel);

		metadataPanel.add(typeLabel);

		metadataPanel.add(dateLabel);

		rightColumn.add(metadataPanel);

		leftColumn.add(downloadButtonPanel);

		leftColumn.add(annotationsWidget);

		rightColumn.add(tagsWidget);

	}

	private void showPreview(DatasetBean dataset,
			Collection<PreviewImageBean> previews) {
		
		for (PreviewImageBean preview : previews) {
			if (preview.getWidth() == 800) {
				imageContainer = new SimplePanel();
				Image imagePreview = new Image(BLOB_URL + preview.getUri());
				imagePreview.addStyleName("imagePreviewNoOverflow");
				imageContainer.add(imagePreview);
			}
		}
		
		if (previews.isEmpty()) {
			imageContainer = new SimplePanel();
//			imageContainer.addStyleName("imagePreviewPanel");
			image = new Image(BLOB_URL + dataset.getUri());
//			image.addStyleName("imagePreview");
			image.addStyleName("imagePreviewNoOverflow");
			imageContainer.add(image);
		}
	}
}
