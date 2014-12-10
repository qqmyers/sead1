package edu.illinois.ncsa.mmdb.web.client.ui.admin;

import java.util.SortedSet;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.dispatch.AddMetadata;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AddMetadataResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.UserMetadataField;
import edu.illinois.ncsa.mmdb.web.client.ui.LabeledListBox;

public class ManageMetadataWidget extends Composite {
    private final DispatchAsync            dispatchAsync;
    VerticalPanel                          mainPanel;
    protected SortedSet<UserMetadataField> availableFields;
    protected LabeledListBox               examples;
    protected FlexTable                    table;

    public ManageMetadataWidget(DispatchAsync dispatchAsync) {
        this.dispatchAsync = dispatchAsync;

        mainPanel = new VerticalPanel();
        initWidget(mainPanel);

        createUI();
    }

    protected void createUI() {

        table = new FlexTable();
        mainPanel.add(table);

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

        Button submitButton = new Button("Add New Term", new ClickHandler() {
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
                    }
                });
            }
        });

        mainPanel.add(submitButton);
    }
}
