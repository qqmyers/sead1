package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.Collection;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.MMDB;

public class SetLicenseDialog extends DialogBox {

    public SetLicenseDialog(String title, Collection<String> batch) {
        super();
        setText(title);
        VerticalPanel thePanel = new VerticalPanel();
        add(thePanel);
        thePanel.add(new LicenseWidget(batch, MMDB.dispatchAsync));
        Button done = new Button("Done");
        done.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                hide();
            }
        });
        thePanel.add(done);
        center();
    }
}
