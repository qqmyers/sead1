package edu.illinois.ncsa.medici.geowebapp.client;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtopenmaps.openlayers.client.Bounds;
import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.MapOptions;
import org.gwtopenmaps.openlayers.client.MapWidget;
import org.gwtopenmaps.openlayers.client.Projection;
import org.gwtopenmaps.openlayers.client.Style;
import org.gwtopenmaps.openlayers.client.control.MousePosition;
import org.gwtopenmaps.openlayers.client.control.SelectFeature;
import org.gwtopenmaps.openlayers.client.event.VectorFeatureSelectedListener;
import org.gwtopenmaps.openlayers.client.event.VectorFeatureUnselectedListener;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.geometry.Point;
import org.gwtopenmaps.openlayers.client.layer.Layer;
import org.gwtopenmaps.openlayers.client.layer.OSM;
import org.gwtopenmaps.openlayers.client.layer.OSMOptions;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.gwtopenmaps.openlayers.client.layer.VectorOptions;
import org.gwtopenmaps.openlayers.client.layer.WMS;
import org.gwtopenmaps.openlayers.client.layer.WMSOptions;
import org.gwtopenmaps.openlayers.client.layer.WMSParams;
import org.gwtopenmaps.openlayers.client.popup.FramedCloud;
import org.gwtopenmaps.openlayers.client.popup.Popup;
import org.gwtopenmaps.openlayers.client.util.Attributes;
import org.gwtopenmaps.openlayers.client.util.JSObject;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
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
import edu.illinois.ncsa.medici.geowebapp.shared.LocationInfo;

/**
 * 
 * @author Jong Lee <jonglee1@illinois.edu>
 * @author Jim Myers <myersjd@umich.edu>
 * 
 */

public class Geo_webapp implements EntryPoint, ValueChangeHandler<String> {
	Logger logger = Logger.getLogger(this.getClass().getName());
	private static final String LOCATION_OF_DATASETS = "Location of Datasets";
	private static final String EPSG_900913 = "EPSG:900913";
	private static final String EPSG_4326 = "EPSG:4326";

	private static String wmsUrl = "http://localhost/geoserver/wms";
	private static String mediciUrl = "http://localhost/acr";

	private static final Bounds defaultBox = new Bounds(-137.42, 19.28, -61.30,
			51.62);

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
	protected FlowPanel layerSwitcher;
	private String encodedTag;
	private Bounds mapExtent;
	private Vector locationLayer;

	public static EventBus eventBus = GWT.create(SimpleEventBus.class);

