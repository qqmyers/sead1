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
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
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
import edu.illinois.ncsa.medici.geowebapp.client.service.AuthenticationService;
import edu.illinois.ncsa.medici.geowebapp.client.service.AuthenticationServiceAsync;
import edu.illinois.ncsa.medici.geowebapp.client.service.MediciProxyService;
import edu.illinois.ncsa.medici.geowebapp.client.service.MediciProxyServiceAsync;
import edu.illinois.ncsa.medici.geowebapp.client.service.WmsProxyService;
import edu.illinois.ncsa.medici.geowebapp.client.service.WmsProxyServiceAsync;
import edu.illinois.ncsa.medici.geowebapp.shared.LayerInfo;
import edu.illinois.ncsa.medici.geowebapp.client.LoginPage;

/**
 * 
 * @author Jong Lee <jonglee1@illinois.edu>
 * @author Jim Myers <myersjd@umich.edu>
 * 
 */

public class Geo_webapp implements EntryPoint, ValueChangeHandler<String> {
	private static final String EPSG_900913 = "EPSG:900913";
	private static final String EPSG_4326 = "EPSG:4326";

	private static String wmsUrl = "http://localhost/geoserver/wms";
	private static String mediciUrl = "http://localhost/#dataset?id=";

	private final WmsProxyServiceAsync wmsProxySvc = (WmsProxyServiceAsync) GWT
			.create(WmsProxyService.class);

	private final MediciProxyServiceAsync mediciProxySvc = (MediciProxyServiceAsync) GWT
			.create(MediciProxyService.class);

	private final AuthenticationServiceAsync authSvc = (AuthenticationServiceAsync) GWT
			.create(AuthenticationService.class);

	private MapWidget mapWidget;

	private Map map;

	private Layer baseLayer;

	private SuggestBox tagTextBox;

	private String tag;

	private LoginStatusWidget lsw = null;

