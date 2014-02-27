/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010, NCSA.  All rights reserved.
 *
 * Developed by:
 * Cyberenvironments and Technologies (CET)
 * http://cet.ncsa.illinois.edu/
 *
 * National Center for Supercomputing Applications (NCSA)
 * http://www.ncsa.illinois.edu/
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the names of CET, University of Illinois/NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *******************************************************************************/
/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.Collection;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.Maps;
import com.google.gwt.maps.client.event.MapClickHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.Overlay;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.PermissionUtil;
import edu.illinois.ncsa.mmdb.web.client.PermissionUtil.PermissionCallback;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AddGeoLocation;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ClearGeoLocation;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ConfigurationResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetConfiguration;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetGeoPoint;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetGeoPointResult;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.uiuc.ncsa.cet.bean.gis.GeoPointBean;

/**
 * Retrieve all geopointBeans associated with the URI and show them on a Map.
 * This widget is hidden by default unless a geopoint is shown on the map.
 * 
 * @author Rob Kooper
 * 
 */
public class LocationWidget extends Composite {
    private final FlowPanel     mainPanel;
    private MapWidget           map;
    private final DispatchAsync service;
    private final String        uri;
    private Label               noLocationLabel;
    private Anchor              addLocationAnchor;

    /**
     * A widget listing tags and providing a way to add a new one.
     * 
     * @param id
     * @param service
     */
    public LocationWidget(String uri, DispatchAsync service) {
        this(uri, service, true);
    }

