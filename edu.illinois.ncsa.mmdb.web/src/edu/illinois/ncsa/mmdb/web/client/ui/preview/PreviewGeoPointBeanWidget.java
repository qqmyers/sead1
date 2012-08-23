/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;

import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.gis.GeoPointBean;

/**
 * @author Nicholas Tenczar <tenczar2@illinois.edu>
 * 
 */
public class PreviewGeoPointBeanWidget extends PreviewBeanWidget<PreviewGeoPointBean> {
    private static final String   ANCHOR_TEXT = "Geolocation";

    private static final String   MAX_WIDTH   = "500px";
    private static final String   MAX_HEIGHT  = "500px";

    private final HorizontalPanel container   = new HorizontalPanel();
    private MapWidget             map;

    private boolean               initialized;

    public PreviewGeoPointBeanWidget(HandlerManager eventBus) {
        super(eventBus);

        // Initialize the Composite.
        container.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        container.addStyleName("centered");

        setWidget(container);
    }

    private void initialize() {
        map = new MapWidget();
        map.setSize(MAX_WIDTH, MAX_HEIGHT);

        map.setUIToDefault();

        container.add(map);

        initialized = true;
    }

    @Override
    public PreviewBeanWidget<PreviewGeoPointBean> newWidget() {
        return new PreviewGeoPointBeanWidget(eventBus);
    }

    @Override
    public Class<? extends PreviewBean> getPreviewBeanClass() {
        return PreviewGeoPointBean.class;
    }

    @Override
    protected void showSection() {
        if (!initialized) {
            initialize();
        }
        map.clearOverlays();

        LatLngBounds bounds = LatLngBounds.newInstance();

        for (GeoPointBean geoPoint : getPreviewBean().getGeoPoints() ) {
            LatLng latlng = LatLng.newInstance(geoPoint.getLatitude(), geoPoint.getLongitude());
            MarkerOptions options = MarkerOptions.newInstance();

            Marker marker = new Marker(latlng, options);

            map.addOverlay(marker);

            bounds.extend(latlng);
        }

        map.setCenter(bounds.getCenter(), map.getBoundsZoomLevel(bounds));
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
