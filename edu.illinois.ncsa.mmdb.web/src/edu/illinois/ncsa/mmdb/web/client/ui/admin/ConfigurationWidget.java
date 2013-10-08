package edu.illinois.ncsa.mmdb.web.client.ui.admin;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

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

        // google map key
        mainPanel.add(createMapSection(configuration));

        // secret api key
        mainPanel.add(createRemoteAPISection(configuration));

        // server updates.
        mainPanel.add(createUpdateSection());

        // vivo configuration.
        mainPanel.add(createVIVOConfigurationSection(configuration));

        // va configuration.
        mainPanel.add(createVAConfigurationSection(configuration));

        // extractor configuration
        mainPanel.add(createExtractorSection(configuration));
    }

    private DisclosurePanel createMailSection(ConfigurationResult configuration) {
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

        // buttons
        HorizontalPanel hp = new HorizontalPanel();
        vp.add(hp);

        Button button = new Button("Submit", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                SetConfiguration query = new SetConfiguration(MMDB.getUsername());
                query.setConfiguration(ConfigurationKey.ProjectName, name.getText());
                query.setConfiguration(ConfigurationKey.ProjectURL, url.getText());
                query.setConfiguration(ConfigurationKey.ProjectDescription, desc.getText());
                dispatchAsync.execute(query, new AsyncCallback<ConfigurationResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not get configuration values.", caught);
                    }

                    @Override
                    public void onSuccess(ConfigurationResult result) {
                        name.setText(result.getConfiguration(ConfigurationKey.ProjectName));
                        url.setText(result.getConfiguration(ConfigurationKey.ProjectURL));
                        desc.setText(result.getConfiguration(ConfigurationKey.ProjectDescription));
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
                        name.setText(result.getConfiguration(ConfigurationKey.ProjectName));
                        url.setText(result.getConfiguration(ConfigurationKey.ProjectURL));
                        desc.setText(result.getConfiguration(ConfigurationKey.ProjectDescription));
                    }
                });
            }
        });
        button.addStyleName("multiAnchor");
        hp.add(button);

        return dp;
    }

    private DisclosurePanel createMapSection(ConfigurationResult configuration) {
        DisclosurePanel dp = new DisclosurePanel("Map");
        dp.addStyleName("datasetDisclosurePanel");
        dp.setOpen(true);
        VerticalPanel vp = new VerticalPanel();
        vp.setWidth("100%");
        dp.add(vp);

        HorizontalPanel hp = new HorizontalPanel();
        /*vp.add(hp);

        hp.add(new Label("Google Map Key"));*/
        FlexTable table = new FlexTable();

        hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        hp.add(new Label("Google Map Key"));
        final TextBox key = new TextBox();
        key.addStyleName("multiAnchor");
        key.setText(configuration.getConfiguration(ConfigurationKey.GoogleMapKey));
        key.setVisibleLength(80);
        table.setText(0, 0, "Google Map Key");
        table.setWidget(0, 1, key);
        vp.add(table);
        /*hp.add(key);*/

        // buttons
        //HorizontalPanel hp = new HorizontalPanel();
        vp.add(hp);

        Button button = new Button("Submit", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                SetConfiguration query = new SetConfiguration(MMDB.getUsername());
                query.setConfiguration(ConfigurationKey.GoogleMapKey, key.getText());
                dispatchAsync.execute(query, new AsyncCallback<ConfigurationResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not get configuration values.", caught);
                    }

                    @Override
                    public void onSuccess(ConfigurationResult result) {
                        key.setText(result.getConfiguration(ConfigurationKey.GoogleMapKey));
                    }
                });
            }
        });
        hp.add(button);

        button = new Button("Reset", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dispatchAsync.execute(new GetConfiguration(MMDB.getUsername(), ConfigurationKey.GoogleMapKey), new AsyncCallback<ConfigurationResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not get configuration values.", caught);
                    }

                    @Override
                    public void onSuccess(ConfigurationResult result) {
                        key.setText(result.getConfiguration(ConfigurationKey.GoogleMapKey));
                    }
                });
            }
        });
        button.addStyleName("multiAnchor");
        hp.add(button);

        return dp;
    }

    private DisclosurePanel createRemoteAPISection(ConfigurationResult configuration) {
        DisclosurePanel dp = new DisclosurePanel("API Key");
        dp.addStyleName("datasetDisclosurePanel");
        dp.setOpen(true);
        VerticalPanel vp = new VerticalPanel();
        vp.setWidth("100%");
        dp.add(vp);

        HorizontalPanel hp = new HorizontalPanel();
        vp.add(hp);

        hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        hp.add(new Label("Remote API Key"));
        final TextBox key = new TextBox();
        key.addStyleName("multiAnchor");
        key.setText(configuration.getConfiguration(ConfigurationKey.RemoteAPIKey));
        key.setVisibleLength(80);
        hp.add(key);

        // buttons
        hp = new HorizontalPanel();
        vp.add(hp);

        Button button = new Button("Generate", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                SetConfiguration query = new SetConfiguration(MMDB.getUsername());
                query.setConfiguration(ConfigurationKey.RemoteAPIKey, UUID.randomUUID().toString());
                dispatchAsync.execute(query, new AsyncCallback<ConfigurationResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not get configuration values.", caught);
                    }

                    @Override
                    public void onSuccess(ConfigurationResult result) {
                        key.setText(result.getConfiguration(ConfigurationKey.RemoteAPIKey));
                    }
                });
            }
        });
        button.addStyleName("multiAnchor");
        hp.add(button);

        button = new Button("Submit", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                SetConfiguration query = new SetConfiguration(MMDB.getUsername());
                query.setConfiguration(ConfigurationKey.RemoteAPIKey, key.getText());
                dispatchAsync.execute(query, new AsyncCallback<ConfigurationResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not get configuration values.", caught);
                    }

                    @Override
                    public void onSuccess(ConfigurationResult result) {
                        key.setText(result.getConfiguration(ConfigurationKey.RemoteAPIKey));
                    }
                });
            }
        });
        hp.add(button);

        button = new Button("Reset", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dispatchAsync.execute(new GetConfiguration(MMDB.getUsername(), ConfigurationKey.RemoteAPIKey), new AsyncCallback<ConfigurationResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not get configuration values.", caught);
                    }

                    @Override
                    public void onSuccess(ConfigurationResult result) {
                        key.setText(result.getConfiguration(ConfigurationKey.RemoteAPIKey));
                    }
                });
            }
        });
        button.addStyleName("multiAnchor");
        hp.add(button);

        return dp;
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
        DisclosurePanel dp = new DisclosurePanel("VIVO Configuration");
        dp.addStyleName("datasetDisclosurePanel");
        dp.setOpen(false);
        VerticalPanel vp = new VerticalPanel();
        vp.setWidth("100%");
        dp.add(vp);

        /*HorizontalPanel hp = new HorizontalPanel();
        vp.add(hp);*/

        FlexTable table = new FlexTable();
        table.setText(0, 0, "VIVO-Joseki End Point");
        /*hp.add(new Label("VIVO-Joseki End Point"));*/
        final TextBox key = new TextBox();
        key.addStyleName("multiAnchor");
        key.setText(configuration.getConfiguration(ConfigurationKey.VIVOJOSEKIURL));
        key.setVisibleLength(80);
        table.setWidget(0, 1, key);
        /*hp.add(key);*/
        vp.add(table);
        // buttons
        HorizontalPanel hp = new HorizontalPanel();
        vp.add(hp);

        Button button = new Button("Submit", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                SetConfiguration query = new SetConfiguration(MMDB.getUsername());
                query.setConfiguration(ConfigurationKey.VIVOJOSEKIURL, key.getText());
                dispatchAsync.execute(query, new AsyncCallback<ConfigurationResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not get configuration values.", caught);
                    }

                    @Override
                    public void onSuccess(ConfigurationResult result) {
                        key.setText(result.getConfiguration(ConfigurationKey.VIVOJOSEKIURL));
                    }
                });
            }
        });
        hp.add(button);

        button = new Button("Reset", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dispatchAsync.execute(new GetConfiguration(MMDB.getUsername(), ConfigurationKey.VIVOJOSEKIURL), new AsyncCallback<ConfigurationResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not get configuration values.", caught);
                    }

                    @Override
                    public void onSuccess(ConfigurationResult result) {
                        key.setText(result.getConfiguration(ConfigurationKey.VIVOJOSEKIURL));
                    }
                });
            }
        });
        button.addStyleName("multiAnchor");
        hp.add(button);

        return dp;

    }

    private DisclosurePanel createVAConfigurationSection(ConfigurationResult configuration) {
        DisclosurePanel dp = new DisclosurePanel("VA Configuration");
        dp.addStyleName("datasetDisclosurePanel");
        dp.setOpen(false);
        VerticalPanel vp = new VerticalPanel();
        vp.setWidth("100%");
        dp.add(vp);

        /*HorizontalPanel hp = new HorizontalPanel();
        vp.add(hp);*/

        FlexTable table = new FlexTable();
        table.setText(0, 0, "VA End Point");
        /*hp.add(new Label("VIVO-Joseki End Point"));*/
        final TextBox key = new TextBox();
        key.addStyleName("multiAnchor");
        key.setText(configuration.getConfiguration(ConfigurationKey.VAURL));
        key.setVisibleLength(80);
        table.setWidget(0, 1, key);
        /*hp.add(key);*/
        vp.add(table);
        // buttons
        HorizontalPanel hp = new HorizontalPanel();
        vp.add(hp);

        Button button = new Button("Submit", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                SetConfiguration query = new SetConfiguration(MMDB.getUsername());
                query.setConfiguration(ConfigurationKey.VAURL, key.getText());
                dispatchAsync.execute(query, new AsyncCallback<ConfigurationResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not get configuration values.", caught);
                    }

                    @Override
                    public void onSuccess(ConfigurationResult result) {
                        key.setText(result.getConfiguration(ConfigurationKey.VAURL));
                    }
                });
            }
        });
        hp.add(button);

        button = new Button("Reset", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dispatchAsync.execute(new GetConfiguration(MMDB.getUsername(), ConfigurationKey.VAURL), new AsyncCallback<ConfigurationResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not get configuration values.", caught);
                    }

                    @Override
                    public void onSuccess(ConfigurationResult result) {
                        key.setText(result.getConfiguration(ConfigurationKey.VAURL));
                    }
                });
            }
        });
        button.addStyleName("multiAnchor");
        hp.add(button);

        return dp;

    }

    private DisclosurePanel createExtractorSection(ConfigurationResult configuration) {
        DisclosurePanel dp = new DisclosurePanel("Extraction Service");
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
        return dp;
    }

}
