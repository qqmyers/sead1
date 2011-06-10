/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010, NCSA.  All rights reserved.
 *
 * Developed by:
 * Cyberenvironments and Technologies (CET)
 * http://cet.ncsa.illinois.edu/
 *
 * National Center for Supercomputing Applications (NCSA)
 * http://www.ncsa.illinois.edu/
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the names of CET, University of Illinois/NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *******************************************************************************/
package edu.illinois.ncsa.mmdb.web.client;

import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.ui.preview.PreviewPanel;

/**

 */
public class Embed implements EntryPoint {

    /**
     * Dispatch service. Should be the only service needed. All commands should
     * go through this endpoint. To learn more look up gwt-dispatch and the
     * command pattern.
     */
    public final DispatchAsync         dispatchAsync = new MyDispatchAsync();

    /** Event bus for propagating events in the interface **/
    public static final HandlerManager eventBus      = new HandlerManager(null);

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {

        final RootPanel rootPanel = RootPanel.get("mmdb-mainContainer");
        if (rootPanel != null) {

            Map<String, String> params = getParams();
            final String uri = params.get("id");

            if (uri != null) {

                GWT.log("ID: " + uri);
                final FlowPanel column = new FlowPanel();
                rootPanel.add(column);

                //Set Width + Height
                final int width = Window.getClientWidth();
                final int height = Window.getClientHeight();
                GWT.log("Width: " + width + " Height: " + height);

                final HorizontalPanel bottom = new HorizontalPanel();
                bottom.addStyleName("bottomRow");
                bottom.setWidth((width - 2) + "px");
                rootPanel.add(bottom);

                final Anchor link = new Anchor();
                link.setHref(GWT.getHostPageBaseURL() + "#dataset?id=" + uri);
                link.setTarget("_parent");

                //Logo
                final Image logo = new Image("images/logo_embedded.png");
                logo.setSize("60px", "24px");
                logo.addStyleName("bottomLogo");
                link.getElement().appendChild(logo.getElement());

                dispatchAsync.execute(new GetDataset(uri), new AsyncCallback<GetDatasetResult>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Error getting dataset", null);
                        rootPanel.add(new Label("Dataset Not Available"));
                    }

                    @Override
                    public void onSuccess(GetDatasetResult result) {
                        PreviewPanel previewPanel = new PreviewPanel(dispatchAsync, eventBus, true, width, height);
                        previewPanel.drawPreview(result, column, uri);
                        Label title = new Label(result.getDataset().getTitle());
                        title.setStyleName("fileTitle");
                        bottom.add(title);
                        bottom.add(link);
                    }
                });

            } else {
                rootPanel.add(new Label("No Dataset Specified."));
            }
        }
    }

    /**
     * Parse the parameters in the history token after the '?'
     * 
     * @return
     */
    public static Map<String, String> getParams() {
        Map<String, String> params = new HashMap<String, String>();
        String paramString = History.getToken().substring(
                History.getToken().indexOf("?") + 1);
        if (!paramString.isEmpty()) {
            for (String paramEntry : paramString.split("&") ) {
                String[] terms = paramEntry.split("=");
                if (terms.length == 2) {
                    params.put(terms[0], terms[1]);
                }
            }
        }
        return params;
    }
}
