package edu.illinois.ncsa.medici.geowebapp.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

import edu.illinois.ncsa.medici.geowebapp.client.service.WmsProxyService;
import edu.illinois.ncsa.medici.geowebapp.client.service.WmsProxyServiceAsync;

/**
 * 
 * @author Jong Lee <jonglee1@illinois.edu>
 * 
 */

public class Geo_webapp implements EntryPoint {
	public final static String URL = "http://sead.ncsa.illinois.edu/geoserver/wms?request=GetCapabilities";

	private final WmsProxyServiceAsync wmsProxySvc = (WmsProxyServiceAsync) GWT
			.create(WmsProxyService.class);

	public void onModuleLoad() {
		// RequestBuilder requestBuilder = new
		// RequestBuilder(RequestBuilder.GET,
		// URL);
		//
		// try {
		// requestBuilder.sendRequest(null, new RequestCallback() {
		//
		// @Override
		// public void onResponseReceived(Request request,
		// Response response) {
		// System.out.println(response.getText());
		//
		// }
		//
		// @Override
		// public void onError(Request request, Throwable exception) {
		// System.out.println("ERROR");
		// }
		// });
		// } catch (RequestException e) {
		// e.printStackTrace();
		// }

		wmsProxySvc.getCapabilities(new AsyncCallback<String>() {

			@Override
			public void onSuccess(String result) {
				System.out.println(result);
				if (result != null)
					showMap(result);

			}

			@Override
			public void onFailure(Throwable caught) {
				System.out.println("ERROR");

			}
		});

	}

	public List<String> getLayerList(String namespace, String xml) {
		List<String> names = new ArrayList<String>();
		Document xmlDoc = XMLParser.parse(xml);
		NodeList layers = xmlDoc.getElementsByTagName("Layer");
		for (int i = 0; i < layers.getLength(); i++) {
			Node layer = layers.item(i);
			String name = getLayerName(layer);
			if (name.startsWith(namespace + ":")) {
				names.add(name);
			}
		}
		return names;
	}

	public String getLayerName(Node layer) {
		NodeList childNodes = layer.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node e = childNodes.item(i);
			if (e.getNodeName().toUpperCase().equals("NAME")) {
				return e.getNodeValue();
			}
		}
		return "";
	}

	public native void showMap(String layerNames) /*-{
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
		map.addLayer(osm);

		var layers = layerNames.split(",");
		for ( var i = 0; i < layers.length; i++) {
			var n = layers[i];
			map.addLayer(new $wnd.OpenLayers.Layer.WMS(n,
					"http://sead.ncsa.illinois.edu/geoserver/wms", {
						layers : n,
						transparent : true
					}, {
						opacity : 0.5
					}));
		}
		var bbox = new $wnd.OpenLayers.Bounds(-14376519.92, 2908438.48,
				-7155972.48, 6528496.14);
		map.zoomToExtent(bbox);

	}-*/;

}
