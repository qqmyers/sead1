/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

import edu.illinois.ncsa.mmdb.web.client.DownloadButton;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetGeoPoint;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetGeoPointResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetMetadata;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetMetadataResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.gis.GeoPointBean;

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
	private static final String PREVIEW_URL = "./api/image/preview/large/";
	private static final String DOWNLOAD_URL = "./api/image/download/";

	private PersonBean creator;

	private Label authorLabel;

	private Label sizeLabel;

	private DisclosurePanel informationPanel;

	private String id;

	private FlexTable informationTable;

    private MapWidget mapWidget;

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
		this.id = id;
		service.execute(new GetDataset(id),
				new AsyncCallback<GetDatasetResult>() {

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
	 * @param collection
	 */
	public void showDataset(DatasetBean dataset) {

		// image preview
		imageContainer = new SimplePanel();
		
		showPreview(dataset);

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

		sizeLabel = new Label("Size: " + humanBytes(dataset.getSize()));

		sizeLabel.addStyleName("metadataEntry");

		typeLabel = new Label("Type: " + dataset.getMimeType());

		typeLabel.addStyleName("metadataEntry");

		dateLabel = new Label("Date: "
				+ DATE_TIME_FORMAT.format(dataset.getDate()));

		dateLabel.addStyleName("metadataEntry");

		tagsWidget = new TagsWidget(dataset.getUri(), service);

		annotationsWidget = new AnnotationsWidget(dataset.getUri(), service);

        // map
        mapWidget = new MapWidget();
        mapWidget.setSize( "250px", "250px" );
        mapWidget.setUIToDefault();
        mapWidget.setVisible( false );
        showMap();

		// TODO change to DOWNLOAD_URL once we have the proper url
		downloadButton = new DownloadButton(DOWNLOAD_URL + dataset.getUri());

		downloadButton.addStyleName("downloadButton");

		downloadButtonPanel = new SimplePanel();

		downloadButtonPanel.addStyleName("downloadButtonContainer");

		downloadButtonPanel.add(downloadButton);
		
		// information panel with extra metadata
		informationPanel = new DisclosurePanel("Extracted Information");
		
		informationPanel.addStyleName("downloadButtonContainer");
		
		informationPanel.setAnimationEnabled(true);
		
		informationPanel.setWidth("100%");
		
		informationTable = new FlexTable();
		
		informationTable.setWidth("100%");
		
		informationPanel.add(informationTable);

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
		
		leftColumn.add(informationPanel);

		leftColumn.add(annotationsWidget);

        rightColumn.add(tagsWidget);

        rightColumn.add( mapWidget );
        
        loadMetadata();
        
	}
	
    private String humanBytes( long x )
    {
        if ( x == Integer.MAX_VALUE ) {
            return "No limit";
        }
        if ( x < 1e3 ) {
            return x + " bytes";
        } else if ( x < 1e6 ) {
            return (int)(x / 1e3 * 100) / 100.0 + " Kb";
        } else if ( x < 1e9 ) {
            return(int)(x / 1e6 * 100) / 100.0 + " Mb";
        } else if ( x < 1e12 ) {
            return (int)(x / 1e9 * 100) / 100.0 + " Gb";
        } else if ( x < 1e15 ) {
            return (int)(x / 1e12 * 100) / 100.0 + " Tb";
        } else {
            return x + " bytes";
        }
    }

	private void loadMetadata() {
		if (id != null) {
			service.execute(new GetMetadata(id), new AsyncCallback<GetMetadataResult>() {

				@Override
				public void onFailure(Throwable arg0) {
					GWT.log("Error retrieving metadata about dataset " + id, null);
					
				}

				@Override
				public void onSuccess(GetMetadataResult arg0) {
					ArrayList<ArrayList<String>> metadata = arg0.getMetadata();
					Collections.sort( metadata, new Comparator<ArrayList<String>>() {
                        @Override
                        public int compare( ArrayList<String> o1, ArrayList<String> o2 )
                        {
                            return o1.get(0).compareTo( o2.get( 0 ) );
                        }
					    
					});
					for (ArrayList<String> tuple : metadata) {
						int row = informationTable.getRowCount()+1;
						informationTable.setText(row, 0, tuple.get(0));
						informationTable.setText(row, 1, tuple.get(1));
					}
				}
			});
		}
	}

   private void showMap() {
        if ( id != null ) {
            service.execute( new GetGeoPoint( id ), new AsyncCallback<GetGeoPointResult>() {

                @Override
                public void onFailure( Throwable arg0 )
                {
                    GWT.log( "Error retrieving geolocations for " + id, arg0 );

                }

                @Override
                public void onSuccess( GetGeoPointResult arg0 )
                {
                    if (arg0.getGeoPoints().isEmpty()) {
                        return;
                    }
                                        
                    // drop marker on the map
                    LatLng center = LatLng.newInstance( 0, 0 );
                    for(GeoPointBean gpb : arg0.getGeoPoints()) {
                        MarkerOptions options = MarkerOptions.newInstance();
                        options.setTitle( "lat=" + gpb.getLatitude() +  " lon=" + gpb.getLongitude() + " alt=" + gpb.getAltitude());
                        LatLng loc = LatLng.newInstance( gpb.getLatitude(), gpb.getLongitude() );
                        mapWidget.addOverlay( new Marker( loc, options ) );
                        center = loc;
                    }

                    mapWidget.setCenter( center, 15 );
                    mapWidget.setVisible( true );
                    mapWidget.checkResizeAndCenter();
                }
            } );
        }
    }

	private void showPreview(DatasetBean dataset) {
		image = new Image(PREVIEW_URL + dataset.getUri());
		image.addStyleName("imagePreviewNoOverflow");
		imageContainer.add(image);
	}
}
