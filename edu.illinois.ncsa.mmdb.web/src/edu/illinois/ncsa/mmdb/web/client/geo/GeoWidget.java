package edu.illinois.ncsa.mmdb.web.client.geo;

import org.gwtopenmaps.openlayers.client.Bounds;
import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.MapOptions;
import org.gwtopenmaps.openlayers.client.MapWidget;
import org.gwtopenmaps.openlayers.client.Projection;
import org.gwtopenmaps.openlayers.client.Style;
import org.gwtopenmaps.openlayers.client.control.MousePosition;
import org.gwtopenmaps.openlayers.client.control.SelectFeature;
import org.gwtopenmaps.openlayers.client.event.VectorFeatureSelectedListener;
import org.gwtopenmaps.openlayers.client.event.VectorFeatureUnselectedListener;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.geometry.Point;
import org.gwtopenmaps.openlayers.client.layer.Layer;
import org.gwtopenmaps.openlayers.client.layer.OSM;
import org.gwtopenmaps.openlayers.client.layer.OSMOptions;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.gwtopenmaps.openlayers.client.layer.VectorOptions;
import org.gwtopenmaps.openlayers.client.layer.WMS;
import org.gwtopenmaps.openlayers.client.layer.WMSOptions;
import org.gwtopenmaps.openlayers.client.layer.WMSParams;
import org.gwtopenmaps.openlayers.client.popup.FramedCloud;
import org.gwtopenmaps.openlayers.client.popup.Popup;
import org.gwtopenmaps.openlayers.client.util.Attributes;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.geo.service.MediciProxyServiceAsync;
import edu.illinois.ncsa.mmdb.web.common.geo.LayerInfo;
import edu.illinois.ncsa.mmdb.web.common.geo.LocationInfo;

/**
 *
 * @author Jong Lee <jonglee1@illinois.edu>
 * @author Jim Myers <myersjd@umich.edu>
 *
 */

public class GeoWidget {
    private static final String           LOCATION_OF_DATASETS = "Location of Datasets";
    private static final String           EPSG_900913          = "EPSG:900913";
    private static final String           EPSG_4326            = "EPSG:4326";

    private static String                 wmsUrl               = null;
    private static String                 mediciUrl            = null;

    private final MediciProxyServiceAsync mediciProxySvc;

    private MapWidget                     mapWidget;

    private Map                           map;

    private Layer                         baseLayer;

    private Bounds                        mapExtent;
    private Vector                        locationLayer;

    private final int                     x;
    private final int                     y;
    private final String                  base;
    private LocationInfo[]                locations;
    private LayerInfo[]                   layers;

    public GeoWidget(MediciProxyServiceAsync mediciProxySvc, String base, int x, int y) {
        mediciUrl = GWT.getHostPageBaseURL();
        wmsUrl = GWT.getHostPageBaseURL() + "geoproxy/wms";

        this.mediciProxySvc = mediciProxySvc;
        this.x = x;
        this.y = y;
        this.base = base;
    }

    /**
     * Rebuild the map with give tag
     *
     * @param tag
     */
    public void buildMapUi(final String encodedTag, final Callback<EmptyResult, Throwable> callback) {
        mediciProxySvc.getLayers(encodedTag, new AsyncCallback<LayerInfo[]>() {
            public void onSuccess(final LayerInfo[] layers) {
                setLayers(layers);

                buildGwtmap(layers);

                // add the layer of dataset locations
                mediciProxySvc.getLocations(encodedTag,
                        new AsyncCallback<LocationInfo[]>() {
                            public void onSuccess(LocationInfo[] locations) {
                                if (locations != null) {
                                    addLocationLayer(locations);
                                }
                                setLocations(locations);
                                callback.onSuccess(null);
                            }

                            public void onFailure(Throwable caught) {
                                fail(caught);
                                callback.onFailure(caught);
                            }
                        });

            }

            public void onFailure(Throwable caught) {
                fail(caught);
                callback.onFailure(caught);
            }
        });
    }

    public void cleanMap() {
        mapWidget = null;
        map = null;
    }

    public void updateVisibility(String name, boolean v) {
        map.getLayerByName(name).setIsVisible(v);
    }

    public void updateOpacity(String name, float opacity) {
        map.getLayerByName(name).setOpacity(opacity);
    }

