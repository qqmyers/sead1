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

import com.google.gwt.core.client.GWT;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetGeoPoint;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetGeoPointResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.uiuc.ncsa.cet.bean.gis.GeoPointBean;

/**
 * Retrieve all geopointBeans associated with the URI and show them on a Map.
 * This widget is hidden by default unless a geopoint is shown on the map.
 * 
 * @author Rob Kooper
 * 
 */
public class LocationWidget extends Composite {
    private final FlowPanel mainPanel;
    private MapWidget       map;

    /**
     * A widget listing tags and providing a way to add a new one.
     * 
     * @param id
     * @param service
     */
    public LocationWidget(String uri, MyDispatchAsync service) {
        this(uri, service, true);
    }

    public LocationWidget(final String uri, MyDispatchAsync service, boolean withTitle) {
        // mainpanel
        mainPanel = new FlowPanel();
        mainPanel.addStyleName("datasetRightColSection");
        mainPanel.setVisible(false);
        initWidget(mainPanel);

        if (withTitle) {
            Label mapHeader = new Label("Location");
            mapHeader.addStyleName("datasetRightColHeading");
            mainPanel.add(mapHeader);
        }

        if (uri != null) {
            service.execute(new GetGeoPoint(uri), new AsyncCallback<GetGeoPointResult>() {
                @Override
                public void onFailure(Throwable arg0) {
                    GWT.log("Error retrieving geolocations for " + uri, arg0);
                }

                @Override
                public void onSuccess(GetGeoPointResult arg0) {
                    if (!arg0.getGeoPoints().isEmpty()) {
                        showPoints(arg0.getGeoPoints());
                    }
                }
            });
        }
    }

    public void showPoint(GeoPointBean bean) {
        initialize();

        MarkerOptions options = MarkerOptions.newInstance();
        options.setTitle("lat=" + bean.getLatitude() + " lon=" + bean.getLongitude() + " alt=" + bean.getAltitude());
        LatLng loc = LatLng.newInstance(bean.getLatitude(), bean.getLongitude());
        map.addOverlay(new Marker(loc, options));
        map.setCenter(loc, 15);
    }

    public void showPoints(Collection<GeoPointBean> beans) {
        initialize();

        LatLngBounds bounds = LatLngBounds.newInstance();
        for (GeoPointBean bean : beans ) {
            MarkerOptions options = MarkerOptions.newInstance();
            options.setTitle("lat=" + bean.getLatitude() + " lon=" + bean.getLongitude() + " alt=" + bean.getAltitude());
            LatLng loc = LatLng.newInstance(bean.getLatitude(), bean.getLongitude());
            bounds.extend(loc);
            map.addOverlay(new Marker(loc, options));
        }
        map.setCenter(bounds.getCenter(), map.getBoundsZoomLevel(bounds));
    }

    private void initialize() {
        if (map == null) {
            setVisible(true);

            map = new MapWidget();
            map.setSize("230px", "230px");
            map.setUIToDefault();
            mainPanel.add(map);
        }
    }
}
