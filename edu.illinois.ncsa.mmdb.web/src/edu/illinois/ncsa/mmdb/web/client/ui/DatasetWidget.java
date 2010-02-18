/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDerivedFrom;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDerivedFromResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetGeoPoint;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetGeoPointResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetMetadata;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetMetadataResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.dispatch.Metadata;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetProperty;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetPropertyResult;
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
@SuppressWarnings("nls")
public class DatasetWidget extends Composite {

	private final MyDispatchAsync service;

	private final static DateTimeFormat DATE_TIME_FORMAT = DateTimeFormat
			.getShortDateTimeFormat();

	private final FlowPanel mainPanel;

	private EditableLabel titleLabel;

	private Label typeLabel;

	private Label dateLabel;

	private TagsWidget tagsWidget;

	private AnnotationsWidget annotationsWidget;

	private FlowPanel metadataPanel;

	private FlowPanel actionsPanel;

	private final FlowPanel leftColumn;

	private final FlowPanel rightColumn;

	private static final String BLOB_URL = "./api/image/";
	private static final String PREVIEW_URL = "./api/image/preview/large/";
	private static final String DOWNLOAD_URL = "./api/image/download/";
	private static final String PYRAMID_URL = "./pyramid/";

	private PersonBean creator;

	private Label authorLabel;

	private Label sizeLabel;

	private String uri;

	private FlexTable informationTable;

	private Label metadataHeader;

	protected CollectionMembershipWidget collectionWidget;
	
	protected DerivedDatasetsWidget derivedDatasetsWidget;

    private MapWidget mapWidget;

	private Label mapHeader;

	private FlowPanel mapPanel;

	private AbsolutePanel previewPanel;
	
	private PreviewWidget preview;

	private FlowPanel previewControls;

	private Anchor downloadAnchor;


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

