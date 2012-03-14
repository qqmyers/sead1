package edu.illinois.ncsa.medici.geowebapp.client;

import com.google.gwt.core.client.EntryPoint;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Geo_webapp implements EntryPoint {

	public void onModuleLoad() {
		showMap();
	}

	public native void test() /*-{
		alert("hello");
	}-*/;

	public native void showMap() /*-{
		var map, osm, wms;
		map = new $wnd.OpenLayers.Map({
			div : 'map',
			projection : new $wnd.OpenLayers.Projection("EPSG:900913"),
			units : "m",
			maxExtent : new $wnd.OpenLayers.Bounds(-14376519.92, 2908438.48,
					-7155972.48, 6528496.14)
		});

		map.addControl(new $wnd.OpenLayers.Control.MousePosition());
		map.addControl(new $wnd.OpenLayers.Control.LayerSwitcher());
		osm = new $wnd.OpenLayers.Layer.OSM("Open Street Map");
		wms = new $wnd.OpenLayers.Layer.WMS("OpenLayers WMS",
				"http://sead.ncsa.illinois.edu/geoserver/wms", {
					layers : 'topp:states',
					transparent : true
				}, {
					opacity : 0.5
				});

		map.addLayers([ osm, wms ]);
		var bbox = new $wnd.OpenLayers.Bounds(-14376519.92, 2908438.48, -7155972.48,
				6528496.14);
		map.zoomToExtent(bbox);

	}-*/;

}
