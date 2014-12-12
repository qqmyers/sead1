package edu.illinois.ncsa.mmdb.web.client.ui.admin;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.dispatch.AddMetadata;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AddMetadataResult;

public class ManageMetadataWidget extends Composite {
    private final DispatchAsync dispatchAsync;
    private final FlowPanel     mainFlowPanel;

    private final VerticalPanel mainPanel;
    protected FlexTable         table;

    public ManageMetadataWidget(DispatchAsync dispatchAsync) {
        this.dispatchAsync = dispatchAsync;

        mainFlowPanel = new FlowPanel();
        mainFlowPanel.addStyleName("page");
        initWidget(mainFlowPanel);

        mainPanel = new VerticalPanel();
        mainPanel.addStyleName("userManagementMain");

        mainPanel.add(discloseAs(addNewTermPanel(), "Add new term", "Add term", false));
        mainPanel.add(discloseAs(modifyExistingTermPanel(), "Modify existing Metadata", "Modify term", false));
        mainPanel.add(discloseAs(removeEditableTermPanel(), "Remove editable term", "Remove term", false));

        mainFlowPanel.add(mainPanel);
    }

    /**
     * This function will have the ability to modify the label and description
     * of any existing
     * metadata term, editable or viewable
     **/
    protected Panel modifyExistingTermPanel() {
        VerticalPanel existingPanel = new VerticalPanel();
        return existingPanel;
    }

    /**
     * This function will have the ability to reclassify editable metadata as
     * viewable, effectively removing
     * it from the list visible to the user.
     **/
    protected Panel removeEditableTermPanel() {
        VerticalPanel editableMetadataPanel = new VerticalPanel();
        return editableMetadataPanel;
    }

    protected Panel addNewTermPanel() {
        VerticalPanel newTermPanel = new VerticalPanel();
        table = new FlexTable();

        int idx = 0;

        TextBox textbox = new TextBox();
        textbox.setVisibleLength(40);
        table.setText(idx, 0, "Label");
        table.setWidget(idx, 1, textbox);
        idx++;

        textbox = new TextBox();
        textbox.setVisibleLength(80);
        table.setText(idx, 0, "URI");
        table.setWidget(idx, 1, textbox);
        idx++;

        textbox = new TextBox();
        textbox.setVisibleLength(80);
        table.setText(idx, 0, "Description");
        table.setWidget(idx, 1, textbox);
        idx++;

        Button submitButton = new Button("Submit", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String label = ((TextBox) table.getWidget(0, 1)).getText();
                String predicate = ((TextBox) table.getWidget(1, 1)).getText();
                String description = ((TextBox) table.getWidget(2, 1)).getText();
                GWT.log("dummy message" + label + predicate + description);
                AddMetadata m = new AddMetadata(predicate, label, description);
                dispatchAsync.execute(m, new AsyncCallback<AddMetadataResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not add Metadata value.", caught);
                    }

                    @Override
                    public void onSuccess(AddMetadataResult result) {
                        ((TextBox) table.getWidget(0, 1)).setText("");
                        ((TextBox) table.getWidget(1, 1)).setText("");
                        ((TextBox) table.getWidget(2, 1)).setText("");
                    }
                });
            }
        });

        newTermPanel.add(table);
        newTermPanel.add(submitButton);

        return newTermPanel;
    }

    /**
     * 
     * Code to deal with panel minimize and open.Copied from
     * UserManagementWidget
     */
    DisclosurePanel discloseAs(Widget w, final String openTitle, final String closedTitle, boolean open) {
        final DisclosurePanel dp = new DisclosurePanel(open ? openTitle : closedTitle);
        dp.setAnimationEnabled(true);
        dp.setOpen(open);

        dp.add(w);
        dp.addOpenHandler(new OpenHandler<DisclosurePanel>() {
            @Override
            public void onOpen(OpenEvent<DisclosurePanel> event) {
                dp.getHeaderTextAccessor().setText(openTitle);
            }
        });
        dp.addCloseHandler(new CloseHandler<DisclosurePanel>() {
            @Override
            public void onClose(CloseEvent<DisclosurePanel> event) {
                dp.getHeaderTextAccessor().setText(closedTitle);
            }
        });
        return dp;
    }

}
