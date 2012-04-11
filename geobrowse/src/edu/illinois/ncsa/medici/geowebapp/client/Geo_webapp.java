package edu.illinois.ncsa.medici.geowebapp.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

import edu.illinois.ncsa.medici.geowebapp.client.event.LayerOpacityChangeEvent;
import edu.illinois.ncsa.medici.geowebapp.client.event.LayerOpacityChangeHandler;
import edu.illinois.ncsa.medici.geowebapp.client.event.LayerVisibilityChangeEvent;
import edu.illinois.ncsa.medici.geowebapp.client.event.LayerVisibilityChangeHandler;
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

	public static EventBus eventBus = GWT.create(SimpleEventBus.class);

	public void onModuleLoad() {
		eventBus.addHandler(LayerOpacityChangeEvent.TYPE,
				new LayerOpacityChangeHandler() {

					@Override
					public void onLayerOpacityChanged(
							LayerOpacityChangeEvent event) {
						updateOpacity(event.getLayerName(), event.getOpacity());
					}
				});
		eventBus.addHandler(LayerVisibilityChangeEvent.TYPE,
				new LayerVisibilityChangeHandler() {

					@Override
					public void onLayerVisibilityChanged(
							LayerVisibilityChangeEvent event) {
						updateVisibility(event.getLayerName(),
								event.getVisibility());

					}
				});

		wmsProxySvc.getCapabilities(new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				System.out.println(result);
				if (result != null) {
					showMap(result);
					VerticalPanel vp = createLayerSwitcher(result);
					RootPanel.get("layers").add(vp);
				}

			}

			@Override
			public void onFailure(Throwable caught) {
				System.out.println("ERROR");

			}
		});

	}

	protected VerticalPanel createLayerSwitcher(String result) {
		String[] layerNames = result.split(",");
		VerticalPanel vp = new VerticalPanel();
		for (final String name : layerNames) {
			HorizontalPanel hp = new HorizontalPanel();
			CheckBox visibleCheckBox = new CheckBox();
			visibleCheckBox.setValue(true);
			visibleCheckBox.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					CheckBox cb = (CheckBox) event.getSource();
					boolean visibility = cb.getValue();
					eventBus.fireEvent(new LayerVisibilityChangeEvent(name,
							visibility));
				}
			});
			hp.add(visibleCheckBox);
			
			Label nameLabel = new Label();
			nameLabel.setText(name);
			hp.add(nameLabel);

			ListBox opacityListBox = new ListBox();
			opacityListBox.addItem("1.0");
			opacityListBox.addItem("0.9");
			opacityListBox.addItem("0.8");
			opacityListBox.addItem("0.7");
			opacityListBox.addItem("0.6");
			opacityListBox.addItem("0.5");
			opacityListBox.setSelectedIndex(0);
			opacityListBox.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					ListBox lb = (ListBox) event.getSource();
					int idx = lb.getSelectedIndex();
					double op = Double.parseDouble(lb.getItemText(idx));
					eventBus.fireEvent(new LayerOpacityChangeEvent(name, op));
				}
			});

			hp.add(opacityListBox);

			vp.add(hp);
		}
		return vp;
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

	public native void updateVisibility(String name, boolean v) /*-{
		layers[name].setVisibility(v);
	}-*/;

	public native void updateOpacity(String name, double opacity) /*-{
		layers[name].setOpacity(opacity);
	}-*/;

	public native void showMap(String layerNamesStr) /*-{
		var map, osm, wms;
		layers = new Array();
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

		var layerNames = layerNamesStr.split(",");

		for ( var i = 0; i < layerNames.length; i++) {
			var n = layerNames[i];
			layers[n] = new $wnd.OpenLayers.Layer.WMS(n,
					"http://sead.ncsa.illinois.edu/geoserver/wms", {
						layers : n,
						transparent : true
					}, {
						opacity : 1.0
					});
		}
		for (key in layers) {
			map.addLayer(layers[key]);
		}
		//map.addLayers(layers);

		var bbox = new $wnd.OpenLayers.Bounds(-14376519.92, 2908438.48,
				-7155972.48, 6528496.14);
		map.zoomToExtent(bbox);

	}-*/;

}
