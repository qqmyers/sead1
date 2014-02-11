/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import org.gwtopenmaps.openlayers.client.Bounds;
import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.MapOptions;
import org.gwtopenmaps.openlayers.client.MapWidget;
import org.gwtopenmaps.openlayers.client.Style;
import org.gwtopenmaps.openlayers.client.control.LayerSwitcher;
import org.gwtopenmaps.openlayers.client.control.MousePosition;
import org.gwtopenmaps.openlayers.client.control.SelectFeature;
import org.gwtopenmaps.openlayers.client.event.VectorFeatureSelectedListener;
import org.gwtopenmaps.openlayers.client.event.VectorFeatureUnselectedListener;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.geometry.Point;
import org.gwtopenmaps.openlayers.client.layer.OSM;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.gwtopenmaps.openlayers.client.layer.WMS;
import org.gwtopenmaps.openlayers.client.layer.WMSOptions;
import org.gwtopenmaps.openlayers.client.layer.WMSParams;
import org.gwtopenmaps.openlayers.client.popup.FramedCloud;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;

import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.PreviewGeoserverBean;
import edu.uiuc.ncsa.cet.bean.gis.GeoPointBean;

/**
 * @author Jong Lee <jonglee1@illinois.edu>
 * 
 */
public class PreviewGeoCollectionBeanWidget extends PreviewBeanWidget<PreviewGeoCollectionBean> {
    private static final String   ANCHOR_TEXT = "Geospatial Data";

    private static final String   MAX_WIDTH   = "700px";
    private static final String   MAX_HEIGHT  = "400px";

    private static final String   GOOGLE      = "EPSG:900913";
    private static final String   WGS84       = "EPSG:4326";

    private final HorizontalPanel container   = new HorizontalPanel();

    private boolean               initialized;

    private MapWidget             mapWidget;

    private Map                   map;

    private OSM                   baseLayer;

    private Vector                markerLayer;

    public PreviewGeoCollectionBeanWidget(HandlerManager eventBus) {
        super(eventBus);

        // Initialize the Composite.
        container.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        container.addStyleName("centered");

        setWidget(container);
    }

    private void initialize() {
        MapOptions defaultMapOptions = new MapOptions();
        defaultMapOptions.setProjection(GOOGLE);
        // defaultMapOptions.setAllOverlays(true);

        mapWidget = new MapWidget(MAX_WIDTH, MAX_HEIGHT, defaultMapOptions);

        map = mapWidget.getMap();

        baseLayer = new OSM();
        // map.setBaseLayer(osm);
        map.addLayer(baseLayer);
        container.add(mapWidget);

        initialized = true;
    }

    @Override
    public PreviewBeanWidget<PreviewGeoCollectionBean> newWidget() {
        return new PreviewGeoCollectionBeanWidget(eventBus);
    }

    @Override
    public Class<? extends PreviewBean> getPreviewBeanClass() {
        return PreviewGeoCollectionBean.class;
    }