    /**
     * Build the map widget and initialize it
     *
     * @param layerInfos
     */
    public void buildGwtmap(LayerInfo[] layerInfos) {
        MapOptions defaultMapOptions = new MapOptions();
        defaultMapOptions.setProjection(EPSG_900913);
        // defaultMapOptions.setAllOverlays(true);

        mapWidget = new MapWidget(x
                + "px", y + "px", defaultMapOptions);
        map = mapWidget.getMap();

        baseLayer = new OSM("OpenStreetMap", new String[] {
                "//a.tile.openstreetmap.org/${z}/${x}/${y}.png",
                "//b.tile.openstreetmap.org/${z}/${x}/${y}.png",
                "//c.tile.openstreetmap.org/${z}/${x}/${y}.png", },
                new OSMOptions());
        baseLayer.setIsBaseLayer(true);
        // map.setBaseLayer(osm);
        map.addLayer(baseLayer);
        map.addControl(new MousePosition());

        mapExtent = null;

        if (layerInfos != null) {
            for (LayerInfo layerInfo : layerInfos ) {
                String name = layerInfo.getName();

                Bounds orgBnd = new Bounds(layerInfo.getMinx(),
                        layerInfo.getMiny(), layerInfo.getMaxx(),
                        layerInfo.getMaxy());
                Bounds newBnd = orgBnd;
                if (layerInfo.getSrs().startsWith("EPSG:")) {
                    newBnd = orgBnd.transform(
                            new Projection(layerInfo.getSrs()), new Projection(
                                    EPSG_900913));
                }
                WMSOptions options = new WMSOptions();
                options.setProjection(EPSG_900913);
                options.setLayerOpacity(0.8);

                // options.setMaxExtent(newBnd);

                WMSParams params = new WMSParams();
                params.setTransparent(true);
                params.setLayers(name);
                WMS wms = new WMS(name, wmsUrl, params, options);
                if (mapExtent == null) {
                    mapExtent = newBnd;
                }
                mapExtent.extend(newBnd);
                GWT.log("[layer] new extent: " + mapExtent.getLowerLeftX()
                        + "," + mapExtent.getLowerLeftY() + ","
                        + mapExtent.getUpperRightX() + ","
                        + mapExtent.getUpperRightY());
                map.addLayer(wms);
            }
        }

        RootPanel.get(base).add(mapWidget);
        if (mapExtent == null) {
            GWT.log("[layer] displaying default box because there is no layers");

            // mapExtent = defaultBox.transform(new Projection(EPSG_4326), new
            // Projection(EPSG_900913));
            mapExtent = new Bounds(-15297524.424811654, 2187929.276048484,
                    -6823884.785627671, 6731706.073556644);
        }
        GWT.log("[layer] final extent: " + mapExtent.getLowerLeftX() + ","
                + mapExtent.getLowerLeftY() + ","
                + mapExtent.getUpperRightX() + ","
                + mapExtent.getUpperRightY());

        map.zoomToExtent(mapExtent);
        mapWidget.getElement().getFirstChildElement().getStyle().setZIndex(0);
    }

    /**
     * add the layer of dataset location
     *
     * @param locations
     */
    public void addLocationLayer(LocationInfo[] locations) {
        GWT.log("** adding dataset location **");

        if (locations == null) {
            GWT.log("** no dataset location **");
            return;
        }
        // construct a vector layer from
        VectorOptions vectorOptions = new VectorOptions();
        locationLayer = new Vector(LOCATION_OF_DATASETS, vectorOptions);

        // build a style (marker) for the layer
        Style pointStyle = new Style();
        pointStyle.setExternalGraphic("images/red-marker.png");
        pointStyle.setGraphicSize(32, 37);
        pointStyle.setGraphicOffset(-16, -37); // anchor on bottom center
        pointStyle.setFillOpacity(1.0);

        for (int i = 0; i < locations.length; i++ ) {
            // add the location to the vector layer
            Point point = new Point(locations[i].getLon(),
                    locations[i].getLat());

            point.transform(new Projection(EPSG_4326), new Projection(
                    EPSG_900913));

            VectorFeature feature = new VectorFeature(point, pointStyle);
            Attributes attributes = new Attributes();
            attributes.setAttribute("title", locations[i].getTitle());
            attributes.setAttribute("uri", locations[i].getUri());
            feature.setAttributes(attributes);

            locationLayer.addFeature(feature);

            // change the map extent by the point
            mapExtent.extend(point);
            GWT.log("[location] new extent: " + mapExtent.getLowerLeftX() + ","
                    + mapExtent.getLowerLeftY() + ","
                    + mapExtent.getUpperRightX() + ","
                    + mapExtent.getUpperRightY());
        }

        // add the vector (maker) layer to the map
        map.addLayer(locationLayer);

        // now we want a popup to appear when user clicks
        // First create a select control and make sure it is activated
        SelectFeature selectFeature = new SelectFeature(locationLayer);
        selectFeature.setAutoActivate(true);
        map.addControl(selectFeature);

        // add a VectorFeatureSelectedListener to the feature to show the popup
        locationLayer
                .addVectorFeatureSelectedListener(new VectorFeatureSelectedListener() {
                    @Override
                    public void onFeatureSelected(
                            FeatureSelectedEvent eventObject) {
                        GWT.log("onFeatureSelected");

                        VectorFeature feature = eventObject.getVectorFeature();

                        // creating popup
                        String title = feature.getAttributes()
                                .getAttributeAsString("title");
                        String uri = feature.getAttributes()
                                .getAttributeAsString("uri");
                        String content = "<b><a href='" + getMediciUrl()
                                + "#dataset?id=" + uri + "' >"
                                + title + "</a></b>";

                        // close button has a bug; so turn off "close" button
                        Popup popup = new FramedCloud(feature.getFID(), feature
                                .getCenterLonLat(), null, content, null, false);
                        popup.setPanMapIfOutOfView(true);
                        popup.setAutoSize(true);
                        feature.setPopup(popup);

                        // And attach the popup to the map
                        map.addPopup(feature.getPopup());
                    }
                });
        // add a VectorFeatureUnselectedListener which removes the popup.
        locationLayer
                .addVectorFeatureUnselectedListener(new VectorFeatureUnselectedListener() {
                    public void onFeatureUnselected(
                            FeatureUnselectedEvent eventObject) {
                        GWT.log("onFeatureUnselected");
                        VectorFeature pointFeature = eventObject
                                .getVectorFeature();
                        map.removePopup(pointFeature.getPopup());
                        pointFeature.resetPopup();
                    }
                });
        map.zoomToExtent(mapExtent);
        mapWidget.getElement().getFirstChildElement().getStyle().setZIndex(0);
    }

    private void fail(Throwable caught) {
        GWT.log("Service call failure");
    }

    public static String getMediciUrl() {
        return mediciUrl;
    }

    public LayerInfo[] getLayers() {
        return layers;
    }

    public LocationInfo[] getLocations() {
        return locations;
    }

    public void setLayers(LayerInfo[] layers) {
        this.layers = layers;
    }

    public void setLocations(LocationInfo[] locations) {
        this.locations = locations;
    }
}
