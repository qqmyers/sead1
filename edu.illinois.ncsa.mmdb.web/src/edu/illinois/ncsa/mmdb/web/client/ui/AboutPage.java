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
/**
 *
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.Map;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SystemInfo;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SystemInfoResult;

/**
 * A page listing all tags in the system.
 *
 * @author myersjd@umich.edu
 *
 */
public class AboutPage extends Page {

    private Map<String, String> info = null;

    public AboutPage(DispatchAsync dispatchAsync, HandlerManager eventBus) {
        super("About", dispatchAsync, eventBus, true);

        dispatchAsync.execute(new SystemInfo(), new AsyncCallback<SystemInfoResult>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Failed to get sysinfo: " + caught.getMessage());
                info = null;
                layout();
            }

            @Override
            public void onSuccess(SystemInfoResult result) {
                info = result.getMap();

                layout();
            }
        });
    }

    @Override
    public void layout() {
        clear();
        mainLayoutPanel.addStyleName("sead-scope");
        mainLayoutPanel.addStyleName("container-fluid");
        FlowPanel top = new FlowPanel();
        top.getElement().setId("abouttop");
        top.setStyleName("row-fluid");

        FlowPanel projInfo = new FlowPanel();
        projInfo.setStyleName("span3");
        projInfo.addStyleName("fixed-height-col");
        projInfo.getElement().setId("projInfo");

        HTML projDesc = new HTML("<div class=\"well\"><div id=\"projectDesc\">" +
                MMDB._projectDescription + "</div></div>");
        projInfo.add(projDesc);

        top.add(projInfo);

        FlowPanel sysInfoPanel = new FlowPanel();
        sysInfoPanel.setStyleName("span9");
        sysInfoPanel.getElement().setId("sysinfo");
        sysInfoPanel.addStyleName("well");
        for (java.util.Map.Entry<String, String> e : info.entrySet() ) {
            FlowPanel fp = new FlowPanel();
            Label keyLabel = new Label(e.getKey());
            Label valueLabel = new Label(e.getValue());
            fp.add(keyLabel);
            fp.add(valueLabel);
            sysInfoPanel.add(fp);
        }
        top.add(sysInfoPanel);
        mainLayoutPanel.add(top);
    }
}