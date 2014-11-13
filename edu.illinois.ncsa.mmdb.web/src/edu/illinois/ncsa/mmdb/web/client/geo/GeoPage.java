package edu.illinois.ncsa.mmdb.web.client.geo;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
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
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.geo.event.LayerOpacityChangeEvent;
import edu.illinois.ncsa.mmdb.web.client.geo.event.LayerOpacityChangeHandler;
import edu.illinois.ncsa.mmdb.web.client.geo.event.LayerVisibilityChangeEvent;
import edu.illinois.ncsa.mmdb.web.client.geo.event.LayerVisibilityChangeHandler;
import edu.illinois.ncsa.mmdb.web.client.geo.service.MediciProxyService;
import edu.illinois.ncsa.mmdb.web.client.geo.service.MediciProxyServiceAsync;
import edu.illinois.ncsa.mmdb.web.client.ui.Page;
import edu.illinois.ncsa.mmdb.web.common.geo.LayerInfo;
import edu.illinois.ncsa.mmdb.web.common.geo.LocationInfo;

/**
 *
 * @author Jong Lee <jonglee1@illinois.edu>
 * @author Jim Myers <myersjd@umich.edu>
 *
 */

public class GeoPage extends Page {
    Logger                                logger               = Logger.getLogger(this.getClass().getName());
    private static final String           LOCATION_OF_DATASETS = "Location of Datasets";
    private static String                 wmsUrl               = null;
    private static String                 mediciUrl            = null;

    private final MediciProxyServiceAsync mediciProxySvc       = (MediciProxyServiceAsync) GWT
                                                                       .create(MediciProxyService.class);
    private SuggestBox                    tagTextBox;

    private String                        tag                  = null;

    protected FlowPanel                   layerSwitcher;

    private VerticalPanel                 tagPanel             = null;
    private GeoWidget                     theMap               = null;

    public GeoPage(String tag, DispatchAsync dispatchAsync, HandlerManager eventBus) {
        super("GeoBrowser", dispatchAsync, eventBus, true);
        mediciUrl = GWT.getHostPageBaseURL();
        wmsUrl = GWT.getHostPageBaseURL() + "geoproxy/wms";
        this.tag = tag;
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

        layout();
    }

    private void buildTagPanel(final FlowPanel dp) {
        if (tagPanel == null) {
            mediciProxySvc.getTags(new AsyncCallback<String[]>() {
                public void onSuccess(String[] result) {

                    MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
                    if (result != null) {
                        for (String s : result ) {
                            oracle.add(s);
                        }
                    }
                    tagPanel = createTagPanel(oracle);
                    dp.add(tagPanel);
                }

                public void onFailure(Throwable caught) {
                    fail(caught);
                }
            });
        } else {
            dp.add(tagPanel);
        }

    }

    /**
     * Rebuild the map with give tag
     *
     * @param tag
     */
    private void buildMapUi(String tag) {

        String encodedTag = null;
        if (tag != null) {

            encodedTag = URL.encode(tag);
        }
        int x = 958;
        theMap = new GeoWidget(mediciProxySvc, "map", x, 400);
        theMap.buildMapUi(encodedTag, new Callback<EmptyResult, Throwable>() {

            @Override
            public void onSuccess(EmptyResult result) {
                layerSwitcher = createLayerSwitcher(theMap.getLayers(),
                        theMap.getLocations());
                RootPanel.get("layers").add(layerSwitcher);
            }

            @Override
            public void onFailure(Throwable caught) {
                fail(caught);
            }
        });
    }

    private void cleanPage() {

        logger.log(Level.INFO, "Cleaning up for refresh");
        if (theMap != null) {
            theMap.cleanMap();
            theMap = null;
        }
        clear();
    }

    /**
     * Create tag panel with the word suggestion
     *
     * @param oracle
     * @return
     */
    protected VerticalPanel createTagPanel(MultiWordSuggestOracle oracle) {

        VerticalPanel vp = new VerticalPanel();
        vp.setSpacing(10);

        vp.add(new HTML("<h3>Filter by Tag</h3>"));
        vp.add(new HTML("<hr>"));

        HorizontalPanel hp = new HorizontalPanel();

        tagTextBox = new SuggestBox(oracle);

        if (this.tag != null) {
            tagTextBox.setText(this.tag);
        }

        Button bt = new Button("Filter");
        bt.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                //FixMe - update history but don't fire event (which causes a new geopage to be created) and just handle the update needed within page?
                History.newItem("geo_tag_" + tagTextBox.getText());
            }
        });
        hp.add(tagTextBox);
        hp.add(bt);

        vp.add(hp);

        return vp;
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

                for (int i = layers.length - 1; i >= 0; i-- ) {
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

        String htmlString = "<a href='" + getMediciUrl() + "#dataset?id="
                + uri + "'>" + title + "</a>";
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

        String htmlString = "GeoTagged_Datasets";
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

    public void updateVisibility(String name, boolean v) {
        theMap.updateVisibility(name, v);
    }

    public void updateOpacity(String name, float opacity) {
        theMap.updateOpacity(name, opacity);
    }

    private void fail(Throwable caught) {
        Window.alert("Could not retrieve data from repository - try later or contact your administrator.:" + caught.getMessage());
        GWT.log("Service call failure");
    }

    public static String getMediciUrl() {
        return mediciUrl;
    }

    public VerticalPanel buildGui() {
        cleanPage();

        HorizontalPanel hp = new HorizontalPanel();
        FlowPanel dp = new FlowPanel();
        buildTagPanel(dp);
        hp.add(dp);
        SimplePanel map = new SimplePanel();
        map.setStyleName("smallmap");
        map.getElement().setId("map");
        hp.add(map);
        VerticalPanel vpPanel = new VerticalPanel();
        vpPanel.add(hp);
        SimplePanel layers = new SimplePanel();
        layers.getElement().setId("layers");
        vpPanel.add(layers);
        return vpPanel;
    }

    @Override
    public void layout() {
        mainLayoutPanel.add(buildGui());
        buildMapUi(tag);
    }

}