	private LoginPage lp = null;

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
				if (mapWidget != null) {
					mapWidget.setWidth((newWidth - 2) + "px");
				}
			}
		});

		History.addValueChangeHandler(this);

		authSvc.getUsername(new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				// Could not contact server
				fail();
			}

			@Override
			public void onSuccess(String result) {
				final String name = result;
				authSvc.getUrls(new AsyncCallback<String[]>() {

					@Override
					public void onSuccess(String[] result) {
						wmsUrl = result[0];
						mediciUrl = result[1];
						lsw = new LoginStatusWidget();
						RootPanel.get("loginMenu").add(lsw);
						setLoginState(name, null);
					}

					@Override
					public void onFailure(Throwable caught) {
						fail();
					}
				});

			}
		});
	}

	void setLoginState(String name, String status) {
		if (name == null) {
			// No valid user, can't be anonymous, or anonymous is
			// forbidden
			lsw.loggedOut();
			showLoginPage(status);
		} else {
			if (name.equals("anonymous")) {
				lsw.loggedOut();
			} else {
				lsw.loggedIn(name);
			}
			cleanApp();
			buildTagPanel();

		}
	}

	private void buildTagPanel() {
		mediciProxySvc.getTags(new AsyncCallback<String[]>() {
			@Override
			public void onSuccess(String[] result) {
				FlowPanel tagPanel = null;
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
				FlowPanel dp2 = createBgSwitchPanel();
				RootPanel.get("bg").add(dp2);
				History.fireCurrentHistoryState();
			}

			@Override
			public void onFailure(Throwable caught) {
				fail();
			}
		});

	}

	private void buildMapUi(String tag) {
		GWT.log("** Clean up ***");
		cleanApp();
		RootPanel.get("map").setVisible(true);
		String encodedTag = null;
		if (tag != null)
			encodedTag = URL.encode(tag);
		wmsProxySvc.getLayers(encodedTag, new AsyncCallback<LayerInfo[]>() {
			@Override
			public void onSuccess(LayerInfo[] result) {
				if (result != null) {
					// showMap(result);
					GWT.log("** Building UI ***");
					buildGwtmap(result);

					FlowPanel dp = createLayerSwitcher(result);
					RootPanel.get("layers").add(dp);
					// FlowPanel hp = createLegendPanel(result);
					// RootPanel.get("info").add(hp);
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				fail();
			}
		});
	}

	private void cleanApp() {
		RootPanel.get("layers").clear();
		RootPanel.get("info").clear();
		RootPanel.get("map").clear();
		if (lp != null) {
			lp.removeFromParent();
		}
	}

	private void showLoginPage(String status) {
		// loginStatusWidget.loggedOut();
		cleanApp();
		RootPanel.get("map").setVisible(false);
		RootPanel.get("tag").clear();
		RootPanel.get("bg").clear();
		if (lp == null) {
			lp = new LoginPage(this, status);
		}
		RootPanel.get("middle").add(lp);
	}

	public AuthenticationServiceAsync getAuthSvc() {
		return authSvc;
	}

	public LoginStatusWidget getLoginStatusWidget() {
		return lsw;
	}

	protected FlowPanel createTagPanel(MultiWordSuggestOracle oracle) {
		FlowPanel dp = new FlowPanel();

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
				History.newItem("tag_" + tagTextBox.getText());
				// buildMapUi(sb.getText());
			}
		});
		hp.add(tagTextBox);
		hp.add(bt);

		vp.add(hp);
		dp.add(vp);
		return dp;
	}

	protected FlowPanel createTagPanel() {
		FlowPanel dp = new FlowPanel();

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

	protected FlowPanel createBgSwitchPanel() {
		FlowPanel dp = new FlowPanel();

		VerticalPanel vp = new VerticalPanel();
		vp.setSpacing(10);

		vp.add(new HTML("<h3>Background map:</h3>"));
		vp.add(new HTML("<hr>"));

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

		vp.add(lb);
		dp.add(vp);
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

	protected FlowPanel createLayerSwitcher(LayerInfo[] result) {
		List<String> layerNames = getLayerNames(result);
		FlowPanel dp = new FlowPanel();

		// DecoratorPanel dp = new DecoratorPanel();

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

			VerticalPanel titlePanel = createLayerTitle(result[i]);

			// DisclosurePanel namePanel = new DisclosurePanel(name);
			//
			// // String href =
			// //
			// "http://sead.ncsa.illinois.edu/#dataset?id="+result[i].getUri();
			// // namePanel.setHeader(new Anchor(name, href));
			//
			// VerticalPanel legendPanel = new VerticalPanel();
			// String url = wmsUrl
			// +
			// "?REQUEST=GetLegendGraphic&VERSION=1.0.0&FORMAT=image/png&LAYER="
			// + name;
			// Image img = new Image(url);
			//
			// legendPanel.add(img);
			// namePanel.add(legendPanel);

			ft.setWidget(currentRow, 2, titlePanel);
		}
		vp.add(new HTML("<h3>Geospatial Datasets</h3>"));
		vp.add(new HTML("<hr>"));
		vp.add(ft);

		dp.add(vp);
		return dp;
	}

	private VerticalPanel createLayerTitle(LayerInfo layer) {
		VerticalPanel content = new VerticalPanel();

		HorizontalPanel hp = new HorizontalPanel();
		hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

		final Image close = new Image("images/rightarrow.png");
		close.setVisible(true);
		final Image open = new Image("images/downarrow.png");
		open.setVisible(false);

		String htmlString = "<a href='" + getMediciUrl() + "/#dataset?id="
				+ layer.getUri() + "' target='new'>" + layer.getName() + "</a>";
		HTML title = new HTML(htmlString);

		hp.add(close);
		hp.add(open);
		hp.add(title);

		final DisclosurePanel dp = new DisclosurePanel();
		dp.setOpen(false);

		VerticalPanel legendPanel = new VerticalPanel();
		String url = wmsUrl
				+ "?REQUEST=GetLegendGraphic&VERSION=1.0.0&FORMAT=image/png&LAYER="
				+ layer.getName();
		Image img = new Image(url);

		legendPanel.add(img);

		dp.add(legendPanel);

		content.add(hp);
		content.add(dp);

		close.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				dp.setOpen(true);
				close.setVisible(false);
				open.setVisible(true);
			}
		});

		open.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				dp.setOpen(false);
				close.setVisible(true);
				open.setVisible(false);
			}
		});

		return content;
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
		GWT.log(bl.getName());

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
				+ "px", "400px", defaultMapOptions);
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

			Bounds newBnd = orgBnd.transform(new Projection(EPSG_4326),
					new Projection(EPSG_900913));
			GWT.log("adding layers: " + name + " " + newBnd);
			WMSOptions options = new WMSOptions();
			options.setProjection(EPSG_900913);
			options.setLayerOpacity(0.8);

			// options.setMaxExtent(newBnd);

			WMSParams params = new WMSParams();
			params.setTransparent(true);
			params.setLayers(name);
			WMS wms = new WMS(name, wmsUrl, params, options);
			if (box == null)
				box = newBnd;

			box.extend(newBnd);

			map.addLayer(wms);
		}

		RootPanel.get("map").add(mapWidget);
		if (box == null)
			map.zoomToMaxExtent();
		else
			map.zoomToExtent(box);

	}

	@Override
	public void onValueChange(ValueChangeEvent<String> event) {

		String historyToken = event.getValue();
		if (historyToken.startsWith("login")) {
			History.newItem("", false);
			showLoginPage(null);

		} else if (historyToken.startsWith("logout")) {
			// push history...
			History.newItem("", false);
			tag = null;
			if (tagTextBox != null) {
				tagTextBox.setText("");
			}
			authSvc.logout(new AsyncCallback<Void>() {

				@Override
				public void onFailure(Throwable caught) {
					fail();
				}

				@Override
				public void onSuccess(Void result) {

					authSvc.getUsername(new AsyncCallback<String>() {

						@Override
						public void onFailure(Throwable caught) {
							fail();
						}

						@Override
						public void onSuccess(String result) {
							RootPanel.get("tag").clear();
							RootPanel.get("bg").clear();
							setLoginState(result, null);
							remoteLogout(new RequestCallback() {

								@Override
								public void onResponseReceived(Request request,
										Response response) {
									// TODO Auto-generated method stub
									GWT.log("RemoteLogout Success");
								}

								@Override
								public void onError(Request request,
										Throwable exception) {
									// TODO Auto-generated method stub
									GWT.log("RemoteLogout Error");
									Window.alert("Could not log out from data server.");
								}

							});
						}

					});
				}

			});
		} else {
			tag = null;

			if (historyToken.startsWith("tag_")) {
				String[] tokens = historyToken.substring("tag_".length())
						.split("/");
				if (tokens != null) {
					tag = URL.decode(tokens[0]);
				}
				if (tagTextBox != null && tag != null) {
					tagTextBox.setText(tag);
				}
			}
			buildMapUi(tag);
		}

	}

	private void fail() {
		Window.alert("Could not retrieve data from repository - try later or contact your administrator.");
		GWT.log("Service call failure");
		History.newItem("logout", true);

	}

	public static String getMediciUrl() {
		return mediciUrl;
	}

	/**
	 * logout from remote server, Set sessionID and local cache of user info to
	 * null, and log out of REST servlets
	 */
	public static void remoteLogout(RequestCallback callback) {

		String restUrl = "./api/logout";
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, restUrl);
		Request request = null;
		try {
			request = builder.sendRequest("", callback);
		} catch (RequestException x) {
			// another error condition, do something
			callback.onError(request, x);
		}
	}

}
