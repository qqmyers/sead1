package edu.illinois.ncsa.mmdb.web.client.ui.admin;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ConfigurationResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ContextConvert;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ExtractionService;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ExtractionServiceResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetConfiguration;
import edu.illinois.ncsa.mmdb.web.client.dispatch.HasPermissionResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ReindexLucene;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ReindexLuceneResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetConfiguration;
import edu.illinois.ncsa.mmdb.web.client.ui.ConfirmDialog;
import edu.illinois.ncsa.mmdb.web.client.ui.LabeledListBox;
import edu.illinois.ncsa.mmdb.web.client.view.DynamicTableView;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.common.Permission;

public class ConfigurationWidget extends Composite {
    private final DispatchAsync       dispatchAsync;
    private final HasPermissionResult permissions;
    private final VerticalPanel       mainPanel;
    private boolean                   deleteOld;
    private SimplePanel               feedbackPanel;

    public ConfigurationWidget(final DispatchAsync dispatchAsync, HasPermissionResult permissions) {
        this.dispatchAsync = dispatchAsync;
        this.permissions = permissions;

        mainPanel = new VerticalPanel();
        initWidget(mainPanel);

        // get all values
        dispatchAsync.execute(new GetConfiguration(MMDB.getUsername()), new AsyncCallback<ConfigurationResult>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Could not get configuration values.", caught);
            }

            @Override
            public void onSuccess(ConfigurationResult result) {
                createUI(result);
            }
        });
    }

    private void createUI(ConfigurationResult configuration) {
        // mail configuration
        mainPanel.add(createMailSection(configuration));

        // mail configuration
        mainPanel.add(createProjectSection(configuration));

        //presentation defaults
        mainPanel.add(createPresentationSection(configuration));

        // google map key
        mainPanel.add(createMapSection(configuration));

        // server updates.
        mainPanel.add(createUpdateSection());

        // vivo configuration.
        mainPanel.add(createVIVOConfigurationSection(configuration));

        // va configuration.
        mainPanel.add(createVAConfigurationSection(configuration));

        //Pointer to ACR discovery app
        mainPanel.add(createDiscoveryConfigurationSection(configuration));

        // extractor configuration
        mainPanel.add(createExtractorSection(configuration));

    }

    private DisclosurePanel createMailSection(ConfigurationResult configuration) {
        //FixMe - use createSimpleConfigurationSection with arrays
        DisclosurePanel dp = new DisclosurePanel("Mail");
        dp.addStyleName("datasetDisclosurePanel");
        dp.setOpen(true);
        VerticalPanel vp = new VerticalPanel();
        vp.setWidth("100%");
        dp.add(vp);

        FlexTable table = new FlexTable();
        vp.add(table);

        final Map<ConfigurationKey, TextBox> inputs = new HashMap<ConfigurationKey, TextBox>();
        int idx = 0;

        TextBox textbox = new TextBox();
        textbox.setVisibleLength(40);
        textbox.setText(configuration.getConfiguration(ConfigurationKey.MailServer));
        table.setText(idx, 0, "SMTP Server");
        table.setWidget(idx, 1, textbox);
        inputs.put(ConfigurationKey.MailServer, textbox);
        idx++;

        textbox = new TextBox();
        textbox.setVisibleLength(40);
        textbox.setText(configuration.getConfiguration(ConfigurationKey.MailFrom));
        table.setText(idx, 0, "From address");
        table.setWidget(idx, 1, textbox);
        inputs.put(ConfigurationKey.MailFrom, textbox);
        idx++;

        textbox = new TextBox();
        textbox.setVisibleLength(40);
        textbox.setText(configuration.getConfiguration(ConfigurationKey.MailFullName));
        table.setText(idx, 0, "From fullname");
        table.setWidget(idx, 1, textbox);
        inputs.put(ConfigurationKey.MailFullName, textbox);
        idx++;

        textbox = new TextBox();
        textbox.setVisibleLength(40);
        textbox.setText(configuration.getConfiguration(ConfigurationKey.MailSubject));
        table.setText(idx, 0, "Subject prefix");
        table.setWidget(idx, 1, textbox);
        inputs.put(ConfigurationKey.MailSubject, textbox);
        idx++;

        textbox = new TextBox();
        textbox.setVisibleLength(40);
        textbox.setText(configuration.getConfiguration(ConfigurationKey.MediciName));
        table.setText(idx, 0, "Medici server");
        table.setWidget(idx, 1, textbox);
        inputs.put(ConfigurationKey.MediciName, textbox);
        idx++;

        // buttons
        HorizontalPanel hp = new HorizontalPanel();
        vp.add(hp);

        Button button = new Button("Submit", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                SetConfiguration query = new SetConfiguration(MMDB.getUsername());
                for (Entry<ConfigurationKey, TextBox> entry : inputs.entrySet() ) {
                    query.setConfiguration(entry.getKey(), entry.getValue().getText());
                }
                dispatchAsync.execute(query, new AsyncCallback<ConfigurationResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not get configuration values.", caught);
                    }

                    @Override
                    public void onSuccess(ConfigurationResult result) {
                        for (Entry<ConfigurationKey, TextBox> entry : inputs.entrySet() ) {
                            entry.getValue().setText(result.getConfiguration(entry.getKey()));
                        }
                    }
                });
            }
        });
        hp.add(button);

        button = new Button("Reset", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dispatchAsync.execute(new GetConfiguration(MMDB.getUsername()), new AsyncCallback<ConfigurationResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not get configuration values.", caught);
                    }

                    @Override
                    public void onSuccess(ConfigurationResult result) {
                        for (Entry<ConfigurationKey, TextBox> entry : inputs.entrySet() ) {
                            entry.getValue().setText(result.getConfiguration(entry.getKey()));
                        }
                    }
                });
            }
        });
        button.addStyleName("multiAnchor");
        hp.add(button);

        return dp;
    }

    private DisclosurePanel createProjectSection(ConfigurationResult configuration) {
        DisclosurePanel dp = new DisclosurePanel("Project");
        dp.addStyleName("datasetDisclosurePanel");
        dp.setOpen(true);
        VerticalPanel vp = new VerticalPanel();
        vp.setWidth("100%");
        dp.add(vp);

        FlexTable table = new FlexTable();
        vp.add(table);

        int idx = 0;

        final TextBox name = new TextBox();
        name.setVisibleLength(40);
        name.setText(configuration.getConfiguration(ConfigurationKey.ProjectName));
        table.setText(idx, 0, "Name");
        table.setWidget(idx, 1, name);
        idx++;

        final TextBox url = new TextBox();
        url.setVisibleLength(40);
        url.setText(configuration.getConfiguration(ConfigurationKey.ProjectURL));
        table.setText(idx, 0, "URL");
        table.setWidget(idx, 1, url);
        idx++;

        final TextArea desc = new TextArea();
        desc.setVisibleLines(5);
        desc.setText(configuration.getConfiguration(ConfigurationKey.ProjectDescription));
        table.setText(idx, 0, "Description");
        table.setWidget(idx, 1, desc);
        idx++;
        //Sort order
        final LabeledListBox sortOptions = new LabeledListBox("");
        sortOptions.addStyleName("pagingLabel");

        for (Map.Entry<String, String> entry : DynamicTableView.SORTCHOICES.entrySet() ) {
            sortOptions.addItem(entry.getValue(), entry.getKey());
        }

        sortOptions.setSelected(configuration.getConfiguration(ConfigurationKey.ProjectSortOrder));
        table.setText(idx, 0, "Default Sort Order");
        table.setWidget(idx, 1, sortOptions);
        idx++;

        //page view type
        final LabeledListBox pageViewType = new LabeledListBox("");
        pageViewType.addStyleName("pagingLabel");
        for (Map.Entry<String, String> entry : DynamicTableView.PAGE_VIEW_TYPES.entrySet() ) {
            pageViewType.addItem(entry.getValue(), entry.getKey());
        }
        String selected = configuration.getConfiguration(ConfigurationKey.ProjectPageViewType);
        if (selected == null || selected.isEmpty()) {
            selected = DynamicTableView.GRID_VIEW_TYPE;
        }
        pageViewType.setSelected(selected);
        table.setText(idx, 0, "Default Page View Type");
        table.setWidget(idx, 1, pageViewType);
        idx++;

        // buttons
        HorizontalPanel hp = new HorizontalPanel();
        vp.add(hp);

        Button button = new Button("Submit", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                SetConfiguration query = new SetConfiguration(MMDB.getUsername());
                query.setConfiguration(ConfigurationKey.ProjectName, name.getText());
                query.setConfiguration(ConfigurationKey.ProjectURL, url.getText());
                String cleanDesc = desc.getText().replace("'", "&#39;").replace("\"", "&#34;");
                query.setConfiguration(ConfigurationKey.ProjectDescription, cleanDesc);
                dispatchAsync.execute(query, new AsyncCallback<ConfigurationResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not get configuration values.", caught);
                    }

                    @Override
                    public void onSuccess(ConfigurationResult result) {
                        name.setText(result.getConfiguration(ConfigurationKey.ProjectName));
                        url.setText(result.getConfiguration(ConfigurationKey.ProjectURL));
                        desc.setText(result.getConfiguration(ConfigurationKey.ProjectDescription).replace("&#34;", "\"").replace("&#39;", "'"));
                    }
                });
            }
        });
        hp.add(button);
        //only resets changes before "submit" button is clicked. i.e. does not have memory of previously submitted changes.
        button = new Button("Reset", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dispatchAsync.execute(new GetConfiguration(MMDB.getUsername()), new AsyncCallback<ConfigurationResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not get configuration values.", caught);
                    }

                    @Override
                    public void onSuccess(ConfigurationResult result) {
                        name.setText(result.getConfiguration(ConfigurationKey.ProjectName));
                        url.setText(result.getConfiguration(ConfigurationKey.ProjectURL));
                        desc.setText(result.getConfiguration(ConfigurationKey.ProjectDescription));
                        sortOptions.setSelected(result.getConfiguration(ConfigurationKey.ProjectSortOrder));
                        pageViewType.setSelected(result.getConfiguration(ConfigurationKey.ProjectPageViewType));
                    }
                });
            }
        });
        button.addStyleName("multiAnchor");
        hp.add(button);

        return dp;
    }

    private DisclosurePanel createPresentationSection(ConfigurationResult configuration) {
        DisclosurePanel dp = new DisclosurePanel("Presentation Defaults");
        dp.addStyleName("datasetDisclosurePanel");
        dp.setOpen(false);
        VerticalPanel vp = new VerticalPanel();
        vp.setWidth("100%");
        dp.add(vp);

        FlexTable table = new FlexTable();
        vp.add(table);

        int idx = 0;

        //Sort order
        final LabeledListBox sortOptions = new LabeledListBox("");
        sortOptions.addStyleName("pagingLabel");

        for (Map.Entry<String, String> entry : DynamicTableView.SORTCHOICES.entrySet() ) {
            sortOptions.addItem(entry.getValue(), entry.getKey());
        }

        sortOptions.setSelected(configuration.getConfiguration(ConfigurationKey.PresentationSortOrder));
        table.setText(idx, 0, "Default Sort Order");
        table.setWidget(idx, 1, sortOptions);
        idx++;

        //page view type
        final LabeledListBox pageViewType = new LabeledListBox("");
        pageViewType.addStyleName("pagingLabel");
        for (Map.Entry<String, String> entry : DynamicTableView.PAGE_VIEW_TYPES.entrySet() ) {
            pageViewType.addItem(entry.getValue(), entry.getKey());
        }
        String selected = configuration.getConfiguration(ConfigurationKey.PresentationPageViewType);
        if (selected == null || selected.isEmpty()) {
            selected = DynamicTableView.GRID_VIEW_TYPE;
        }
        pageViewType.setSelected(selected);
        table.setText(idx, 0, "Default Page View Type");
        table.setWidget(idx, 1, pageViewType);
        idx++;

        // buttons
        HorizontalPanel hp = new HorizontalPanel();
        vp.add(hp);

        Button button = new Button("Submit", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                SetConfiguration query = new SetConfiguration(MMDB.getUsername());
                query.setConfiguration(ConfigurationKey.PresentationSortOrder, sortOptions.getSelected());
                query.setConfiguration(ConfigurationKey.PresentationPageViewType, pageViewType.getSelected());
                dispatchAsync.execute(query, new AsyncCallback<ConfigurationResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not get configuration values.", caught);
                    }

                    @Override
                    public void onSuccess(ConfigurationResult result) {
                        sortOptions.setSelected(result.getConfiguration(ConfigurationKey.PresentationSortOrder));
                        pageViewType.setSelected(result.getConfiguration(ConfigurationKey.PresentationPageViewType));
                    }
                });
            }
        });
        hp.add(button);
        //only resets changes before "submit" button is clicked. i.e. does not have memory of previously submitted changes.
        button = new Button("Reset", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dispatchAsync.execute(new GetConfiguration(MMDB.getUsername()), new AsyncCallback<ConfigurationResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not get configuration values.", caught);
                    }

                    @Override
                    public void onSuccess(ConfigurationResult result) {
                        sortOptions.setSelected(result.getConfiguration(ConfigurationKey.PresentationSortOrder));
                        pageViewType.setSelected(result.getConfiguration(ConfigurationKey.PresentationPageViewType));
                    }
                });
            }
        });
        button.addStyleName("multiAnchor");
        hp.add(button);

        return dp;

    }

    private DisclosurePanel createMapSection(ConfigurationResult configuration) {
        return createSimpleConfigurationSection(configuration, "Map", "GoogleMapKey", ConfigurationKey.GoogleMapKey, true);
    }

    private DisclosurePanel createUpdateSection() {
        DisclosurePanel dp = new DisclosurePanel("Server Updates");
        dp.addStyleName("datasetDisclosurePanel");
        dp.setOpen(false);
        VerticalPanel vp = new VerticalPanel();
        vp.setWidth("100%");
        dp.add(vp);

        if (permissions.isPermitted(Permission.REINDEX_FULLTEXT)) {

            final Anchor luceneAnchor = new Anchor("Re-build full-text index");
            luceneAnchor.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    luceneAnchor.setEnabled(false);
                    luceneAnchor.setText("Requesting...");
                    dispatchAsync.execute(new ReindexLucene(), new AsyncCallback<ReindexLuceneResult>() {
                        public void onFailure(Throwable caught) {
                            new ConfirmDialog("Error", "Error reindexing");
                            luceneAnchor.setText("Reindex Lucene");
                            luceneAnchor.setEnabled(true);
                        }

                        public void onSuccess(ReindexLuceneResult result) {
                            new ConfirmDialog("Started", "Queued " + result.getNumberQueued() + " dataset(s) for reindexing");
                            luceneAnchor.setText("Reindex Lucene");
                            luceneAnchor.setEnabled(true);
                        }
                    });
                }
            });
            vp.add(luceneAnchor);
        }

        final Anchor updateAnchor = new Anchor("Update Context");
        updateAnchor.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                updateAnchor.setEnabled(false);
                updateAnchor.setText("Update Context Running...");
                dispatchAsync.execute(new ContextConvert(), new AsyncCallback<EmptyResult>() {

                    public void onFailure(Throwable caught) {
                        updateAnchor.setText("Update Context Failed");
                        updateAnchor.setEnabled(true);
                    }

                    @Override
                    public void onSuccess(EmptyResult result) {
                        updateAnchor.setText("Update Context Finished");
                        updateAnchor.setEnabled(true);
                    }
                });
            }
        });
        vp.add(updateAnchor);

        return dp;
    }

    private DisclosurePanel createVIVOConfigurationSection(ConfigurationResult configuration) {
        return createSimpleConfigurationSection(configuration, "VIVO Configuration", new String[] { "VIVO Query Endpoint", "VIVO Identifier Prefix" }, new ConfigurationKey[] { ConfigurationKey.VIVOQUERYURL, ConfigurationKey.VIVOIDENTIFIERURL }, false);
    }

    private DisclosurePanel createSimpleConfigurationSection(ConfigurationResult configuration, String sectionLabel, String keyLabel, final ConfigurationKey configKey, boolean startOpen) {
        return createSimpleConfigurationSection(configuration, sectionLabel, new String[] { keyLabel }, new ConfigurationKey[] { configKey }, startOpen);
    }

    private DisclosurePanel createSimpleConfigurationSection(ConfigurationResult configuration, String sectionLabel, final String keyLabels[], final ConfigurationKey[] configKeys, boolean startOpen) {

        DisclosurePanel dp = new DisclosurePanel(sectionLabel);
        dp.addStyleName("datasetDisclosurePanel");
        dp.setOpen(startOpen);
        VerticalPanel vp = new VerticalPanel();
        vp.setWidth("100%");
        dp.add(vp);

        /*HorizontalPanel hp = new HorizontalPanel();
        vp.add(hp);*/

        final FlexTable table = new FlexTable();
        for (int i = 0; i < keyLabels.length; i++ ) {
            table.setText(i, 0, keyLabels[i]);
            final TextBox key = new TextBox();
            key.addStyleName("multiAnchor");
            key.setText(configuration.getConfiguration(configKeys[i]));
            key.setVisibleLength(80);
            table.setWidget(i, 1, key);
        }
        /*hp.add(key);*/
        vp.add(table);
        // buttons
        HorizontalPanel hp = new HorizontalPanel();
        vp.add(hp);

        Button button = new Button("Submit", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                //FixMe - put both vals in the same query - see mail section above
                for (int i = 0; i < keyLabels.length; i++ ) {
                    final int j = i;
                    SetConfiguration query = new SetConfiguration(MMDB.getUsername());
                    query.setConfiguration(configKeys[j], ((TextBox) table.getWidget(j, 1)).getText());

                    dispatchAsync.execute(query, new AsyncCallback<ConfigurationResult>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            GWT.log("Could not set configuration value for " + configKeys[j], caught);
                        }

                        @Override
                        public void onSuccess(ConfigurationResult result) {
                            ((TextBox) table.getWidget(j, 1)).setText(result.getConfiguration(configKeys[j]));
                        }
                    });
                }
            }
        });
        hp.add(button);

        button = new Button("Reset", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                //FixMe - put both vals in the same query - see mail section above
                for (int i = 0; i < keyLabels.length; i++ ) {
                    final int j = i;

                    dispatchAsync.execute(new GetConfiguration(MMDB.getUsername(), configKeys[j]), new AsyncCallback<ConfigurationResult>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            GWT.log("Could not get configuration value for" + configKeys[j], caught);
                        }

                        @Override
                        public void onSuccess(ConfigurationResult result) {
                            ((TextBox) table.getWidget(j, 1)).setText(result.getConfiguration(configKeys[j]));
                        }
                    });
                }
            }
        });
        button.addStyleName("multiAnchor");
        hp.add(button);

        return dp;

    }

    private DisclosurePanel createVAConfigurationSection(ConfigurationResult configuration) {
        return createSimpleConfigurationSection(configuration, "VA Configuration", "VA End Point", ConfigurationKey.VAURL, false);
    }

    private DisclosurePanel createDiscoveryConfigurationSection(ConfigurationResult configuration) {
        return createSimpleConfigurationSection(configuration, "ACR Discovery Configuration", "Discovery App URL", ConfigurationKey.DiscoveryURL, false);
    }

    private DisclosurePanel createExtractorSection(ConfigurationResult configuration) {
        DisclosurePanel dp = new DisclosurePanel("Preview/Extraction Services");
        dp.addStyleName("datasetDisclosurePanel");
        dp.setOpen(false);

        VerticalPanel vp = new VerticalPanel();
        vp.setWidth("100%");
        dp.add(vp);

        HorizontalPanel hp = new HorizontalPanel();
        vp.add(hp);

        hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        hp.add(new Label("URL"));
        final TextBox key = new TextBox();
        key.addStyleName("multiAnchor");
        key.setText(configuration.getConfiguration(ConfigurationKey.ExtractorUrl));
        key.setVisibleLength(80);
        hp.add(key);
        deleteOld = false;
        feedbackPanel = new SimplePanel();
        hp.add(feedbackPanel);

        // buttons
        hp = new HorizontalPanel();
        vp.add(hp);

        // submit new url button
        Button button = new Button("Submit", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                SetConfiguration query = new SetConfiguration(MMDB.getUsername());
                query.setConfiguration(ConfigurationKey.ExtractorUrl,
                        key.getText());
                dispatchAsync.execute(query, new AsyncCallback<ConfigurationResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not get configuration values.", caught);
                    }

                    @Override
                    public void onSuccess(ConfigurationResult result) {
                        // if the input url is not saved in the configuration key by the execution, then it is invalid
                        String input = key.getText();
                        if (!input.endsWith("/")) {
                            input += "/";
                        }
                        if (result.getConfiguration(ConfigurationKey.ExtractorUrl).equalsIgnoreCase(input)) {
                            feedbackPanel.clear();
                            key.setText(result.getConfiguration(ConfigurationKey.ExtractorUrl));
                        }
                        else {
                            GWT.log("Invalid Extraction Server URL", null);
                            // display feedback message
                            Label message = new Label(
                                    "Invalid Extraction Server URL");
                            message.addStyleName("loginError");
                            feedbackPanel.clear();
                            feedbackPanel.add(message);
                        }
                    }
                });
            }
        });
        hp.add(button);

        // reset URL button
        button = new Button("Reset", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dispatchAsync.execute(new GetConfiguration(MMDB.getUsername(), ConfigurationKey.ExtractorUrl), new AsyncCallback<ConfigurationResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not get configuration values.", caught);
                    }

                    @Override
                    public void onSuccess(ConfigurationResult result) {
                        // clear feedback message and pull the current extractor's URL from the server
                        feedbackPanel.clear();
                        key.setText(result.getConfiguration(ConfigurationKey.ExtractorUrl));
                    }
                });
            }
        });
        button.addStyleName("multiAnchor");
        hp.add(button);

        // rerun panel
        hp = new HorizontalPanel();
        vp.add(hp);

        hp.add(new Label("Rerun Extraction on All Data: "));

        final CheckBox deleteOldBox = new CheckBox();
        deleteOldBox.setValue(false);
        deleteOldBox.setText(" Delete Old");
        hp.add(deleteOldBox);

        deleteOldBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(final ValueChangeEvent<Boolean> event) {
                deleteOld = deleteOldBox.getValue();
            }
        });

        // rerun button
        hp = new HorizontalPanel();
        vp.add(hp);
        button = new Button("OK", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dispatchAsync.execute(new ExtractionService(null, deleteOld), new AsyncCallback<ExtractionServiceResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Error submitting extraction jobs.", caught);
                    }

                    @Override
                    public void onSuccess(ExtractionServiceResult result) {
                        GWT.log("Success submitting extraction jobs ");
                    }
                });
            }
        });
        hp.add(button);

        // Google Doc Viewer panel
        hp = new HorizontalPanel();
        vp.add(hp);

        hp.add(new Label("Previews: "));

        final CheckBox useGoogleDocViewerBox = new CheckBox();
        useGoogleDocViewerBox.setValue(configuration.getConfiguration(ConfigurationKey.UseGoogleDocViewer).equalsIgnoreCase("true"));
        useGoogleDocViewerBox.setText("Enable Google Doc Viewer");
        hp.add(useGoogleDocViewerBox);

        useGoogleDocViewerBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(final ValueChangeEvent<Boolean> event) {
                SetConfiguration query = new SetConfiguration(MMDB.getUsername());
                query.setConfiguration(ConfigurationKey.UseGoogleDocViewer, useGoogleDocViewerBox.getValue().toString());
                dispatchAsync.execute(query, new AsyncCallback<ConfigurationResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not set configuration value for UseGoogleDocViewer.", caught);
                    }

                    @Override
                    public void onSuccess(ConfigurationResult result) {
                        //Shouldn't have to do anything, but confirm by setting checkbox with result
                        useGoogleDocViewerBox.setValue(result.getConfiguration(ConfigurationKey.UseGoogleDocViewer).equalsIgnoreCase("true"));
                    }
                });

            }
        });

        return dp;
    }

}
