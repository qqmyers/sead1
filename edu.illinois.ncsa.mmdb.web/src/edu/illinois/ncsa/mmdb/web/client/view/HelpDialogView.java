package edu.illinois.ncsa.mmdb.web.client.view;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.presenter.HelpPresenter.Display;

public class HelpDialogView extends DialogBox implements Display {
    final FlowPanel contentLayout;
    final Button    dismissButton;

    public HelpDialogView(String title) {
        setText(title);

        VerticalPanel theLayout = new VerticalPanel();

        contentLayout = new FlowPanel();
        theLayout.add(contentLayout);

        dismissButton = new Button("Close");
        theLayout.add(dismissButton);
        theLayout.setCellHorizontalAlignment(dismissButton, HasAlignment.ALIGN_CENTER);

        setWidget(theLayout);
    }

    @Override
    public void addContent(Widget content) {
        contentLayout.add(content);
    }

    @Override
    public void reveal() {
        center();
        show();
    }

    @Override
    public void dismiss() {
        hide();
    }

    @Override
    public HasClickHandlers getDismissControl() {
        return dismissButton;
    }

}
