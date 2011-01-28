/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GeoSearch;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GeoSearchResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;

/**
 * @author lmarini
 * 
 */
public class MapPage extends Page {

    private static final String   TITLE = "Data";
    private final HandlerManager  eventbus;
    private final MyDispatchAsync dispatch;
    private MapWidget             map;
    private FlowPanel             hitsPanel;

    public MapPage(MyDispatchAsync dispatch, HandlerManager eventbus) {
        super(TITLE, dispatch);
        this.dispatch = dispatch;
        this.eventbus = eventbus;
        populate();
    }

    private void populate() {
        dispatch.execute(new GeoSearch(), new AsyncCallback<GeoSearchResult>() {

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error geosearching in map page", caught);
            }

            @Override
            public void onSuccess(GeoSearchResult result) {
                listResults(result);
            }
        });
    }

    private void listResults(GeoSearchResult result) {
        for (String hit : result.getHits() ) {
            hitsPanel.add(new Label(hit));
        }
    }

    /* (non-Javadoc)
     * @see edu.illinois.ncsa.mmdb.web.client.ui.Page#layout()
     */
    @Override
    public void layout() {
        map = new MapWidget();
        map.setSize("500px", "500px");
        mainLayoutPanel.add(map);
        hitsPanel = new FlowPanel();
        mainLayoutPanel.add(hitsPanel);
    }
}
