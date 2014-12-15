package edu.illinois.ncsa.mmdb.web.client.ui.admin;

import java.util.SortedSet;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
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
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.dispatch.AddMetadata;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListUserMetadataFields;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListUserMetadataFieldsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MetadataTermResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.RemoveMetadata;
import edu.illinois.ncsa.mmdb.web.client.dispatch.UpdateMetadata;
import edu.illinois.ncsa.mmdb.web.client.dispatch.UserMetadataField;

public class ManageMetadataWidget extends Composite {
    private final DispatchAsync            dispatch;
    private final FlowPanel                mainFlowPanel;

    private final VerticalPanel            mainPanel;
    protected SortedSet<UserMetadataField> availableFields;

    public ManageMetadataWidget(DispatchAsync dispatchAsync) {
        this.dispatch = dispatchAsync;

        mainFlowPanel = new FlowPanel();
        mainFlowPanel.addStyleName("page");
        initWidget(mainFlowPanel);

        mainPanel = new VerticalPanel();
        mainPanel.addStyleName("userManagementMain");

        mainPanel.add(discloseAs(addNewTermPanel(), "Add new editable term", "Add Editable term", true));
        mainPanel.add(discloseAs(modifyExistingTermPanel(), "Modify label/description of Metadata", "Modify label/description", false));
        mainPanel.add(discloseAs(removeEditableTermPanel(), "Mark as viewable term", "Mark as viewable", false));

        mainFlowPanel.add(mainPanel);
    }

    /**
     * This function will have the ability to modify the label and description
     * of any existing
     * metadata term, editable or viewable
     **/
    protected Panel modifyExistingTermPanel() {
        final VerticalPanel existingPanel = new VerticalPanel();
        final ListBox uriList = new ListBox();
        final TextBox labelText = new TextBox();
        final TextBox descriptionText = new TextBox();
        final Button submitButton = new Button("Submit", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String label = labelText.getText();
                String description = descriptionText.getText();
                String predicate = uriList.getItemText(uriList.getSelectedIndex());

                UpdateMetadata m = new UpdateMetadata(predicate, label, description);
                dispatch.execute(m, new AsyncCallback<MetadataTermResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not add Metadata value.", caught);
                    }

                    @Override
                    public void onSuccess(MetadataTermResult result) {
                        dispatch.execute(new ListUserMetadataFields(false), new AsyncCallback<ListUserMetadataFieldsResult>() {
                            public void onFailure(Throwable caught) {
                                GWT.log("Error retrieving available list of User Specified Metadata fields", caught);
                            }

                            public void onSuccess(ListUserMetadataFieldsResult result) {
                                availableFields = result.getFieldsSortedByName();
                            }
                        });
                    }
                });
            }
        });

        uriList.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                String selectedText = uriList.getItemText(uriList.getSelectedIndex());
                for (UserMetadataField field : availableFields ) {
                    if (field.getUri().compareTo(selectedText) == 0) {
                        labelText.setText(field.getLabel());
                        descriptionText.setText(field.getDescription());
                    }
                }
            }
        });

        dispatch.execute(new ListUserMetadataFields(false), new AsyncCallback<ListUserMetadataFieldsResult>() {
            public void onFailure(Throwable caught) {
                GWT.log("Error retrieving available list of User Specified Metadata fields", caught);
            }

            public void onSuccess(ListUserMetadataFieldsResult result) {
                availableFields = result.getFieldsSortedByName();
                if (availableFields.size() > 0) {
                    for (UserMetadataField field : availableFields ) {
                        String uri = field.getUri();
                        uriList.addItem(uri);
                    }
                    existingPanel.add(uriList);
                    existingPanel.add(labelText);
                    existingPanel.add(descriptionText);
                    existingPanel.add(submitButton);
                }

            }
        });

        return existingPanel;
    }

    /**
     * This function will have the ability to reclassify editable metadata as
     * viewable, effectively removing
     * it from the list visible to the user.
     **/
    protected Panel removeEditableTermPanel() {
        final VerticalPanel existingPanel = new VerticalPanel();
        final ListBox uriList = new ListBox();
        final TextBox labelText = new TextBox();
        final TextBox descriptionText = new TextBox();
        final Button submitButton = new Button("Submit", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String label = labelText.getText();
                String description = descriptionText.getText();
                String predicate = uriList.getItemText(uriList.getSelectedIndex());

                RemoveMetadata m = new RemoveMetadata(predicate, label, description);
                dispatch.execute(m, new AsyncCallback<MetadataTermResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not add Metadata value.", caught);
                    }

                    @Override
                    public void onSuccess(MetadataTermResult result) {
                        dispatch.execute(new ListUserMetadataFields(true), new AsyncCallback<ListUserMetadataFieldsResult>() {
                            public void onFailure(Throwable caught) {
                                GWT.log("Error retrieving available list of User Specified Metadata fields", caught);
                            }

                            public void onSuccess(ListUserMetadataFieldsResult result) {
                                availableFields = result.getFieldsSortedByName();
                                uriList.clear();
                                labelText.setText("");
                                descriptionText.setText("");
                                if (availableFields.size() > 0) {
                                    for (UserMetadataField field : availableFields ) {
                                        String uri = field.getUri();
                                        uriList.addItem(uri);
                                    }
                                }

                            }
                        });
                    }
                });
            }
        });

        uriList.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                String selectedText = uriList.getItemText(uriList.getSelectedIndex());
                for (UserMetadataField field : availableFields ) {
                    if (field.getUri().compareTo(selectedText) == 0) {
                        labelText.setText(field.getLabel());
                        descriptionText.setText(field.getDescription());
                    }
                }
            }
        });

        dispatch.execute(new ListUserMetadataFields(true), new AsyncCallback<ListUserMetadataFieldsResult>() {
            public void onFailure(Throwable caught) {
                GWT.log("Error retrieving available list of User Specified Metadata fields", caught);
            }

            public void onSuccess(ListUserMetadataFieldsResult result) {
                availableFields = result.getFieldsSortedByName();
                if (availableFields.size() > 0) {
                    for (UserMetadataField field : availableFields ) {
                        String uri = field.getUri();
                        uriList.addItem(uri);
                    }
                    existingPanel.add(uriList);
                    existingPanel.add(labelText);
                    existingPanel.add(descriptionText);
                    existingPanel.add(submitButton);
                }

            }
        });

        return existingPanel;
    }

    protected Panel addNewTermPanel() {
        VerticalPanel newTermPanel = new VerticalPanel();
        final FlexTable table = new FlexTable();

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

                AddMetadata m = new AddMetadata(predicate, label, description);
                dispatch.execute(m, new AsyncCallback<MetadataTermResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not add Metadata value.", caught);
                    }

                    @Override
                    public void onSuccess(MetadataTermResult result) {
                        //on success adding a new term, kill the cached metadata list.
                        dispatch.execute(new ListUserMetadataFields(false), new AsyncCallback<ListUserMetadataFieldsResult>() {
                            public void onFailure(Throwable caught) {
                                GWT.log("Error retrieving available list of User Specified Metadata fields", caught);
                            }

                            public void onSuccess(ListUserMetadataFieldsResult result) {
                            }
                        });

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
