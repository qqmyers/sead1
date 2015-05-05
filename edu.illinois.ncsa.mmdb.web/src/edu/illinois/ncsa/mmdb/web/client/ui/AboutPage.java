/*******************************************************************************
 * Copyright 2014 University of Michigan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

/**
 *
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.Map;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
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
        projInfo.setStyleName("span8");
        projInfo.addStyleName("fixed-height-col");
        projInfo.getElement().setId("projInfo");

        HTML projDesc = new HTML("<div class=\"well\"><h4>Project Description</h4><div id=\"projectDesc\">" +
                MMDB._projectDescription + "</div></div>");
        projInfo.add(projDesc);

        top.add(projInfo);
        FlowPanel rightFlowPanel = new FlowPanel();
        rightFlowPanel.setStyleName("span4");
        FlowPanel sysInfoPanel = new FlowPanel();

        sysInfoPanel.getElement().setId("sysinfo");
        sysInfoPanel.addStyleName("well");
        sysInfoPanel.add(new HTML("<h4>Basic Information</h4>"));
        for (java.util.Map.Entry<String, String> e : info.entrySet() ) {
            Label keyLabel = new Label(e.getKey());
            Label valueLabel = new Label(e.getValue());
            FlowPanel fp = new FlowPanel();
            fp.add(keyLabel);
            fp.add(valueLabel);
            sysInfoPanel.add(fp);
        }
        rightFlowPanel.add(sysInfoPanel);
        if (MMDB._projectUrl.length() != 0) {
            Button homeButton = new Button("Visit Project Homepage");
            homeButton.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    Window.open(MMDB._projectUrl, null, null);

                }

            });
            rightFlowPanel.add(homeButton);
        }
        top.add(rightFlowPanel);
        mainLayoutPanel.add(top);
    }
}