/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import org.gwtopenmaps.openlayers.client.Bounds;
import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.MapOptions;
import org.gwtopenmaps.openlayers.client.MapWidget;
import org.gwtopenmaps.openlayers.client.control.LayerSwitcher;
import org.gwtopenmaps.openlayers.client.control.MousePosition;
import org.gwtopenmaps.openlayers.client.layer.OSM;
import org.gwtopenmaps.openlayers.client.layer.WMS;
import org.gwtopenmaps.openlayers.client.layer.WMSOptions;
import org.gwtopenmaps.openlayers.client.layer.WMSParams;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;

import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.PreviewGeoserverBean;

/**
 * @author Jong Lee <jonglee1@illinois.edu>
 * 
 */
public class PreviewGeoserverCollectionBeanWidget extends PreviewBeanWidget<PreviewGeoserverCollectionBean> {
    private static final String   ANCHOR_TEXT = "Geospatial Data";

    private static final String   MAX_WIDTH   = "800px";
    private static final String   MAX_HEIGHT  = "400px";

    private static final String   EPSG_900913 = "EPSG:900913";

    private final HorizontalPanel container   = new HorizontalPanel();

    private boolean               initialized;

    private MapWidget             mapWidget;

    private Map                   map;

    private OSM                   baseLayer;

    public PreviewGeoserverCollectionBeanWidget(HandlerManager eventBus) {
        super(eventBus);

        // Initialize the Composite.
        container.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        container.addStyleName("centered");

        setWidget(container);
    }

    private void initialize() {
        MapOptions defaultMapOptions = new MapOptions();
        defaultMapOptions.setProjection(EPSG_900913);
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
    public PreviewBeanWidget<PreviewGeoserverCollectionBean> newWidget() {
        return new PreviewGeoserverCollectionBeanWidget(eventBus);
    }

    @Override
    public Class<? extends PreviewBean> getPreviewBeanClass() {
        return PreviewGeoserverCollectionBean.class;
    }

    @Override
    protected void showSection() {
        if (!initialized) {
            initialize();
        }
        PreviewGeoserverCollectionBean geoCollectionBean = getPreviewBean();

        map.removeOverlayLayers();

        Bounds bbox = null;
        for (PreviewGeoserverBean geoBean : geoCollectionBean.getPreviewGeoservers() ) {
            // add wms layer from geoserver
            WMSOptions options = new WMSOptions();
            options.setProjection(EPSG_900913);
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

        map.addControl(new MousePosition());
        map.addControl(new LayerSwitcher());
        map.zoomToExtent(bbox);
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
