/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;


import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.DownloadButton;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * Show one datasets and related information about it.
 * 
 * @author Luigi Marini
 *
 * TODO replace VerticalPanel and HorizontalPanel with FlowPanel
 */
public class DatasetWidget extends Composite {

	private final MyDispatchAsync service;
	
	private final static DateTimeFormat DATE_TIME_FORMAT = DateTimeFormat.getShortDateTimeFormat();

	private VerticalPanel mainPanel;

	private Label titleLabel;

	private Label typeLabel;

	private Label dateLabel;

	private TagsWidget tagsWidget;

	private AnnotationsWidget annotationsWidget;

	private DownloadButton downloadButton;

	private Image placeholderImage;

	private HorizontalPanel previewPanel;

	private FlowPanel metadataPanel;

	private SimplePanel imageContainer;

	private SimplePanel downloadButtonPanel;
	
	private static final String BLOB_URL = "./api/image/";
	
	private static final String DOWNLOAD_URL = "./api/image/download/";
	
	/**
	 * 
	 * @param dispatchAsync
	 */
	public DatasetWidget(MyDispatchAsync dispatchAsync) {
		this.service = dispatchAsync;
		
		mainPanel = new VerticalPanel();
		
		mainPanel.addStyleName("datasetMainContainer");
		
		initWidget(mainPanel);
		
		previewPanel = new HorizontalPanel();
		
		previewPanel.addStyleName("previewPanel");
	}
	
	/**
	 * 
	 * @param id
	 */
	public void showDataset(String id) {
		service.execute(new GetDataset(id), new AsyncCallback<GetDatasetResult>() {

			@Override
			public void onFailure(Throwable caught) {
				GWT.log("Error getting dataset", null);
			}

			@Override
			public void onSuccess(GetDatasetResult result) {
				showDataset(result.getDataset());
				
			}
		});
	}
	
	/**
	 * 
	 * @param dataset
	 */
	public void showDataset(DatasetBean dataset) {
		
		// create widgets
        placeholderImage = new Image(BLOB_URL + dataset.getUri());
        
        placeholderImage.addStyleName("imagePreview");
        
        imageContainer = new SimplePanel();
        
        imageContainer.addStyleName("imagePreviewPanel");
        
        imageContainer.add(placeholderImage);
		
		titleLabel = new Label(dataset.getTitle());
		
        titleLabel.addStyleName("datasetTitle");
		
		typeLabel = new Label("Type: " + dataset.getMimeType());
		
		typeLabel.addStyleName("metadataEntry");
		
		dateLabel = new Label("Date: " + DATE_TIME_FORMAT.format(dataset.getDate()));
		
		tagsWidget = new TagsWidget(dataset.getUri(), service);
		
		annotationsWidget = new AnnotationsWidget(dataset.getUri(), service);
		
		// TODO change to DOWNLOAD_URL once we have the proper url
        downloadButton = new DownloadButton(DOWNLOAD_URL + dataset.getUri());
        
        downloadButton.addStyleName("downloadButton");
        
        downloadButtonPanel = new SimplePanel();
        
        downloadButtonPanel.addStyleName("downloadButtonContainer");
        
        downloadButtonPanel.add(downloadButton);
        
        // lay them out
        mainPanel.add(titleLabel);
        
        mainPanel.add(previewPanel);
        
        previewPanel.add(imageContainer);
        
        metadataPanel = new FlowPanel();
        
        metadataPanel.addStyleName("metadataPreview");
        
        metadataPanel.addStyleName("alignRight");
        
        previewPanel.add(metadataPanel);
		
        metadataPanel.add(typeLabel);
		
        metadataPanel.add(dateLabel);
        
		mainPanel.add(downloadButtonPanel);
		
		mainPanel.add(tagsWidget);

		mainPanel.add(annotationsWidget);
		
	}
}
