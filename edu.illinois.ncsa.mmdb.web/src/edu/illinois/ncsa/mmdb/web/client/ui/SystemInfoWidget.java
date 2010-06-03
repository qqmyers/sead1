/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
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
    private final FlexTable     mainTable;
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

        mainTable = new FlexTable();
        mainTable.addStyleName("datasetRightColText");
        mainPanel.add(mainTable);

        refresh();
    }

    public void refresh() {
        GWT.log("Updating system overview panel", null);

        mainTable.removeAllRows();
        mainTable.setHTML(0, 0, "Loading ...");

        service.execute(new SystemInfo(), new AsyncCallback<SystemInfoResult>() {
            public void onFailure(Throwable caught) {
                GWT.log("Error getResourcesOfType", caught);
            }

            public void onSuccess(SystemInfoResult result) {
                int row = 0;
                mainTable.removeAllRows();
                for (String key : result.getKeys() ) {
                    mainTable.setHTML(row, 0, result.getLabel(key));
                    mainTable.setHTML(row, 1, result.getValue(key));
                    mainTable.getCellFormatter().setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_LEFT);
                    row++;
                }
            }
        });
    }
}