	public void onModuleLoad() {
		eventBus.addHandler(LayerOpacityChangeEvent.TYPE,
				new LayerOpacityChangeHandler() {

					public void onLayerOpacityChanged(
							LayerOpacityChangeEvent event) {
						updateOpacity(event.getLayerName(), event.getOpacity());
					}
				});
		eventBus.addHandler(LayerVisibilityChangeEvent.TYPE,
				new LayerVisibilityChangeHandler() {

					public void onLayerVisibilityChanged(
							LayerVisibilityChangeEvent event) {
						updateVisibility(event.getLayerName(),
								event.getVisibility());

					}
				});

		Window.addResizeHandler(new ResizeHandler() {
			public void onResize(ResizeEvent event) {
				int newWidth = RootPanel.get("map").getOffsetWidth();
				if (mapWidget != null) {
					mapWidget.setWidth((newWidth - 2) + "px");
				}
			}
		});

		History.addValueChangeHandler(this);
		lsw = new LoginStatusWidget();
		RootPanel.get("loginMenu").add(lsw);

		authSvc.getUsername(new AsyncCallback<String>() {

			public void onFailure(Throwable caught) {
				// Could not contact server or anon not allowed
				setLoginState(null, "Login required for access");
			}

			public void onSuccess(String result) {
				/*
				 * If we have a name, need to verify that remote server agrees /
				 * that a session w/the remote server also exists. If yes and
				 * names match, we're good. If yes and names don't match, we
				 * should logout and start again (two users in same browser) If
				 * no, and local name is anonymous, remotelogin as anonymous.
				 */
				final String name = result;
				authSvc.getUrls(new AsyncCallback<String[]>() {

					public void onSuccess(String[] result) {
						wmsUrl = result[0];
						mediciUrl = result[1];
						/*
						 * Call the REST checkLogin endpoint to verify the user
						 * is logged in (session cookie is valid) and return the
						 * user's ID for local use.
						 */
						String restUrl = getMediciUrl() + "/api/checkLogin";
						RequestBuilder builder = new RequestBuilder(
								RequestBuilder.GET, restUrl);
						builder.setIncludeCredentials(true);
						try {
							GWT.log("checking login status @ " + restUrl, null);
							// we need to block.
							builder.sendRequest("", new RequestCallback() {
								public void onError(Request request,
										Throwable exception) {
									fail();
								}

								public void onResponseReceived(Request request,
										Response response) {
									// success!
									GWT.log("REST auth status code = "
											+ response.getStatusCode(), null);
									if (response.getStatusCode() > 300) {
										GWT.log("not authenticated to REST services",
												null);
										if (name.equals("anonymous")) {
											LoginPage
													.remoteLogin(
															name,
															"none",
															new AsyncCallback<String>() {
																@Override
																public void onSuccess(
																		String name) {
																	setLoginState(
																			name,
																			null);
																}

																@Override
																public void onFailure(
																		Throwable caught) {
																	setLoginState(
																			null,
																			"Login required for access");
																}
															});
										} else {
											setLoginState(null,
													"Login required for access");

										}

									} else {
										String userid = response.getText();
										// FixMe! Don't assume name/id mapping
										// here
										if (userid
												.equals("http://cet.ncsa.uiuc.edu/2007/person/"
														+ name)) {
											setLoginState(name, null);
										} else {
											if (name.equals("anonymous")) {
												LoginPage
														.remoteLogin(
																name,
																"none",
																new AsyncCallback<String>() {
																	@Override
																	public void onSuccess(
																			String name) {
																		setLoginState(
																				name,
																				null);
																	}

																	@Override
																	public void onFailure(
																			Throwable caught) {
																		setLoginState(
																				null,
																				"Login required for access");
																	}
																});
											} else {
												setLoginState(null,
														"Login required for access");

											}
										}

									}
								}
							});
						} catch (RequestException x) {
							fail();
						}

					}

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
			public void onSuccess(String[] result) {
				FlowPanel tagPanel = null;
				MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
				if (result != null) {
					for (String s : result) {
						oracle.add(s);
					}
				}

				tagPanel = createTagPanel(oracle);

				RootPanel.get("tag").add(tagPanel);
				History.fireCurrentHistoryState();
			}

			public void onFailure(Throwable caught) {
				fail();
			}
		});

	}

	/**
	 * Rebuild the map with give tag
	 * 
	 * @param tag
	 */
	private void buildMapUi(String tag) {
		cleanApp();
		RootPanel.get("map").setVisible(true);
		encodedTag = null;
		if (tag != null)
			encodedTag = URL.encode(tag);
		mediciProxySvc.getLayers(encodedTag, new AsyncCallback<LayerInfo[]>() {
			public void onSuccess(final LayerInfo[] layers) {
				// showMap(result);
				logger.log(Level.INFO, "** Building UI ***");
				buildGwtmap(layers);

				// add the layer of dataset locations
				mediciProxySvc.getLocations(encodedTag,
						new AsyncCallback<LocationInfo[]>() {
							public void onSuccess(LocationInfo[] locations) {
								if (locations != null) {
									addLocationLayer(locations);
								}

								layerSwitcher = createLayerSwitcher(layers,
										locations);
								RootPanel.get("layers").add(layerSwitcher);
							}

							public void onFailure(Throwable caught) {
								fail();
							}
						});

			}

			public void onFailure(Throwable caught) {
				fail();
			}
		});
	}

	private void cleanApp() {
		logger.log(Level.INFO, "Cleaning up for refresh");
		mapWidget = null;
		map = null;
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

	/**
	 * Create tag panel with the word suggestion
	 * 
	 * @param oracle
	 * @return
	 */
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
			}
		});
		hp.add(tagTextBox);
		hp.add(bt);

		vp.add(hp);
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

	protected FlowPanel createLayerSwitcher(LayerInfo[] layers,
			LocationInfo[] locations) {
		// List<String> layerNames = getLayerNames(result);
		FlowPanel dp = new FlowPanel();

		// DecoratorPanel dp = new DecoratorPanel();

		dp.setWidth("100%");
		dp.setHeight("100%");

		VerticalPanel vp = new VerticalPanel();
		vp.setSpacing(10);

		vp.add(new HTML("<h3>Geospatial Datasets</h3>"));
		vp.add(new HTML("<hr>"));

		FlexTable ft = new FlexTable();

		// if there is geospatial dataset, OR locations, then add the table
		if (locations != null || layers != null) {
			ft.setWidget(0, 0, new HTML("<b><center>Show?</center></b>"));
			ft.setWidget(0, 1, new HTML("<b><center>Opacity</center></b>"));
			ft.setWidget(0, 2, new HTML("<b><center>Name/Legend</center></b>"));
		} else {
			// if no locations, layers, then display message
			vp.add(new HTML("<h4><i>No geospatial datasets</i></h4>"));
			dp.add(vp);
			return dp;
		}

		if (locations != null) {
			// add location layer row
			VerticalPanel locationTitlePanel = createLocationLayerTitle();
			addLayerRow(ft, LOCATION_OF_DATASETS, locationTitlePanel);
		}

		// if not, add the message
		if (layers != null) {
			if (layers.length > 0) {

				// VerticalPanel vp = new VerticalPanel();
				// build layer switcher with reverse order
				// since the top layer should be on top of the list

				for (int i = layers.length - 1; i >= 0; i--) {
					LayerInfo layerInfo = layers[i];
					VerticalPanel titlePanel = createLayerTitle(
							layerInfo.getUri(), layerInfo.getTitle(),
							layerInfo.getName());
					addLayerRow(ft, layerInfo.getName(), titlePanel);
				}
			}
		}

		vp.add(ft);
		dp.add(vp);
		return dp;
	}

	private void addLayerRow(FlexTable ft, final String name,
			VerticalPanel titlePanel) {
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

		ft.setWidget(currentRow, 2, titlePanel);
	}

	private VerticalPanel createLayerTitle(String uri, String title, String name) {
		VerticalPanel content = new VerticalPanel();

		HorizontalPanel hp = new HorizontalPanel();
		hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

		final Image close = new Image("images/rightarrow.png");
		close.setVisible(true);
		final Image open = new Image("images/downarrow.png");
		open.setVisible(false);

		String htmlString = "<a href='" + getMediciUrl() + "/#dataset?id="
				+ uri + "' target='new'>" + title + "</a>";
		HTML htmltitle = new HTML(htmlString);

		hp.add(close);
		hp.add(open);
		hp.add(htmltitle);

		final DisclosurePanel dp = new DisclosurePanel();
		dp.setOpen(false);

		VerticalPanel legendPanel = new VerticalPanel();
		String url = wmsUrl
				+ "?REQUEST=GetLegendGraphic&VERSION=1.0.0&FORMAT=image/png&LAYER="
				+ name;
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

	private VerticalPanel createLocationLayerTitle() {
		VerticalPanel content = new VerticalPanel();

		HorizontalPanel hp = new HorizontalPanel();
		hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

		final Image close = new Image("images/rightarrow.png");
		close.setVisible(true);
		final Image open = new Image("images/downarrow.png");
		open.setVisible(false);

		String htmlString = "<a href='" + getMediciUrl()
				+ "/#map' target='new'>" + LOCATION_OF_DATASETS + "</a>";
		HTML htmltitle = new HTML(htmlString);

		hp.add(close);
		hp.add(open);
		hp.add(htmltitle);

		final DisclosurePanel dp = new DisclosurePanel();
		dp.setOpen(false);

		VerticalPanel legendPanel = new VerticalPanel();
		Image img = new Image("images/red-marker.png");

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

	/**
	 * Build the map widget and initialize it
	 * 
	 * @param layerInfos
	 */
	public void buildGwtmap(LayerInfo[] layerInfos) {
		MapOptions defaultMapOptions = new MapOptions();
		defaultMapOptions.setProjection(EPSG_900913);
		// defaultMapOptions.setAllOverlays(true);

		mapWidget = new MapWidget((RootPanel.get("map").getOffsetWidth() - 2)
				+ "px", "400px", defaultMapOptions);
		map = mapWidget.getMap();

		baseLayer = new OSM("OpenStreetMap", new String[] {
				"//a.tile.openstreetmap.org/${z}/${x}/${y}.png",
				"//b.tile.openstreetmap.org/${z}/${x}/${y}.png",
				"//c.tile.openstreetmap.org/${z}/${x}/${y}.png", },
				new OSMOptions());
		baseLayer.setIsBaseLayer(true);
		// map.setBaseLayer(osm);
		map.addLayer(baseLayer);
		map.addControl(new MousePosition());

		mapExtent = null;

		if (layerInfos != null) {
			for (LayerInfo layerInfo : layerInfos) {
				String name = layerInfo.getName();
				Bounds orgBnd = new Bounds(layerInfo.getMinx(),
						layerInfo.getMiny(), layerInfo.getMaxx(),
						layerInfo.getMaxy());
				Bounds newBnd = orgBnd;
				if (layerInfo.getSrs().startsWith("EPSG:")) {
					newBnd = orgBnd.transform(
							new Projection(layerInfo.getSrs()), new Projection(
									EPSG_900913));
				}
				logger.log(Level.INFO, "adding layers: " + name + " " + newBnd);
				WMSOptions options = new WMSOptions();
				options.setProjection(EPSG_900913);
				options.setLayerOpacity(0.8);

				// options.setMaxExtent(newBnd);

				WMSParams params = new WMSParams();
				params.setTransparent(true);
				params.setLayers(name);
				WMS wms = new WMS(name, wmsUrl, params, options);
				if (mapExtent == null)
					mapExtent = newBnd;
				mapExtent.extend(newBnd);
				logger.log(
						Level.INFO,
						"[layer] new extent: " + mapExtent.getLowerLeftX()
								+ "," + mapExtent.getLowerLeftY() + ","
								+ mapExtent.getUpperRightX() + ","
								+ mapExtent.getUpperRightY());

				map.addLayer(wms);
			}
		}

		RootPanel.get("map").add(mapWidget);

		if (mapExtent == null) {
			logger.log(Level.INFO,
					"[layer] displaying default box because there is no layers");

			// mapExtent = defaultBox.transform(new Projection(EPSG_4326), new
			// Projection(EPSG_900913));
			mapExtent = new Bounds(-15297524.424811654, 2187929.276048484,
					-6823884.785627671, 6731706.073556644);
		}
		logger.log(
				Level.INFO,
				"[layer] final extent: " + mapExtent.getLowerLeftX() + ","
						+ mapExtent.getLowerLeftY() + ","
						+ mapExtent.getUpperRightX() + ","
						+ mapExtent.getUpperRightY());

		map.zoomToExtent(mapExtent);
		mapWidget.getElement().getFirstChildElement().getStyle().setZIndex(0);

	}

	/**
	 * add the layer of dataset location
	 * 
	 * @param locations
	 */
	public void addLocationLayer(LocationInfo[] locations) {
		logger.log(Level.INFO, "** adding dataset location **");

		if (locations == null) {
			logger.log(Level.INFO, "** no dataset location **");
			return;
		}
		// construct a vector layer from
		VectorOptions vectorOptions = new VectorOptions();
		locationLayer = new Vector(LOCATION_OF_DATASETS, vectorOptions);

		// build a style (marker) for the layer
		Style pointStyle = new Style();
		pointStyle.setExternalGraphic("images/red-marker.png");
		pointStyle.setGraphicSize(32, 37);
		pointStyle.setGraphicOffset(-16, -37); // anchor on bottom center
		pointStyle.setFillOpacity(1.0);

		for (int i = 0; i < locations.length; i++) {
			// add the location to the vector layer
			Point point = new Point(locations[i].getLon(),
					locations[i].getLat());
			point.transform(new Projection(EPSG_4326), new Projection(
					EPSG_900913));

			VectorFeature feature = new VectorFeature(point, pointStyle);
			Attributes attributes = new Attributes();
			attributes.setAttribute("title", locations[i].getTitle());
			attributes.setAttribute("uri", locations[i].getUri());
			feature.setAttributes(attributes);

			locationLayer.addFeature(feature);

			// change the map extent by the point
			mapExtent.extend(point);
			logger.log(
					Level.INFO,
					"[location] new extent: " + mapExtent.getLowerLeftX() + ","
							+ mapExtent.getLowerLeftY() + ","
							+ mapExtent.getUpperRightX() + ","
							+ mapExtent.getUpperRightY());
		}

		// add the vector (maker) layer to the map
		map.addLayer(locationLayer);

		// now we want a popup to appear when user clicks
		// First create a select control and make sure it is actived
		SelectFeature selectFeature = new SelectFeature(locationLayer);
		selectFeature.setAutoActivate(true);
		map.addControl(selectFeature);

		// add a VectorFeatureSelectedListener to the feature to show the popup
		locationLayer
				.addVectorFeatureSelectedListener(new VectorFeatureSelectedListener() {
					@Override
					public void onFeatureSelected(
							FeatureSelectedEvent eventObject) {
						logger.log(Level.INFO, "onFeatureSelected");

						VectorFeature feature = eventObject.getVectorFeature();

						// creating popup
						String title = feature.getAttributes()
								.getAttributeAsString("title");
						String uri = feature.getAttributes()
								.getAttributeAsString("uri");
						String content = "<b><a href='" + getMediciUrl()
								+ "/#dataset?id=" + uri + "' target='new'>"
								+ title + "</a></b>";

						// close button has a bug; so turn off "close" button
						Popup popup = new FramedCloud(feature.getFID(), feature
								.getCenterLonLat(), null, content, null, false);
						popup.setPanMapIfOutOfView(true);
						popup.setAutoSize(true);
						feature.setPopup(popup);

						// And attach the popup to the map
						map.addPopup(feature.getPopup());
					}
				});
		// add a VectorFeatureUnselectedListener which removes the popup.
		locationLayer
				.addVectorFeatureUnselectedListener(new VectorFeatureUnselectedListener() {
					public void onFeatureUnselected(
							FeatureUnselectedEvent eventObject) {
						logger.log(Level.INFO, "onFeatureUnselected");
						VectorFeature pointFeature = eventObject
								.getVectorFeature();
						map.removePopup(pointFeature.getPopup());
						pointFeature.resetPopup();
					}
				});
		map.zoomToExtent(mapExtent);
		mapWidget.getElement().getFirstChildElement().getStyle().setZIndex(0);
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

		String restUrl = getMediciUrl() + "/api/logout";
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
