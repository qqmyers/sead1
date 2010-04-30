package edu.illinois.ncsa.mmdb.web.client.view;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;

import edu.illinois.ncsa.mmdb.web.client.presenter.EditableUserMetadataPresenter.Display;
import edu.illinois.ncsa.mmdb.web.client.ui.LabeledListBox;

public class BatchAddMetadataView extends DialogBox implements Display {
    HorizontalPanel      thePanel;
    final LabeledListBox selector;
    final TextBox        valueBox;
    final Button         submitButton;
    final Button         cancelButton;

    public BatchAddMetadataView(String title) {
        setText(title);
        thePanel = new HorizontalPanel();
        add(thePanel);
        selector = new LabeledListBox("Set field:");
        thePanel.add(selector);
        valueBox = new TextBox();
        thePanel.add(valueBox);
        submitButton = new Button("Submit");
        thePanel.add(submitButton);
        cancelButton = new Button("Cancel");
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });
        thePanel.add(cancelButton);
        center();
    }

    @Override
    public String getSelectedField() {
        // TODO Auto-generated method stub
        return selector.getSelected();
    }

    @Override
    public HasClickHandlers getSubmitControl() {
        // TODO Auto-generated method stub
        return submitButton;
    }

    @Override
    public String getValue() {
        // TODO Auto-generated method stub
        return valueBox.getText();
    }

    @Override
    public void addMetadataField(String uri, String name) {
        selector.addItem(name, uri);
        center();
    }

    @Override
    public void addMetadataValue(String uri, String value) {
        // do nothing, this view doesn't display values
    }

    @Override
    public void onFailure() {
        // FIXME warn the user
    }

    @Override
    public void onSuccess() {
        hide();
    }
}
