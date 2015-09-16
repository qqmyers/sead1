package edu.illinois.ncsa.mmdb.web.client.ui.admin;

import java.util.SortedSet;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

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
    protected SortedSet<UserMetadataField> editableFields; //editable
    protected SortedSet<UserMetadataField> availableFields; //editable + viewable

    public ManageMetadataWidget(DispatchAsync dispatchAsync) {
        this.dispatch = dispatchAsync;

        mainFlowPanel = new FlowPanel();
        mainFlowPanel.addStyleName("page");
        mainFlowPanel.getElement().setClassName("metadatapanel");
        initWidget(mainFlowPanel);

        mainPanel = new VerticalPanel();

        mainPanel.add(addNewTermPanel());
        mainPanel.add(modifyExistingTermPanel());
        mainPanel.add(removeEditableTermPanel());

        mainFlowPanel.add(mainPanel);
    }

    /**
     * This function will have the ability to modify the label and description
     * of any existing
     * metadata term, editable or viewable
     **/
    protected DisclosurePanel modifyExistingTermPanel() {
        DisclosurePanel dp = new DisclosurePanel("Modify Label/Description of Metadata");
        dp.addStyleName("datasetDisclosurePanel");
        dp.setOpen(false);

        final VerticalPanel existingPanel = new VerticalPanel();
        dp.add(existingPanel);
        final FlexTable table = new FlexTable();
        int idx = 0;

        ListBox listbox = new ListBox();
        table.setText(idx, 0, "URI");
        table.setWidget(idx, 1, listbox);
        idx++;

        final TextBox labeltextbox = new TextBox();
        labeltextbox.setVisibleLength(40);
        table.setText(idx, 0, "Label");
        table.setWidget(idx, 1, labeltextbox);
        labeltextbox.addKeyPressHandler(new KeyPressHandler() {
            public void onKeyPress(KeyPressEvent event) {
                //Don't allow '.' chars which are bad for json/bson/mongodb
                if (".".contains(String.valueOf(event.getCharCode()))) {
                    labeltextbox.cancelKey();

                }
            }
        });
        idx++;

        TextBox textbox = new TextBox();
        textbox.setVisibleLength(80);
        table.setText(idx, 0, "Description");
        table.setWidget(idx, 1, textbox);
        idx++;

        final ListBox uriList = ((ListBox) table.getWidget(0, 1));
        final TextBox labelText = ((TextBox) table.getWidget(1, 1));
        final TextBox descriptionText = ((TextBox) table.getWidget(2, 1));

        final Button submitButton = new Button("Modify", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String predicate = uriList.getItemText(uriList.getSelectedIndex());
                UpdateMetadata m = new UpdateMetadata(predicate, labelText.getText(), descriptionText.getText());

                dispatch.execute(m, new AsyncCallback<MetadataTermResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not update a Metadata value.", caught);
                        Window.alert(caught.getLocalizedMessage());
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
                    existingPanel.add(table);
                    existingPanel.add(submitButton);
                    //Pre-fill label and description for the current/first item
                    uriList.setItemSelected(0, true);
                    DomEvent.fireNativeEvent(Document.get().createChangeEvent(), uriList);
                }

            }
        });

        return dp;
    }

    /**
     * This function will have the ability to reclassify editable metadata as
     * viewable, effectively removing
     * it from the list visible to the user.
     **/
    protected DisclosurePanel removeEditableTermPanel() {
        DisclosurePanel dp = new DisclosurePanel("Remove Metadata term");
        dp.addStyleName("datasetDisclosurePanel");
        dp.setOpen(false);

        final VerticalPanel existingPanel = new VerticalPanel();
        dp.add(existingPanel);

        final FlexTable table = new FlexTable();
        int idx = 0;

        ListBox listbox = new ListBox();
        table.setText(idx, 0, "URI");
        table.setWidget(idx, 1, listbox);
        idx++;

        TextBox textbox = new TextBox();
        textbox.setVisibleLength(40);
        textbox.setReadOnly(true);
        table.setText(idx, 0, "Label");
        table.setWidget(idx, 1, textbox);
        idx++;

        textbox = new TextBox();
        textbox.setVisibleLength(80);
        table.setText(idx, 0, "Description");
        textbox.setReadOnly(true);
        table.setWidget(idx, 1, textbox);
        idx++;

        final ListBox uriList = ((ListBox) table.getWidget(0, 1));
        final TextBox labelText = ((TextBox) table.getWidget(1, 1));
        final TextBox descriptionText = ((TextBox) table.getWidget(2, 1));
        final Button submitButton = new Button("Remove", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String predicate = uriList.getItemText(uriList.getSelectedIndex());
                RemoveMetadata m = new RemoveMetadata(predicate, labelText.getText(), descriptionText.getText());
                dispatch.execute(m, new AsyncCallback<MetadataTermResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not remove Metadata value.", caught);
                    }

                    @Override
                    public void onSuccess(MetadataTermResult result) {
                        dispatch.execute(new ListUserMetadataFields(true), new AsyncCallback<ListUserMetadataFieldsResult>() {
                            public void onFailure(Throwable caught) {
                                GWT.log("Error retrieving available list of User Specified Metadata fields", caught);
                            }

                            public void onSuccess(ListUserMetadataFieldsResult result) {
                                editableFields = result.getFieldsSortedByName();
                                uriList.clear();
                                labelText.setText("");
                                descriptionText.setText("");
                                if (editableFields.size() > 0) {
                                    for (UserMetadataField field : editableFields ) {
                                        String uri = field.getUri();
                                        uriList.addItem(uri);
                                    }
                                    //Pre-fill label and description for the current/first item
                                    uriList.setItemSelected(0, true);
                                    DomEvent.fireNativeEvent(Document.get().createChangeEvent(), uriList);
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
                for (UserMetadataField field : editableFields ) {
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
                editableFields = result.getFieldsSortedByName();
                if (editableFields.size() > 0) {
                    for (UserMetadataField field : editableFields ) {
                        String uri = field.getUri();
                        uriList.addItem(uri);
                    }
                    existingPanel.add(table);
                    existingPanel.add(submitButton);
                    //Pre-fill label and description for the current/first item
                    uriList.setItemSelected(0, true);
                    DomEvent.fireNativeEvent(Document.get().createChangeEvent(), uriList);
                }

            }
        });

        return dp;
    }

    protected DisclosurePanel addNewTermPanel() {
        DisclosurePanel dp = new DisclosurePanel("Add new Metadata term");
        dp.addStyleName("datasetDisclosurePanel");
        dp.setOpen(true);

        VerticalPanel newTermPanel = new VerticalPanel();
        dp.add(newTermPanel);
        final FlexTable table = new FlexTable();

        int idx = 0;

        final TextBox labeltextbox = new TextBox();
        labeltextbox.setVisibleLength(40);
        labeltextbox.addKeyPressHandler(new KeyPressHandler() {
            public void onKeyPress(KeyPressEvent event) {
                //Don't allow '.' chars which are bad for json/bson/mongodb
                if (".".contains(String.valueOf(event.getCharCode()))) {
                    labeltextbox.cancelKey();

                }
            }
        });
        table.setText(idx, 0, "Label");
        table.setWidget(idx, 1, labeltextbox);
        idx++;

        TextBox textbox = new TextBox();
        textbox.setVisibleLength(80);
        table.setText(idx, 0, "URI");
        table.setWidget(idx, 1, textbox);
        idx++;

        textbox = new TextBox();
        textbox.setVisibleLength(80);
        table.setText(idx, 0, "Description");
        table.setWidget(idx, 1, textbox);
        idx++;

        Button submitButton = new Button("Add", new ClickHandler() {
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
                        Window.alert(caught.getLocalizedMessage());
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

        return dp;
    }

}