    @Override
    protected void showSection() {
        if (!initialized) {
            initialize();
        }
        PreviewGeoCollectionBean geoCollectionBean = getPreviewBean();
        PreviewGeoserverCollectionBean geoserverCollectionBean = geoCollectionBean.getPreviewGeoserverCollectionBean();
        PreviewGeoPointBean geoPointBean = geoCollectionBean.getPreviewGeoPointBean();

        map.removeOverlayLayers();

        Bounds bbox = null;
        if (geoserverCollectionBean != null) {
            for (PreviewGeoserverBean geoBean : geoserverCollectionBean.getPreviewGeoservers() ) {
                // add wms layer from geoserver
                WMSOptions options = new WMSOptions();
                options.setProjection(GOOGLE);
                options.setLayerOpacity(0.8);

                WMSParams params = new WMSParams();
                params.setTransparent(true);
                params.setLayers(geoBean.getLayerName());
                WMS wms = new WMS(geoBean.getLabel(),
                        geoBean.getWmsUrl(), params,
                        options);
                map.addLayer(wms);
                Double[] extent = geoBean.getExtent();
                Bounds tmpbbox = new Bounds(extent[0], extent[1], extent[2], extent[3]);
                if (bbox == null) {
                    bbox = tmpbbox;
                } else {
                    bbox.extend(tmpbbox);
                }
            }
        }

        if (geoPointBean != null) {

            Style style = new Style();
            style.setExternalGraphic("js/openlayers/img/new-marker.png");
            style.setPointRadius("12");
            style.setFillOpacity(1.0);
            markerLayer = new Vector("Geolocation Layer");

            markerLayer.addVectorFeatureSelectedListener(new VectorFeatureSelectedListener() {

                @Override
                public void onFeatureSelected(FeatureSelectedEvent eventObject) {
                    map.addPopup(eventObject.getVectorFeature().getPopup());
                }
            });
            markerLayer.addVectorFeatureUnselectedListener(new VectorFeatureUnselectedListener() {

                @Override
                public void onFeatureUnselected(FeatureUnselectedEvent eventObject) {
                    map.removePopup(eventObject.getVectorFeature().getPopup());
                }
            });

            markerLayer.setStyle(style);

            SelectFeature sf = new SelectFeature(markerLayer);
            sf.setToggle(true);
            sf.setMultiple(false);
            //            sf.sf.addFeatureHighlightedListener(new FeatureHighlightedListener() {
            //                @Override
            //                public void onFeatureHighlighted(VectorFeature vectorFeature) {
            //                    map.addPopup(vectorFeature.getPopup());
            //                }
            //            });

            for (int i = 0; i < geoPointBean.getGeoPoints().size(); i++ ) {
                GeoPointBean geoPoint = geoPointBean.getGeoPoints().get(i);
                DatasetBean dataset = geoPointBean.getDatasets().get(i);
                LonLat center = new LonLat(geoPoint.getLongitude(), geoPoint.getLatitude());
                center.transform(WGS84, GOOGLE);
                //                Size size = new Size(22, 34);
                //                Pixel offset = new Pixel(-5, -17);
                //                Icon icon = new Icon("js/openlayers/img/new-marker.png", size, offset);
                //                Marker marker = new Marker(center, icon);
                Point p = new Point(center.lon(), center.lat());
                final VectorFeature feature = new VectorFeature(p);

                FramedCloud popup = createPopup(center, dataset, feature);
                feature.setPopup(popup);

                //                marker.getEvents().register("click", marker, new EventHandler() {
                //
                //                    @Override
                //                    public void onHandle(EventObject eventObject) {
                //                        map.addPopup(popup);
                //                        popup.show();
                //                    }
                //                });
                markerLayer.addFeature(feature);

                //Non-null bounding box required
                double maxval= 20037508.34; //max value in GOOGLE coords
                double latmin = Math.max(center.lat() - 5000, -maxval); 
                        double latmax = Math.min(center.lat() + 5000, maxval);
                        
                                double lonmin = Math.max (center.lon() - 5000, -maxval); 
                                        double lonmax = Math.min(center.lon() + 5000, maxval);
                Bounds tmpbbox = new Bounds(lonmin, latmin, lonmax, latmax );
                if (bbox == null) {
                    bbox = tmpbbox;
                } else {
                    bbox.extend(tmpbbox);
                }

            }
            map.addControl(sf);
            sf.activate();
            map.addLayer(markerLayer);
        }
        map.addControl(new MousePosition());
        map.addControl(new LayerSwitcher());
        map.zoomToExtent(bbox);

    }

    public FramedCloud createPopup(LonLat loc, DatasetBean dataset, final VectorFeature feature) {

        String html = "<table border=0>";
        html += "<tr><td>Title:</td><td><a href='#dataset?id=" + dataset.getUri() + "'>" + dataset.getTitle() + "</a></td></tr>";
        html += "<tr><td>Contributor:</td><td>" + dataset.getCreator().getName() + "</td></tr>";
        html += "<tr><td>Uploaded:</td><td>" + DateTimeFormat.getShortDateFormat().format(dataset.getDate()) + "</td></tr>";
        html += "</table>";

        //        FramedCloud popup = new FramedCloud("test", loc, null, html, null, true, new CloseListener() {
        //
        //            @Override
        //            public void onPopupClose(JSObject evt) {
        //                map.removePopup(feature.getPopup());
        //
        //            }
        //        });

        FramedCloud popup = new FramedCloud("test", loc, null, html, null, false);

        return popup;

    }

    @Override
    public void setSection(String section) throws IllegalArgumentException {
        // TODO Auto-generated method stub

    }

    @Override
    public String getSection() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAnchorText() {
        return ANCHOR_TEXT;
    }

}
