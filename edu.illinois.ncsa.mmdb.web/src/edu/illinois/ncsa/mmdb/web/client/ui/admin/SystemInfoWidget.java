/**
 *
 */
package edu.illinois.ncsa.mmdb.web.client.ui.admin;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.client.dispatch.SystemInfo;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SystemInfoResult;

/**
 * A simple overview of the number of objects in the system by type
 *
 * @author Luigi Marini
 * @author Rob Kooper
 *
 */
public class SystemInfoWidget extends Composite {
    private final DispatchAsync service;
    private final FlowPanel     mainPanel;

    public SystemInfoWidget(DispatchAsync service, boolean showTitle) {
        this.service = service;

        mainPanel = new FlowPanel();
        mainPanel.addStyleName("datasetRightColSection");
        initWidget(mainPanel);

        if (showTitle) {
            Label lbl = new Label("System Info");
            lbl.addStyleName("datasetRightColHeading");
            mainPanel.add(lbl);
        }

        FlowPanel sysInfoPanel = new FlowPanel();
        mainPanel.add(sysInfoPanel);
        refresh(sysInfoPanel);
    }

    public void refresh(final FlowPanel sysInfoPanel) {
        GWT.log("Updating system overview panel", null);
        sysInfoPanel.clear();

        service.execute(new SystemInfo(), new AsyncCallback<SystemInfoResult>() {
            public void onFailure(Throwable caught) {
                GWT.log("Error getResourcesOfType", caught);
            }

            public void onSuccess(SystemInfoResult result) {
                sysInfoPanel.getElement().setId("sysinfo");
                sysInfoPanel.addStyleName("well");
                for (java.util.Map.Entry<String, String> e : result.getMap().entrySet() ) {
                    Label keyLabel = new Label(e.getKey());
                    Label valueLabel = new Label(e.getValue());
                    FlowPanel fp = new FlowPanel();
                    fp.add(keyLabel);
                    fp.add(valueLabel);
                    sysInfoPanel.add(fp);
                }

            }
        });
    }
}
