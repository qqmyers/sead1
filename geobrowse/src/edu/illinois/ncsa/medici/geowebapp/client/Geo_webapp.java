package edu.illinois.ncsa.medici.geowebapp.client;

import java.util.ArrayList;
import java.util.List;

import org.gwtopenmaps.openlayers.client.Bounds;
import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.MapOptions;
import org.gwtopenmaps.openlayers.client.MapWidget;
import org.gwtopenmaps.openlayers.client.Projection;
import org.gwtopenmaps.openlayers.client.control.MousePosition;
import org.gwtopenmaps.openlayers.client.layer.Layer;
import org.gwtopenmaps.openlayers.client.layer.OSM;
import org.gwtopenmaps.openlayers.client.layer.WMS;
import org.gwtopenmaps.openlayers.client.layer.WMSOptions;
import org.gwtopenmaps.openlayers.client.layer.WMSParams;
import org.gwtopenmaps.openlayers.client.util.JSObject;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.medici.geowebapp.client.event.LayerOpacityChangeEvent;
import edu.illinois.ncsa.medici.geowebapp.client.event.LayerOpacityChangeHandler;
import edu.illinois.ncsa.medici.geowebapp.client.event.LayerVisibilityChangeEvent;
import edu.illinois.ncsa.medici.geowebapp.client.event.LayerVisibilityChangeHandler;
import edu.illinois.ncsa.medici.geowebapp.client.service.MediciProxyService;
import edu.illinois.ncsa.medici.geowebapp.client.service.MediciProxyServiceAsync;
import edu.illinois.ncsa.medici.geowebapp.client.service.WmsProxyService;
import edu.illinois.ncsa.medici.geowebapp.client.service.WmsProxyServiceAsync;
import edu.illinois.ncsa.medici.geowebapp.shared.LayerInfo;

/**
 * 
 * @author Jong Lee <jonglee1@illinois.edu>
 * 
 */

public class Geo_webapp implements EntryPoint, ValueChangeHandler<String> {
	private static final String EPSG_900913 = "EPSG:900913";

	public final static String WMS_URL = "http://sead.ncsa.illinois.edu/geoserver/wms?request=GetCapabilities";
	public final static String REST_URL = "http://sead.ncsa.illinois.edu/geoserver/rest";

	private final WmsProxyServiceAsync wmsProxySvc = (WmsProxyServiceAsync) GWT
			.create(WmsProxyService.class);

	private final MediciProxyServiceAsync mediciProxySvc = (MediciProxyServiceAsync) GWT
			.create(MediciProxyService.class);

	private MapWidget mapWidget;

	private Map map;

	private Layer baseLayer;

	private SuggestBox tagTextBox;

	private String tag;

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

		Window.addResizeHandler(new ResizeHandler() {
			@Override
			public void onResize(ResizeEvent event) {
				int newWidth = RootPanel.get("map").getOffsetWidth();
				mapWidget.setWidth((newWidth - 2) + "px");
			}
		});

		mediciProxySvc.getTags(new AsyncCallback<String[]>() {
			@Override
			public void onSuccess(String[] result) {
				DecoratorPanel tagPanel = null;
				if (result != null) {
					MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
					for (String s : result) {
						oracle.add(s);
					}
					tagPanel = createTagPanel(oracle);
				} else {
					tagPanel = createTagPanel();
				}
				RootPanel.get("tag").add(tagPanel);
			}

			@Override
			public void onFailure(Throwable caught) {
				DecoratorPanel tagPanel = createTagPanel();
				RootPanel.get("tag").add(tagPanel);
			}
		});

		DecoratorPanel dp2 = createBgSwitchPanel();
		RootPanel.get("bg").add(dp2);

