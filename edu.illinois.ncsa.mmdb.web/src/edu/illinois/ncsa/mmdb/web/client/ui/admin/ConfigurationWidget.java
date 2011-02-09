package edu.illinois.ncsa.mmdb.web.client.ui.admin;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ConfigurationResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ContextConvert;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetConfiguration;
import edu.illinois.ncsa.mmdb.web.client.dispatch.HasPermissionResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ReindexLucene;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ReindexLuceneResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetConfiguration;
import edu.illinois.ncsa.mmdb.web.client.ui.ConfirmDialog;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.uiuc.ncsa.cet.bean.rbac.medici.Permission;

public class ConfigurationWidget extends Composite {
    private DispatchAsync       dispatchAsync;
    private TextBox             key;
    private HasPermissionResult permissions;
    private VerticalPanel       mainPanel;

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

        // google map key
        mainPanel.add(createMapSection(configuration));

        // server updates.
        mainPanel.add(createUpdateSection());

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

    private DisclosurePanel createMapSection(ConfigurationResult configuration) {
        DisclosurePanel dp = new DisclosurePanel("Map");
        dp.addStyleName("datasetDisclosurePanel");
        dp.setOpen(true);
        VerticalPanel vp = new VerticalPanel();
        vp.setWidth("100%");
        dp.add(vp);

        HorizontalPanel hp = new HorizontalPanel();
        vp.add(hp);

        hp.add(new Label("Google Map Key"));
        key = new TextBox();
        key.addStyleName("multiAnchor");
        key.setText(configuration.getConfiguration(ConfigurationKey.GoogleMapKey));
        key.setVisibleLength(80);
        hp.add(key);

        // buttons
        hp = new HorizontalPanel();
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

}
