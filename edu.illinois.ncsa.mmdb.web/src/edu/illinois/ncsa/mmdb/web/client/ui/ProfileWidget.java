package edu.illinois.ncsa.mmdb.web.client.ui;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ChangeUser;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.uiuc.ncsa.cet.bean.PersonBean;

public class ProfileWidget extends Composite {
    private final DispatchAsync service;
    private final Label         lblFeedBack;

    public ProfileWidget(DispatchAsync service) {
        this.service = service;

        PersonBean pb = MMDB.getSessionState().getCurrentUser();

        FlowPanel fp = new FlowPanel();
        initWidget(fp);

        lblFeedBack = new Label();
        fp.add(lblFeedBack);

        final FlexTable table = new FlexTable();
        table.getColumnFormatter().setWidth(0, "150px");
        table.getColumnFormatter().setWidth(1, "200px");
        fp.add(table);

        addNameRow(table, pb);
        addEmailRow(table, pb);
        addPasswordRow(table, pb);
    }

    private void addNameRow(final FlexTable table, final PersonBean user) {
        final FlowPanel buttons = new FlowPanel();

        final TextBox txtName = new TextBox();

        final int row = table.getRowCount();
        table.setText(row, 0, "Name:");
        table.getCellFormatter().addStyleName(row, 0, "homePageWidgetRow");
        table.setText(row, 1, user.getName());

        final Anchor edit = new Anchor("Change");
        edit.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                txtName.setText(user.getName());
                table.setWidget(row, 1, txtName);
                table.setWidget(row, 2, buttons);
            }
        });
        table.setWidget(row, 2, edit);

        Anchor ok = new Anchor("OK");
        ok.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final String newname = txtName.getText();
                if (newname.equals("")) {
                    showFeedbackMessage("Name can not be empty.");
                    return;
                }
                if (newname.equals(user.getName())) {
                    showFeedbackMessage("Name can not be same as original name.");
                    return;
                }

                service.execute(new ChangeUser(user.getUri(), txtName.getText()), new AsyncCallback<EmptyResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Error changing name.", caught);
                        showFeedbackMessage("Oops! There was an error changing the name");
                    }

                    @Override
                    public void onSuccess(EmptyResult result) {
                        showFeedbackMessage("Name successfully changed");
                        user.setName(newname);
                        table.setText(row, 1, newname);
                    }
                });

                showFeedbackMessage("");
                table.setText(row, 1, user.getName());
                table.setWidget(row, 2, edit);
            }
        });
        buttons.add(ok);

        Anchor cancel = new Anchor("Cancel");
        cancel.addStyleName("multiAnchor");
        cancel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showFeedbackMessage("");
                table.setText(row, 1, user.getName());
                table.setWidget(row, 2, edit);
            }
        });
        buttons.add(cancel);
    }

    private void addEmailRow(final FlexTable table, final PersonBean user) {
        int row = table.getRowCount();
        table.setText(row, 0, "Email:");
        table.getCellFormatter().addStyleName(1, 0, "homePageWidgetRow");
        table.setText(row, 1, user.getEmail());
    }

    private void addPasswordRow(final FlexTable table, final PersonBean user) {
        final PasswordTextBox curPassword = new PasswordTextBox();
        final PasswordTextBox newPassword1 = new PasswordTextBox();
        final PasswordTextBox newPassword2 = new PasswordTextBox();

        final int row = table.getRowCount();
        table.setText(row, 0, "Password:");
        table.getCellFormatter().addStyleName(row, 0, "homePageWidgetRow");
        table.setText(row, 1, "******");

        final Anchor edit = new Anchor("Change");
        edit.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                curPassword.setText("");
                newPassword1.setText("");
                newPassword2.setText("");

                table.getRowFormatter().setVisible(row + 0, false);
                table.getRowFormatter().setVisible(row + 1, true);
                table.getRowFormatter().setVisible(row + 2, true);
                table.getRowFormatter().setVisible(row + 3, true);
            }
        });
        table.setWidget(row, 2, edit);

        final FlowPanel buttons = new FlowPanel();

        table.setText(row + 1, 0, "Current password");
        table.getCellFormatter().addStyleName(row + 1, 0, "homePageWidgetRow");
        table.setWidget(row + 1, 1, curPassword);
        table.getRowFormatter().setVisible(row + 1, false);

        table.setText(row + 2, 0, "New password");
        table.getCellFormatter().addStyleName(row + 2, 0, "homePageWidgetRow");
        table.setWidget(row + 2, 1, newPassword1);
        table.getRowFormatter().setVisible(row + 2, false);

        table.setText(row + 3, 0, "Confirm password");
        table.getCellFormatter().addStyleName(row + 3, 0, "homePageWidgetRow");
        table.setWidget(row + 3, 1, newPassword2);
        table.setWidget(row + 3, 2, buttons);
        table.getRowFormatter().setVisible(row + 3, false);

        Anchor ok = new Anchor("OK");
        ok.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (curPassword.getText().equals("")) {
                    showFeedbackMessage("Current password can not be empty.");
                    return;
                }
                if (newPassword1.getText().equals("")) {
                    showFeedbackMessage("New password can not be empty.");
                    return;
                }
                if (newPassword1.getText().length() < 6) {
                    showFeedbackMessage("New password is to short (minimum of 6 characters).");
                    return;
                }
                if (newPassword1.getText().equals(curPassword.getText())) {
                    showFeedbackMessage("New password can not be the same as old password.");
                    return;
                }
                if (!newPassword1.getText().equals(newPassword2.getText())) {
                    showFeedbackMessage("Passwords are not the same.");
                    return;
                }

                service.execute(new ChangeUser(user.getUri(), curPassword.getText(), newPassword1.getText()), new AsyncCallback<EmptyResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Error updating password", caught);
                        showFeedbackMessage("Oops! There was an error updating the password");
                    }

                    @Override
                    public void onSuccess(EmptyResult result) {
                        showFeedbackMessage("Password successfully updated");
                    }
                });

                showFeedbackMessage("");
                table.getRowFormatter().setVisible(row + 0, true);
                table.getRowFormatter().setVisible(row + 1, false);
                table.getRowFormatter().setVisible(row + 2, false);
                table.getRowFormatter().setVisible(row + 3, false);
            }
        });
        buttons.add(ok);

        Anchor cancel = new Anchor("Cancel");
        cancel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showFeedbackMessage("");
                table.getRowFormatter().setVisible(row + 0, true);
                table.getRowFormatter().setVisible(row + 1, false);
                table.getRowFormatter().setVisible(row + 2, false);
                table.getRowFormatter().setVisible(row + 3, false);
            }
        });
        buttons.add(cancel);
    }

    private void showFeedbackMessage(String msg) {
        lblFeedBack.setText(msg);
    }
}
