/**
 *
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;

import edu.illinois.ncsa.mmdb.web.client.MMDB;

/**
 * @author myersjd@umich.edu
 *
 */
public class DiscoveryPage extends Page {

    String currentCollection = null;

    public DiscoveryPage(String collection, String title, DispatchAsync dispatchAsync, HandlerManager eventbus) {
        super(title, dispatchAsync, eventbus, true);
        currentCollection = collection;
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
        top.getElement().setId("discovery");
        top.setStyleName("row-fluid");

        FlowPanel left = new FlowPanel();
        left.setStyleName("span3");
        FlowPanel search = new FlowPanel();
        search.getElement().setId("search");
        search.setStyleName("well");
        if (currentCollection == null) {
            HTML searchHtml = new HTML("<h3>Search By</h3><div id=\"facetedSearch\" class=\"well\"></div>" +
                    "<div id=\"reset\" style=\"display:none;float:right;color:blue;margin-right:10px;margin-top:10px;\"><span>Reset Filters</span></div>" +
                    "<div id=\"legend\" class=\"well\" ><i>Viewing all collections.</i>");
            search.add(searchHtml);
        }
        left.add(search);
        HTML projInfo = new HTML("<div class=\"well\"><h4>Project Description<h4><div id=\"projectDesc\">" +
                MMDB._projectDescription + "</div></div>");
        left.add(projInfo);
        top.add(left);
        HTML bodyHtml = new HTML("<div id=\"xmlBody\"></div>");
        bodyHtml.setStyleName("span9");
        top.add(bodyHtml);
        String va_url = RootPanel.get("va").getElement().getAttribute("href");
        mainLayoutPanel.add(new HTML("This page contains collections of datasets that this project team has published through the " +
                "<a href=\"" + va_url + "\">SEAD Virtual Archive</a> (or manually marked as having a publication date)."));
        mainLayoutPanel.add(top);
        if (currentCollection == null) {
            loadCollections();
        } else {
            loadCollection(currentCollection);
        }
    }

    public static native void loadCollection(String collectionId)/*-{

		$wnd.loadPublishedCollection(collectionId);
    }-*/;

    public static native void loadCollections() /*-{

		$wnd.loadPublishedCollections();
    }-*/;

}