	@Override
	protected void onUnload() {
	    super.onUnload();
	    hideSeadragon( );
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
						drawPage(result.getDataset(), result.getPyramid());

					}
				});
	}

	/**
	 * Draw the content on the page given a specific dataset.
	 * 
	 * @param dataset
	 * @param collection
	 */
    private void drawPage(final DatasetBean dataset, String pyramid) {

		// image preview
		preview = new PreviewWidget(dataset.getUri(), GetPreviews.LARGE, null);
		
		// title
		titleLabel = new EditableLabel(dataset.getTitle());

		titleLabel.getLabel().addStyleName("datasetTitle");
		
		titleLabel.addValueChangeHandler(new ValueChangeHandler<String>() {
			public void onValueChange(final ValueChangeEvent<String> event) {
				SetProperty change = new SetProperty(dataset.getUri(), "http://purl.org/dc/elements/1.1/title", event.getValue());
				service.execute(change, new AsyncCallback<SetPropertyResult>() {
					public void onFailure(Throwable caught) {
						titleLabel.cancel();
					}
					public void onSuccess(SetPropertyResult result) {
						titleLabel.setText(event.getValue());
					}
				});
			}
		});

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

        downloadAnchor = new Anchor();
		downloadAnchor.setHref(DOWNLOAD_URL + dataset.getUri());
		downloadAnchor.setText("Download full size");
		downloadAnchor.setTarget("_blank");
		downloadAnchor.addStyleName("datasetActionLink");
        downloadAnchor.addStyleName("datasetActionLink");

		actionsPanel = new FlowPanel();

		actionsPanel.addStyleName("datasetActions");

		actionsPanel.add(downloadAnchor);

        if ( pyramid != null ) {
        	
            final Anchor zoomAnchor = new Anchor( "Zoom" );
            
            zoomAnchor.addStyleName("datasetActionLink");
            
            final String zoomUri = PYRAMID_URL + pyramid + "/xml";
            
            zoomAnchor.addClickHandler( new ClickHandler() {
                public void onClick( ClickEvent event )
                {
                    previewPanel.clear();
                    if ( zoomAnchor.getText().equals( "Zoom" ) ) {
                        Label seadragon = new Label();
                        seadragon.addStyleName( "seadragon" );
                        previewPanel.add( seadragon );
                        seadragon.getElement().setId( "seadragon" );
                        zoomAnchor.setText( "Preview" );
                        showSeadragon( seadragon.getElement().getId(), zoomUri );
                    } else {
                        hideSeadragon( );
                        previewPanel.add( preview );
                        zoomAnchor.setText( "Zoom" );
                    }
                }
            } );
            
            actionsPanel.add( zoomAnchor );
        }

		informationTable = new FlexTable();
		
		informationTable.addStyleName("metadataTable");

		informationTable.setWidth("100%");
		
		UserMetadataWidget um = new UserMetadataWidget(dataset.getUri(), service);
		um.setWidth("100%");
		DisclosurePanel additionalInformationPanel = new DisclosurePanel("Additional Information");
		additionalInformationPanel.addStyleName("additionalInformation");
		additionalInformationPanel.setOpen(false);
		additionalInformationPanel.setAnimationEnabled(true);
		VerticalPanel verticalPanel = new VerticalPanel();
		verticalPanel.add(new HTML("<b>From User</b>"));
		verticalPanel.add(um);
		verticalPanel.add(new HTML("<b>Extracted</b>"));
		verticalPanel.add(informationTable);
		additionalInformationPanel.add(verticalPanel);

		// layout
		leftColumn.add(titleLabel);

		previewPanel = new AbsolutePanel();
		
		previewPanel.add(preview);
		
		previewPanel.addStyleName("previewPanel");
		
		leftColumn.add(previewPanel);

		rightColumn.add(metadataPanel);
		
		leftColumn.add(actionsPanel);
		
		leftColumn.add(additionalInformationPanel);
		
		leftColumn.add(annotationsWidget);

		rightColumn.add(tagsWidget);

		loadMetadata();

		loadCollections();
		
		loadDerivedFrom(uri,4);
	}

    public final native void showSeadragon( String container, String url ) /*-{
        $wnd.Seadragon.Config.debug = true;
        $wnd.Seadragon.Config.imagePath = "img/";
        $wnd.Seadragon.Config.autoHideControls = true;

        // close existing viewer
        if ($wnd.viewer) {
            $wnd.viewer.setFullPage(false);
            $wnd.viewer.setVisible(false);
            $wnd.viewer.close();
            $wnd.viewer = null;            
        }

        // open with new url
        if (url != null) {
            $wnd.viewer = new $wnd.Seadragon.Viewer(container);
            $wnd.viewer.openDzi(url);
        }
    }-*/;

    public final native void hideSeadragon() /*-{
        // hide the current viewer if open
        if ($wnd.viewer) {
            $wnd.viewer.setFullPage(false);
            $wnd.viewer.setVisible(false);
            $wnd.viewer.close();
            $wnd.viewer = null;            
        }
    }-*/;

	/**
	 * Asynchronously load the collections this dataset is part of.
	 */
	private void loadCollections() {
		
		collectionWidget = new CollectionMembershipWidget(service, uri);

		rightColumn.add(collectionWidget);
	}


	private void loadDerivedFrom(final String uri, final int level) {
		service.execute(new GetDerivedFrom(uri),
				new AsyncCallback<GetDerivedFromResult>() {
					@Override
					public void onFailure(Throwable arg0) {
						// TODO Auto-generated method stub
					}

					@Override
					public void onSuccess(GetDerivedFromResult arg0) {
						List<DatasetBean> df = arg0.getDerivedFrom();
						if (df.size() > 0) {
							if(derivedDatasetsWidget == null) {
								derivedDatasetsWidget = new DerivedDatasetsWidget();
								rightColumn.add(derivedDatasetsWidget);
							}
							if (derivedDatasetsWidget != null) {
								for(DatasetBean d : df) {
									derivedDatasetsWidget.addDataset(d);
									if(level > 0) {
										loadDerivedFrom(d.getUri(),level-1);
									}
								}
							}
						}
					}
				});
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
					List<Metadata> metadata = arg0.getMetadata();
					Collections.sort( metadata);
					String category = "";
					for (Metadata tuple : metadata) {
					    if (!category.equals( tuple.getCategory() )) {					        
	                        int row = informationTable.getRowCount()+1;
	                        informationTable.setHTML(row, 0, "<b>" + tuple.getCategory() + "</b>");
	                        informationTable.setText(row, 1, ""); //$NON-NLS-1$
	                        informationTable.getFlexCellFormatter().addStyleName(row, 0, "metadataTableCell");
	                        category = tuple.getCategory();
					    }
						int row = informationTable.getRowCount();
						informationTable.setText(row, 0, tuple.getLabel());
						informationTable.setText(row, 1, tuple.getValue());
						
						// formatting
						informationTable.getFlexCellFormatter().addStyleName(row, 0, "metadataTableCell");
						informationTable.getFlexCellFormatter().addStyleName(row, 1, "metadataTableCell");
						if (row % 2 == 0) {
							informationTable.getRowFormatter().addStyleName(row, "metadataTableEvenRow");
						} else {
							informationTable.getRowFormatter().addStyleName(row, "metadataTableOddRow");
						}
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
}
