/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.maps.client.InfoWindow;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.Maps;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.PermissionUtil;
import edu.illinois.ncsa.mmdb.web.client.PermissionUtil.PermissionCallback;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ConfigurationResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GeoSearch;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GeoSearchResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetConfiguration;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetGeoPoint;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetGeoPointResult;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.gis.GeoPointBean;
import edu.uiuc.ncsa.cet.bean.rbac.medici.Permission;

/**
 * Page plotting all datasets that have a geolocation on a google map.
 * TODO address potential performance concerns for very large number of points
 * 
 * @author Luigi Marini <lmarini@ncsa.illinois.edu>
 * 
 */
public class MapPage extends Page {

    private static final String TITLE = "Datasets by Location";
    private MapWidget           map;
    private Label               permissionLabel;

    public MapPage(DispatchAsync dispatch, HandlerManager eventbus) {
        super(TITLE, dispatch, eventbus);
        initialize();
    }

    private void initialize() {
        permissionLabel = new Label("We are sorry, but you don't have the proper permissions to view gelocation information.");
        mainLayoutPanel.add(permissionLabel);

        PermissionUtil rbac = new PermissionUtil(dispatchAsync);
        rbac.doIfAllowed(Permission.VIEW_LOCATION, new PermissionCallback() {
            @Override
            public void onAllowed() {
                mainLayoutPanel.remove(permissionLabel);

                dispatchAsync.execute(new GetConfiguration(MMDB.getUsername(), ConfigurationKey.GoogleMapKey), new AsyncCallback<ConfigurationResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not get GoogleMapKey", caught);
                        populate(null);
                    }

                    @Override
                    public void onSuccess(ConfigurationResult result) {
                        populate(result.getConfiguration(ConfigurationKey.GoogleMapKey));
                    }
                });
            }
        });
    }

    private void populate(String googlekey) {
        GWT.log("Loading google maps." + googlekey);

        Maps.loadMapsApi(googlekey, "2", false, new Runnable() {
            @Override
            public void run() {
                GWT.log("Loaded google maps." + Maps.getVersion());

                map = new MapWidget();
                map.setSize("950px", "500px");
                map.setUIToDefault();
                mainLayoutPanel.add(map);

                dispatchAsync.execute(new GeoSearch(), new AsyncCallback<GeoSearchResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Error geosearching in map page", caught);
                    }

                    @Override
                    public void onSuccess(GeoSearchResult result) {
                        listResults(result);
                    }
                });
            }
        });
    }

    private void listResults(GeoSearchResult result) {
        for (final String hit : result.getHits() ) {
            // get dataset bean
            dispatchAsync.execute(new GetDataset(hit), new AsyncCallback<GetDatasetResult>() {

                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Error getting bean for " + hit);
                }

                @Override
                public void onSuccess(GetDatasetResult result) {
                    final DatasetBean dataset = result.getDataset();
                    // get geopoint of dataset
                    dispatchAsync.execute(new GetGeoPoint(hit), new AsyncCallback<GetGeoPointResult>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            GWT.log("Error getting geopoint for " + hit);
                        }

                        @Override
                        public void onSuccess(GetGeoPointResult result) {
                            for (GeoPointBean geoPoint : result.getGeoPoints() ) {
                                final LatLng loc = LatLng.newInstance(geoPoint.getLatitude(), geoPoint.getLongitude());
                                final Marker marker = new Marker(loc);
                                marker.addMarkerClickHandler(new MarkerClickHandler() {

                                    @Override
                                    public void onClick(MarkerClickEvent event) {
                                        InfoWindow infoWindow = map.getInfoWindow();
                                        infoWindow.open(marker, createInfoWindowContent(dataset));
                                    }
                                });
                                map.addOverlay(marker);
                            }
                        }
                    });

                }
            });
        }
    }

    private InfoWindowContent createInfoWindowContent(DatasetBean dataset) {
        FlexTable mainPanel = new FlexTable();
        Hyperlink hyperlink = new Hyperlink(dataset.getTitle(), "dataset?id=" + dataset.getUri());
        mainPanel.setText(0, 0, "Title:");
        mainPanel.setWidget(0, 1, hyperlink);
        mainPanel.setText(1, 0, "Contributor:");
        mainPanel.setText(1, 1, dataset.getCreator().getName());
        mainPanel.setText(2, 0, "Uploaded:");
        mainPanel.setText(2, 1, DateTimeFormat.getShortDateFormat().format(dataset.getDate()));
        InfoWindowContent infoWindowContent = new InfoWindowContent(mainPanel);
        return infoWindowContent;
    }

    /* (non-Javadoc)
     * @see edu.illinois.ncsa.mmdb.web.client.ui.Page#layout()
     */
    @Override
    public void layout() {
    }
}