		History.addValueChangeHandler(this);
		History.fireCurrentHistoryState();

	}

	private void buildMapUi(String tag) {
		System.out.println("** Clean up ***");
		cleanApp();
		String encodedTag = null;
		if (tag != null)
			encodedTag = URL.encode(tag);
		wmsProxySvc.getLayers(encodedTag, new AsyncCallback<LayerInfo[]>() {
			@Override
			public void onSuccess(LayerInfo[] result) {
				if (result != null) {
					// showMap(result);
					System.out.println("** Building UI ***");
					buildGwtmap(result);

					DecoratorPanel dp = createLayerSwitcher(result);
					RootPanel.get("layers").add(dp);
					// FlowPanel hp = createLegendPanel(result);
					// RootPanel.get("info").add(hp);
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				System.out.println("ERROR");

			}
		});
	}

	private void cleanApp() {
		RootPanel.get("layers").clear();
		RootPanel.get("info").clear();
		RootPanel.get("map").clear();

	}

	protected DecoratorPanel createTagPanel(MultiWordSuggestOracle oracle) {
		DecoratorPanel dp = new DecoratorPanel();

		VerticalPanel vp = new VerticalPanel();
		vp.setSpacing(10);

		vp.add(new HTML("<h3>Filter by Tag</h3>"));
		vp.add(new HTML("<hr>"));

		HorizontalPanel hp = new HorizontalPanel();

		tagTextBox = new SuggestBox(oracle);

		if (this.tag != null)
			tagTextBox.setText(this.tag);

		Button bt = new Button("Filter");
		bt.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				History.newItem(tagTextBox.getText());
				// buildMapUi(sb.getText());
			}
		});
		hp.add(tagTextBox);
		hp.add(bt);

		vp.add(hp);
		dp.add(vp);
		return dp;
	}

	protected DecoratorPanel createTagPanel() {
		DecoratorPanel dp = new DecoratorPanel();

		VerticalPanel vp = new VerticalPanel();
		vp.setSpacing(10);

		vp.add(new HTML("<h3>Filter by Tag</h3>"));
		vp.add(new HTML("<hr>"));

		HorizontalPanel hp = new HorizontalPanel();

		final TextBox tb = new TextBox();
		tb.addKeyPressHandler(new KeyPressHandler() {

			@Override
			public void onKeyPress(KeyPressEvent event) {
				if (event.getCharCode() == KeyCodes.KEY_ENTER) {
					buildMapUi(tb.getText());
				}

			}
		});
		Button bt = new Button("Filter");
		bt.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				buildMapUi(tb.getText());
			}
		});
		hp.add(tb);
		hp.add(bt);

		vp.add(hp);
		dp.add(vp);
		return dp;
	}

	protected DecoratorPanel createBgSwitchPanel() {
		DecoratorPanel dp = new DecoratorPanel();

		HorizontalPanel hp = new HorizontalPanel();
		hp.setSpacing(10);

		ListBox lb = new ListBox();
		lb.addItem("Open Street Map", "osm");
		lb.addItem("Toner Style", "toner");
		lb.addItem("Hybrid Terrain", "terrain");

		lb.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				ListBox lb = (ListBox) event.getSource();
				String mapType = lb.getValue(lb.getSelectedIndex());
				changeBg(mapType);

			}
		});

		hp.add(new HTML("<h3>Background:</h3>"));
		hp.add(lb);
		dp.add(hp);
		return dp;
	}

	// protected FlowPanel createLegendPanel(LayerInfo[] result) {
	// List<String> layerNames = getLayerNames(result);
	// FlowPanel hp = new FlowPanel();
	// for (String n : layerNames) {
	// VerticalPanel vp = new VerticalPanel();
	// HTML label = new HTML("<b><center>" + n + "</center></b>");
	//
	// vp.add(label);
	//
	// String url =
	// "http://sead.ncsa.illinois.edu/geoserver/wms?REQUEST=GetLegendGraphic&VERSION=1.0.0&FORMAT=image/png&LAYER="
	// + n;
	// Image img = new Image(url);
	//
	// vp.add(img);
	//
	// hp.add(vp);
	//
	// }
	// return hp;
	// }

	private List<String> getLayerNames(LayerInfo[] result) {
		List<String> layerNames = new ArrayList<String>();
		for (int i = result.length - 1; i >= 0; i--) {
			layerNames.add(result[i].getName());
		}
		return layerNames;
	}

	protected DecoratorPanel createLayerSwitcher(LayerInfo[] result) {
		List<String> layerNames = getLayerNames(result);
		DecoratorPanel dp = new DecoratorPanel();
		dp.setWidth("100%");
		dp.setHeight("100%");

		VerticalPanel vp = new VerticalPanel();
		vp.setSpacing(10);

		FlexTable ft = new FlexTable();
		ft.setWidget(0, 0, new HTML("<b><center>Show?</center></b>"));
		ft.setWidget(0, 1, new HTML("<b><center>Opacity</center></b>"));
		ft.setWidget(0, 2, new HTML("<b><center>Name/Legend</center></b>"));
		// VerticalPanel vp = new VerticalPanel();
		// build layer switcher with reverse order
		// since the top layer should be on top of the list
		for (int i = result.length - 1; i >= 0; i--) {
			final String name = result[i].getName();
			int currentRow = ft.getRowCount();
			ToggleButton vizToggleButton = new ToggleButton("Off", "On");
			vizToggleButton.setValue(true);
			vizToggleButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					ToggleButton tb = (ToggleButton) event.getSource();
					boolean visibility = tb.getValue();
					eventBus.fireEvent(new LayerVisibilityChangeEvent(name,
							visibility));
				}
			});

			ft.setWidget(currentRow, 0, vizToggleButton);
			ft.getCellFormatter().setAlignment(currentRow, 0,
					HasHorizontalAlignment.ALIGN_CENTER,
					HasVerticalAlignment.ALIGN_TOP);

			ListBox opacityListBox = new ListBox();
			opacityListBox.setWidth("50px");
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
					float op = Float.parseFloat(lb.getItemText(idx));
					eventBus.fireEvent(new LayerOpacityChangeEvent(name, op));
				}
			});

			ft.setWidget(currentRow, 1, opacityListBox);
			ft.getCellFormatter().setAlignment(currentRow, 1,
					HasHorizontalAlignment.ALIGN_CENTER,
					HasVerticalAlignment.ALIGN_TOP);

			DisclosurePanel namePanel = new DisclosurePanel(name);

			// String href =
			// "http://sead.ncsa.illinois.edu/#dataset?id="+result[i].getUri();
			// namePanel.setHeader(new Anchor(name, href));

			VerticalPanel legendPanel = new VerticalPanel();
			String url = "http://sead.ncsa.illinois.edu/geoserver/wms?REQUEST=GetLegendGraphic&VERSION=1.0.0&FORMAT=image/png&LAYER="
					+ name;
			Image img = new Image(url);

			legendPanel.add(img);
			namePanel.add(legendPanel);

			ft.setWidget(currentRow, 2, namePanel);
		}
		vp.add(new HTML("<h3>Layer manager</h3>"));
		vp.add(new HTML("<hr>"));
		vp.add(ft);

		dp.add(vp);
		return dp;
	}

	// public List<String> getLayerList(String namespace, String xml) {
	// List<String> names = new ArrayList<String>();
	// Document xmlDoc = XMLParser.parse(xml);
	// NodeList layers = xmlDoc.getElementsByTagName("Layer");
	// for (int i = 0; i < layers.getLength(); i++) {
	// Node layer = layers.item(i);
	// String name = getLayerName(layer);
	// if (name.startsWith(namespace + ":")) {
	// names.add(name);
	// }
	// }
	// return names;
	// }
	//
	// public String getLayerName(Node layer) {
	// NodeList childNodes = layer.getChildNodes();
	// for (int i = 0; i < childNodes.getLength(); i++) {
	// Node e = childNodes.item(i);
	// if (e.getNodeName().toUpperCase().equals("NAME")) {
	// return e.getNodeValue();
	// }
	// }
	// return "";
	// }

	public void updateVisibility(String name, boolean v) {
		map.getLayerByName(name).setIsVisible(v);
	}

	// public native void updateVisibility(String name, boolean v) /*-{
	// layers[name].setVisibility(v);
	// }-*/;

	public void updateOpacity(String name, float opacity) {
		map.getLayerByName(name).setOpacity(opacity);
	}

	// public native void updateOpacity(String name, double opacity) /*-{
	// layers[name].setOpacity(opacity);
	// }-*/;

	public void changeBg(String mapType) {
		Layer tempBase = null;
		if (mapType.equals("osm")) {
			tempBase = new OSM();
		} else {
			tempBase = Layer.narrowToLayer(getStamenMap(mapType));

		}
		map.removeLayer(baseLayer);
		baseLayer = tempBase;
		map.addLayer(baseLayer);
		map.setBaseLayer(baseLayer);
		Layer bl = map.getBaseLayer();
		System.out.println(bl.getName());

		baseLayer.redraw();
	}

	public native JSObject getStamenMap(String mapType) /*-{
		return new $wnd.OpenLayers.Layer.Stamen(mapType);
	}-*/;

	// public native void changeBg(String maptype) /*-{
	// if (maptype == "terrian") {
	// $wnd.bg = new $wnd.OpenLayers.Layer.Stamen("terrain");
	// } else if (maptype == "toner") {
	// $wnd.bg = new $wnd.OpenLayers.Layer.Stamen("toner");
	// }
	// var bbox = new $wnd.OpenLayers.Bounds(-14376519, 2908438, -7155972,
	// 6528496);
	// $wnd.map.zoomToExtent(bbox);
	// }-*/;

	public void buildGwtmap(LayerInfo[] layerInfos) {
		MapOptions defaultMapOptions = new MapOptions();
		defaultMapOptions.setProjection(EPSG_900913);
		// defaultMapOptions.setAllOverlays(true);

		mapWidget = new MapWidget((RootPanel.get("map").getOffsetWidth() - 2)
				+ "px", "500px", defaultMapOptions);
		map = mapWidget.getMap();

		baseLayer = new OSM();
		// map.setBaseLayer(osm);
		map.addLayer(baseLayer);
		map.addControl(new MousePosition());
		Bounds box = null;
		for (LayerInfo layerInfo : layerInfos) {
			String name = layerInfo.getName();
			Bounds orgBnd = new Bounds(layerInfo.getMinx(),
					layerInfo.getMiny(), layerInfo.getMaxx(),
					layerInfo.getMaxy());
			// System.out.println("Original bounds: " + orgBnd);
			Bounds newBnd = orgBnd.transform(
					new Projection(layerInfo.getCrs()), new Projection(
							EPSG_900913));
			// System.out.println("New bounds: " + newBnd);
			System.out.println("adding layers: " + name + " " + newBnd);
			WMSOptions options = new WMSOptions();
			options.setProjection(EPSG_900913);
			options.setLayerOpacity(0.8);

			// options.setMaxExtent(newBnd);

			WMSParams params = new WMSParams();
			params.setTransparent(true);
			params.setLayers(name);
			WMS wms = new WMS(name,
					"http://sead.ncsa.illinois.edu/geoserver/wms", params,
					options);
			if (box == null)
				box = newBnd;

			box.extend(newBnd);
			// System.out.println("box: " + box);

			map.addLayer(wms);
		}
		// System.out.println(box);
		// Bounds bbox = new Bounds(-14376519.92, 2908438.48, -7155972.48,
		// 6528496.14);
		// map.zoomToMaxExtent();
		// map.addControl(new LayerSwitcher());

		RootPanel.get("map").add(mapWidget);
		if (box == null)
			map.zoomToMaxExtent();
		else
			map.zoomToExtent(box);

	}

	@Override
	public void onValueChange(ValueChangeEvent<String> event) {

		String historyToken = event.getValue();

		String[] tokens = historyToken.split("/");
		tag = null;
		if (tokens != null) {
			tag = URL.decode(tokens[0]);
		}
		if(tagTextBox != null && tag != null) {
			tagTextBox.setText(tag);
		}
		
		buildMapUi(tag);

	}

	// public Bounds getLayerExtent(Layer layer) {
	// return Bounds.narrowToBounds(getLayerExtentImpl(layer.getJSObject()));
	// }
	//
	// public native JSObject getLayerExtentImpl(JSObject layer) /*-{
	// $wnd.alert("hello " + layer.getDataExtent());
	// return layer.getDataExtent();
	// }-*/;
	//
	// public native void showMap(String layerNamesStr) /*-{
	// var wms;
	// layers = new Array();
	// $wnd.map = new $wnd.OpenLayers.Map({
	// div : 'map',
	// projection : new $wnd.OpenLayers.Projection("EPSG:900913")
	// });
	//
	// $wnd.map.addControl(new $wnd.OpenLayers.Control.MousePosition());
	// //$wnd.map.addControl(new $wnd.OpenLayers.Control.LayerSwitcher());
	// // $wnd.bg = new $wnd.OpenLayers.Layer.OSM("Open Street Map");
	// $wnd.bg = new $wnd.OpenLayers.Layer.Stamen("terrain");
	//
	// $wnd.map.addLayer($wnd.bg);
	//
	// var layerNames = layerNamesStr.split(",");
	// var infoControls = new Array();
	//
	// var defaultLayer = layerNames[layerNames.length - 1];
	//
	// for ( var i = 0; i < layerNames.length; i++) {
	// var n = layerNames[i];
	// layers[n] = new $wnd.OpenLayers.Layer.WMS(n,
	// "http://sead.ncsa.illinois.edu/geoserver/wms", {
	// layers : n,
	// transparent : true
	// }, {
	// opacity : 1.0
	// });
	// $wnd.map.addLayer(layers[n]);
	//
	// }
	//
	// var bbox = new $wnd.OpenLayers.Bounds(-14376519.92, 2908438.48,
	// -7155972.48, 6528496.14);
	// $wnd.map.zoomToExtent(bbox);
	// }-*/;

}
