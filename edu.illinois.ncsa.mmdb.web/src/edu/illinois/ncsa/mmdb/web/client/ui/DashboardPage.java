package edu.illinois.ncsa.mmdb.web.client.ui;

import net.customware.gwt.dispatch.client.DispatchAsync;

import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.MapWidget;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.geo.GeoWidget;
import edu.illinois.ncsa.mmdb.web.client.geo.service.MediciProxyService;
import edu.illinois.ncsa.mmdb.web.client.geo.service.MediciProxyServiceAsync;

/**
 * @author myersjd@umich.edu
 *
 */
public class DashboardPage extends Page {

    String                                currentCollection = null;
    private MapWidget                     mapWidget;
    private Map                           map;
    private final MediciProxyServiceAsync mediciProxySvc    = (MediciProxyServiceAsync) GWT
                                                                    .create(MediciProxyService.class);
    private GeoWidget                     theMap;

    public DashboardPage(String title, DispatchAsync dispatchAsync, HandlerManager eventbus) {
        super(title, dispatchAsync, eventbus, true);
        layout();
    }

    /* (non-Javadoc)
     * @see edu.illinois.ncsa.mmdb.web.client.ui.Page#layout()
     */
    @Override
    public void layout() {
        clear();
        mainLayoutPanel.addStyleName("sead-scope");
        mainLayoutPanel.addStyleName("container-fluid");
        FlowPanel top = new FlowPanel();
        top.getElement().setId("dashboardtop");
        top.setStyleName("row-fluid");

        FlowPanel projInfo = new FlowPanel();
        projInfo.setStyleName("span3");
        projInfo.addStyleName("fixed-height-col");
        projInfo.getElement().setId("projInfo");

        HTML projDesc = new HTML("<div class=\"well\"><div id=\"projectDesc\">" +
                MMDB._projectDescription + "</div></div>");
        projInfo.add(projDesc);

        FlowPanel mapPanel = new FlowPanel();
        mapPanel.setStyleName("span6");
        mapPanel.addStyleName("fixed-height-col");
        mapPanel.getElement().setId("mapwrapper");

        FlowPanel summaryMap = new FlowPanel();
        summaryMap.setStyleName("smallmap");
        summaryMap.getElement().setId("map");
        mapPanel.add(summaryMap);
        FlowPanel mapMsg = new FlowPanel();
        mapMsg.getElement().setId("mapMsg");
        mapPanel.add(mapMsg);

        FlowPanel team = new FlowPanel();
        team.setStyleName("span3");
        team.addStyleName("fixed-height-col");
        team.addStyleName("well");
        team.getElement().setId("teammembers");

        top.add(projInfo);
        top.add(mapPanel);
        top.add(team);

        FlowPanel bottom = new FlowPanel();
        bottom.getElement().setId("dashboardbottom");
        bottom.setStyleName("row-fluid");

        FlowPanel dataDist = new FlowPanel();
        dataDist.setStyleName("span3");
        dataDist.getElement().setId("datadistribution");

        FlowPanel dataTable = new FlowPanel();
        dataTable.setStyleName("span6");
        dataTable.addStyleName("fixed-height-col");
        dataTable.getElement().setId("datatable");

        FlowPanel recentData = new FlowPanel();
        recentData.setStyleName("span3");
        recentData.addStyleName("fixed-height-col");
        recentData.getElement().setId("recentuploads");

        bottom.add(dataDist);
        bottom.add(dataTable);
        bottom.add(recentData);

        mainLayoutPanel.add(top);
        mainLayoutPanel.add(bottom);

        buildMapUi();

        Scheduler.get().scheduleDeferred(new Command() {
            public void execute() {
                loadDashboard();
            }
        });
    }

    public static native void loadDashboard() /*-{

		$wnd.loadDashboard();
    }-*/;

    /**
     * Rebuild the map with give tag
     *
     * @param tag
     */
    private void buildMapUi() {
        theMap = new GeoWidget(mediciProxySvc, "map", 564, 300);
        theMap.buildMapUi(null, new Callback<EmptyResult, Throwable>() {

            @Override
            public void onSuccess(EmptyResult result) {
            }

            @Override
            public void onFailure(Throwable caught) {
                fail(caught);
            }
        });
    }

    private void fail(Throwable caught) {
        RootPanel.get("mapMsg").add(new HTML("Could not retrieve geo data"));
        GWT.log("Service call failure");
    }
}
