/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

import edu.illinois.ncsa.mmdb.web.client.DownloadButton;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollections;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollectionsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetGeoPoint;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetGeoPointResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetMetadata;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetMetadataResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.uiuc.ncsa.cet.bean.CollectionBean;
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

	private FlowPanel metadataPanel;

	private FlowPanel actionsPanel;

	private final FlowPanel leftColumn;

	private final FlowPanel rightColumn;

	private static final String BLOB_URL = "./api/image/";
	private static final String PREVIEW_URL = "./api/image/preview/large/";
	private static final String DOWNLOAD_URL = "./api/image/download/";
	private static final String PYRAMID_URL = "./pyramid/uri=";

	private PersonBean creator;

	private Label authorLabel;

	private Label sizeLabel;

	private DisclosurePanel informationPanel;

	private String uri;

	private FlexTable informationTable;

	private Button addToCollectionButton;

	private Label metadataHeader;

	protected CollectionMembershipWidget collectionWidget;

	private AddToCollectionDialog addToCollectionDialog;

    private MapWidget mapWidget;

	private Label mapHeader;

	private FlowPanel mapPanel;

	private AbsolutePanel previewPanel;
	
	private PreviewWidget preview;
	
	private Frame zoom;

//	private FlowPanel previewPanel;

	private FlowPanel previewControls;

	private PreviewWidget pw;

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
	 * Retrieve a specific dataset given the uri.
	 * 
	 * @param uri dataset uri
	 */
	public void showDataset(String uri) {
		this.uri = uri;
		service.execute(new GetDataset(uri),
				new AsyncCallback<GetDatasetResult>() {

					@Override
					public void onFailure(Throwable caught) {
						GWT.log("Error getting dataset", null);
					}

					@Override
					public void onSuccess(GetDatasetResult result) {
						drawPage(result.getDataset(), result.getPyramidUrl());

					}
				});
	}

	/**
	 * Draw the content on the page given a specific dataset.
	 * 
	 * @param dataset
	 * @param collection
	 */
	private void drawPage(DatasetBean dataset, String pyramidUrl) {

		// image preview
//		previewPanel(dataset.getUri());
		preview = new PreviewWidget(dataset.getUri(), GetPreviews.LARGE, null);
		
		// title
		titleLabel = new Label(dataset.getTitle());

		titleLabel.addStyleName("datasetTitle");

		// metadata
		metadataHeader = new Label("Info");

		metadataHeader.addStyleName("datasetRightColHeading");

		authorLabel = new Label("Author: ");

		authorLabel.addStyleName("metadataEntry");

		creator = dataset.getCreator();

		if (creator != null) {

			authorLabel.setTitle(creator.getEmail());

			authorLabel.setText("Author: " + creator.getName());
		}

		sizeLabel = new Label("Size: " + humanBytes(dataset.getSize()));

		sizeLabel.addStyleName("metadataEntry");

		typeLabel = new Label("Type: " + dataset.getMimeType());

		typeLabel.addStyleName("metadataEntry");

		String dateString = dataset.getDate() != null ? DATE_TIME_FORMAT.format(dataset.getDate()) : "";
		
		dateLabel = new Label("Date: "+dateString);

		dateLabel.addStyleName("metadataEntry");

		metadataPanel = new FlowPanel();

		metadataPanel.addStyleName("datasetRightColSection");

		metadataPanel.add(metadataHeader);

		metadataPanel.add(authorLabel);

		metadataPanel.add(sizeLabel);

		metadataPanel.add(typeLabel);

		metadataPanel.add(dateLabel);

		// tags
		tagsWidget = new TagsWidget(dataset.getUri(), service);

		// annotations
		annotationsWidget = new AnnotationsWidget(dataset.getUri(), service);

        // map
        showMap();

		// TODO change to DOWNLOAD_URL once we have the proper url
		downloadButton = new DownloadButton(DOWNLOAD_URL + dataset.getUri());

		downloadButton.addStyleName("downloadButton");

		addToCollectionButton = new Button("Add to collection",
				new ClickHandler() {

					@Override
					public void onClick(ClickEvent arg0) {
						showAddToCollectionDialog();
					}
				});

		actionsPanel = new FlowPanel();

		actionsPanel.addStyleName("downloadButtonContainer");

		actionsPanel.add(downloadButton);

		actionsPanel.add(addToCollectionButton);

		if(pyramidUrl != null) {
			Button zoomButton = new Button("Zoom");
			final String zoomUri = PYRAMID_URL + dataset.getUri(); 
			zoomButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					// image zoom
					zoom = new Frame(zoomUri) {
						int mouseInScreenX, mouseInScreenY, mouseInClientX, mouseInClientY;
						public void onBrowserEvent(Event event) {
							if(event.getTypeInt() == Event.ONMOUSEOVER) {
								mouseInScreenX = event.getScreenX();
								mouseInScreenY = event.getScreenY();
								mouseInClientX = event.getClientX();
								mouseInClientY = event.getClientY();
							} else if(event.getTypeInt() == Event.ONMOUSEOUT) {
								NativeEvent mouseUp = Document.get().createMouseUpEvent(0, mouseInScreenX, mouseInScreenY, mouseInClientX, mouseInClientY, false, false, false, false, Event.BUTTON_LEFT);
								getElement().dispatchEvent(mouseUp);
							}
							super.onBrowserEvent(event);
						}
					};
					zoom.sinkEvents(Event.ONMOUSEOVER);
					zoom.sinkEvents(Event.ONMOUSEOUT);
					zoom.addStyleName("datasetZoom");
					zoom.removeStyleName("gwt-Frame"); // remove frame border!
					DOM.setElementAttribute(zoom.getElement(), "frameborder", "0"); // IE
					previewPanel.clear();
					previewPanel.add(zoom);
				}
			});
			actionsPanel.add(zoomButton);
		}
		
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

		previewPanel = new AbsolutePanel();
		previewPanel.add(preview);
		previewPanel.addStyleName("downloadButtonContainer");
		
		leftColumn.add(previewPanel);

		rightColumn.add(metadataPanel);
		
		leftColumn.add(actionsPanel);
		
		leftColumn.add(informationPanel);

		leftColumn.add(annotationsWidget);

		rightColumn.add(tagsWidget);

		loadMetadata();

		loadCollections();
	}

	private void previewPanel(final String uri) {
		
//		previewPanel = new FlowPanel();
		previewPanel = new AbsolutePanel();
		
		previewPanel.addStyleName("previewPanel");
		
		previewControls = new FlowPanel();
		
		Anchor zoomLink = new Anchor("Zoom", GWT.getHostPageBaseURL()+"pyramid/uri/"+uri);
		
		zoomLink.addStyleName("actionLink");
		
		previewPanel.add(previewControls);
		
//		pw = new PreviewWidget(uri, GetPreviews.LARGE, null);
//		
//		previewPanel.add(pw);

		Anchor downloadAnchor = new Anchor("Download full size");
		downloadAnchor.addStyleName("actionLink");
		downloadAnchor.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				Window.open(DOWNLOAD_URL + uri, "_blank", "");
			}
		});
		
		Anchor addToCollectionAnchor = new Anchor("Add to collection");
		
		addToCollectionAnchor.addStyleName("actionLink");
		
		addToCollectionAnchor.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				showAddToCollectionDialog();
			}
		});
		
		actionsPanel = new FlowPanel();

		actionsPanel.add(zoomLink);
		
		actionsPanel.add(downloadAnchor);

		actionsPanel.add(addToCollectionAnchor);
		
		previewPanel.add(actionsPanel);
	}

	/**
	 * Asynchronously load the collections this dataset is part of.
	 */
	private void loadCollections() {
		service.execute(new GetCollections(uri),
				new AsyncCallback<GetCollectionsResult>() {

					@Override
					public void onFailure(Throwable arg0) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onSuccess(GetCollectionsResult arg0) {
						ArrayList<CollectionBean> collections = arg0
								.getCollections();
						if (collections.size() > 0) {
							if (collectionWidget != null) {
								rightColumn.remove(collectionWidget);
							}
							collectionWidget = new CollectionMembershipWidget();
							for (CollectionBean collection : collections) {
								collectionWidget.addCollection(collection);
							}
							rightColumn.add(collectionWidget);
						}
					}
				});
	}

	/**
	 * Popup dialog to add the dataset to a collection. User selects collection
	 * from a list box.
	 */
	protected void showAddToCollectionDialog() {
		addToCollectionDialog = new AddToCollectionDialog(service,
				new AddToCollectionHandler());
		addToCollectionDialog.center();
	}
	
	/**
	 * Format bytes.
	 * 
	 * @param x number of bytes
	 * @return formatted string
	 */
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

    /**
     * Asynchronously add the current dataset to a collection.
     * 
     * @param value
     */
	protected void addToCollection(String value) {
		GWT.log("Adding " + uri + " to collection " + value, null);
		Collection<String> datasets = new HashSet<String>();
		datasets.add(uri);
		service.execute(new AddToCollection(value, datasets),
				new AsyncCallback<AddToCollectionResult>() {

					@Override
					public void onFailure(Throwable arg0) {
						GWT.log("Error adding dataset to collection", arg0);
					}

					@Override
					public void onSuccess(AddToCollectionResult arg0) {
						GWT.log("Datasets successfully added to collection",
								null);
						loadCollections();
					}
				});
	}

	private void loadMetadata() {
		if (uri != null) {
			service.execute(new GetMetadata(uri),
					new AsyncCallback<GetMetadataResult>() {

						@Override
						public void onFailure(Throwable arg0) {
							GWT.log("Error retrieving metadata about dataset "
									+ uri, null);
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
        if ( uri != null ) {
            service.execute( new GetGeoPoint( uri ), new AsyncCallback<GetGeoPointResult>() {

                @Override
                public void onFailure( Throwable arg0 )
                {
                    GWT.log( "Error retrieving geolocations for " + uri, arg0 );

                }

                @Override
                public void onSuccess( GetGeoPointResult arg0 )
                {
                    if (arg0.getGeoPoints().isEmpty()) {
                        return;
                    }
                           
                    mapWidget = new MapWidget();
                    mapWidget.setSize( "230px", "230px" );
                    mapWidget.setUIToDefault();
                    mapWidget.setVisible( false );

            		mapPanel = new FlowPanel();
            		mapPanel.addStyleName("datasetRightColSection");
               		mapHeader = new Label("Location");
            		mapHeader.addStyleName("datasetRightColHeading");
            		mapPanel.add(mapHeader);
                    mapPanel.add(mapWidget);
                    rightColumn.add(mapPanel);
             
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

	class AddToCollectionHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent arg0) {
			String value = addToCollectionDialog.getSelectedValue();
			if (value != null) {
				GWT.log("Adding " + uri + " to collection " + value, null);
				Collection<String> datasets = new HashSet<String>();
				datasets.add(uri);
				service.execute(new AddToCollection(value, datasets),
						new AsyncCallback<AddToCollectionResult>() {

							@Override
							public void onFailure(Throwable arg0) {
								GWT.log("Error adding dataset to collection",
										arg0);
							}

							@Override
							public void onSuccess(AddToCollectionResult arg0) {
								GWT
										.log(
												"Datasets successfully added to collection",
												null);
								addToCollectionDialog.hide();
								loadCollections();
							}
						});
			}
		}
	}
}
