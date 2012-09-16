/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import org.gwtopenmaps.openlayers.client.Bounds;
import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.MapOptions;
import org.gwtopenmaps.openlayers.client.MapWidget;
import org.gwtopenmaps.openlayers.client.control.MousePosition;
import org.gwtopenmaps.openlayers.client.layer.OSM;
import org.gwtopenmaps.openlayers.client.layer.WMS;
import org.gwtopenmaps.openlayers.client.layer.WMSOptions;
import org.gwtopenmaps.openlayers.client.layer.WMSParams;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.PreviewGeoserverBean;

/**
 * @author Jong Lee <jonglee1@illinois.edu>
 * 
 */
public class PreviewGeoserverBeanWidget extends PreviewBeanWidget<PreviewGeoserverBean> {
    private static final String ANCHOR_TEXT = "Geospatial Data";

    private static final String MAX_WIDTH   = "650px";
    private static final String MAX_HEIGHT  = "400px";

    private static final String EPSG_900913 = "EPSG:900913";

    private final VerticalPanel container   = new VerticalPanel();

    private boolean             initialized;

    private MapWidget           mapWidget;

    private Label               errorMsg;

    private Map                 map;

    private OSM                 baseLayer;

    public PreviewGeoserverBeanWidget(HandlerManager eventBus) {
        super(eventBus);

        // Initialize the Composite.
        container.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        container.addStyleName("centered");

        setWidget(container);
    }

    private void initialize() {
        PreviewGeoserverBean geoBean = getPreviewBean();

        MapOptions defaultMapOptions = new MapOptions();
        if (!geoBean.getProjection().equals("-1")) {
            defaultMapOptions.setProjection(EPSG_900913);
        }
        // defaultMapOptions.setAllOverlays(true);

        mapWidget = new MapWidget(MAX_WIDTH, MAX_HEIGHT, defaultMapOptions);

        map = mapWidget.getMap();
        errorMsg = new Label();

        container.add(mapWidget);
        container.add(errorMsg);

        initialized = true;
    }

    @Override
    public PreviewBeanWidget<PreviewGeoserverBean> newWidget() {
        return new PreviewGeoserverBeanWidget(eventBus);
    }

    @Override
    public Class<? extends PreviewBean> getPreviewBeanClass() {
        return PreviewGeoserverBean.class;
    }

    @Override
    protected void showSection() {
        if (!initialized) {
            initialize();
        }
        PreviewGeoserverBean geoBean = getPreviewBean();

        String projection = geoBean.getProjection();
        if (!projection.equals("-1")) {
            baseLayer = new OSM();
            map.addLayer(baseLayer);
        } else {
            errorMsg.setText("Projection is unknown");
        }

        map.removeOverlayLayers();

        // add wms layer from geoserver
        WMSOptions options = new WMSOptions();

        if (!projection.equals("-1")) {
            options.setProjection(EPSG_900913);
        }
        options.setLayerOpacity(0.8);

        WMSParams params = new WMSParams();
        params.setTransparent(true);
        params.setLayers(geoBean.getLayerName());
        WMS wms = new WMS(geoBean.getLabel(),
                geoBean.getWmsUrl(), params,
                options);
        map.addLayer(wms);
        map.addControl(new MousePosition());

        if (!projection.equals("-1")) {
            Double[] extent = geoBean.getExtent();
            Bounds bbox = new Bounds(extent[0], extent[1], extent[2], extent[3]);

            map.zoomToExtent(bbox);
        }

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