    public LocationWidget(final String uri, final DispatchAsync service, final boolean withTitle) {
        this.uri = uri;
        this.service = service;
        // mainpanel
        mainPanel = new FlowPanel();
        mainPanel.addStyleName("datasetRightColSection");
        //        mainPanel.setVisible(false);
        initWidget(mainPanel);

        if (uri != null) {
            PermissionUtil rbac = new PermissionUtil(service);
            rbac.doIfAllowed(Permission.VIEW_LOCATION, uri, new PermissionCallback() {
                @Override
                public void onAllowed() {
                    if (withTitle) {
                        Label mapHeader = new Label("Location");
                        mapHeader.addStyleName("datasetRightColHeading");
                        mainPanel.add(mapHeader);
                    }

                    service.execute(new GetConfiguration(MMDB.getUsername(), ConfigurationKey.GoogleMapKey), new AsyncCallback<ConfigurationResult>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            GWT.log("Could not get GoogleMapKey", caught);
                            loadMaps(null);
                        }

                        @Override
                        public void onSuccess(ConfigurationResult result) {
                            loadMaps(result.getConfiguration(ConfigurationKey.GoogleMapKey));
                        }
                    });
                }
            });
        }
    }

    private void loadMaps(String googlekey) {
        GWT.log("Loading google maps." + googlekey);
        Maps.loadMapsApi(googlekey, "2", false, new Runnable() {
            @Override
            public void run() {
                GWT.log("Loaded google maps." + Maps.getVersion());
                getGeoPoint();
            }
        });

    }

    private void getGeoPoint() {
        service.execute(new GetGeoPoint(uri), new AsyncCallback<GetGeoPointResult>() {
            @Override
            public void onFailure(Throwable arg0) {
                GWT.log("Error retrieving geolocations for " + uri, arg0);
            }

            @Override
            public void onSuccess(GetGeoPointResult arg0) {
                if (!arg0.getGeoPoints().isEmpty()) {
                    showPoints(arg0.getGeoPoints());
                } else {
                    showAddLocation();
                }
            }
        });
    }

    protected void showAddLocation() {
        noLocationLabel = new Label("No location set");
        mainPanel.add(noLocationLabel);
        addLocationAnchor = new Anchor("Set location");
        addLocationAnchor.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                final PickLocationPanel locationPanel = new PickLocationPanel();
                locationPanel.center();
                locationPanel.show();
                locationPanel.addCloseHandler(new CloseHandler<PopupPanel>() {

                    @Override
                    public void onClose(CloseEvent<PopupPanel> event) {
                        Marker location = locationPanel.getLocation();
                        if (location != null) {
                            submitNewLocation(location.getLatLng());
                        }
                    }
                });
            }
        });
        mainPanel.add(addLocationAnchor);
    }

    private void submitNewLocation(LatLng latLng) {
        service.execute(new AddGeoLocation(uri, latLng.getLatitude(), latLng.getLongitude()), new AsyncCallback<EmptyResult>() {

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error adding location for " + uri, caught);
            }

            @Override
            public void onSuccess(EmptyResult result) {
                mainPanel.remove(noLocationLabel);
                mainPanel.remove(addLocationAnchor);
                getGeoPoint();
            }
        });
    }

    private void showPoints(Collection<GeoPointBean> beans) {
        initialize();

        LatLngBounds bounds = LatLngBounds.newInstance();
        map.clearOverlays();
        for (GeoPointBean bean : beans ) {
            MarkerOptions options = MarkerOptions.newInstance();
            options.setTitle("lat=" + bean.getLatitude() + " lon=" + bean.getLongitude() + " alt=" + bean.getAltitude());
            LatLng loc = LatLng.newInstance(bean.getLatitude(), bean.getLongitude());
            bounds.extend(loc);
            map.addOverlay(new Marker(loc, options));
        }
        // if more then one point, set center and zoom level based on bounds
        if (beans.size() > 1) {
            map.setCenter(bounds.getCenter(), map.getBoundsZoomLevel(bounds));
        } else if (beans.size() == 1) { // if only one point, default to zoom level 5
            GeoPointBean point = beans.iterator().next();
            map.setCenter(LatLng.newInstance(point.getLatitude(), point.getLongitude()));
            map.setZoomLevel(5);
        }

        final Anchor clearLocationAnchor = new Anchor("Clear location(s)");
        clearLocationAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                service.execute(new ClearGeoLocation(uri), new AsyncCallback<EmptyResult>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Error removing location for " + uri, caught);
                    }

                    @Override
                    public void onSuccess(EmptyResult result) {
                        mainPanel.remove(clearLocationAnchor);
                        mainPanel.remove(map);
                        getGeoPoint();
                    }
                });
            }
        });
        mainPanel.add(clearLocationAnchor);
    }

    private void initialize() {
        if (map == null) {
            //            setVisible(true);

            map = new MapWidget();
            map.setSize("200px", "200px");
            map.setUIToDefault();
            mainPanel.add(map);
        }
    }

    class PickLocationPanel extends PopupPanel {
        MapWidget map;
        Marker    marker;

        public PickLocationPanel() {
            super();
            setGlassEnabled(true);
            setAnimationEnabled(true);
            FlowPanel mainPanel = new FlowPanel();
            // directions
            mainPanel.add(new Label("Pick location by clicking on the map or endtering lat/lon"));
            // map
            map = new MapWidget();
            map.setSize("500px", "500px");
            map.setUIToDefault();
            mainPanel.add(map);
            map.addMapClickHandler(new MapClickHandler() {

                @Override
                public void onClick(MapClickEvent event) {
                    MapWidget sender = event.getSender();
                    Overlay overlay = event.getOverlay();
                    LatLng point = event.getLatLng();

                    if (marker != null) {
                        sender.removeOverlay(marker);
                    }

                    if (overlay != null && marker.equals(overlay)) {
                        sender.removeOverlay(overlay);
                        marker = null;
                    } else {
                        marker = new Marker(point);
                        sender.addOverlay(marker);
                    }
                }

            });

            // container for lat lon input text box
            HorizontalPanel hpLatLon = new HorizontalPanel();
            hpLatLon.setSpacing(5);

            // lat lable and textbox
            Label latLabel = new Label("Latitude:");
            final DoubleBox latBox = new DoubleBox();
            latBox.setWidth("80px");
            hpLatLon.add(latLabel);
            hpLatLon.add(latBox);

            // lon label and textbox
            Label lonLabel = new Label("Longitude:");
            final DoubleBox lonBox = new DoubleBox();
            lonBox.setWidth("80px");
            hpLatLon.add(lonLabel);
            hpLatLon.add(lonBox);

            // button to show the marker on the map
            Button showButton = new Button("Show on Map");
            showButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {

                    if (latBox.getValue() != null && lonBox.getValue() != null) {
                        // creating lat/lng point
                        LatLng point = LatLng.newInstance(latBox.getValue(), lonBox.getValue());

                        map.clearOverlays();
                        marker = new Marker(point);
                        map.addOverlay(marker);
                        map.panTo(point);
                    }
                }
            });
            hpLatLon.add(showButton);

            // add the container to the main panel
            mainPanel.add(hpLatLon);

            // close button
            Button closeButton = new Button("Close", new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    hide();
                }
            });
            mainPanel.add(closeButton);
            setWidget(mainPanel);
        }

        public Marker getLocation() {
            return marker;
        }

    }
}
