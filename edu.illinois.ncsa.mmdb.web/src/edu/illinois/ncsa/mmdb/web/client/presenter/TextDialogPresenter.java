package edu.illinois.ncsa.mmdb.web.client.presenter;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.mvp.Presenter;

public abstract class TextDialogPresenter implements Presenter {

    public interface Display {
        HasClickHandlers getSubmitButton();

        HasKeyUpHandlers getTextBox();

        void hide();

        HasClickHandlers getCancelButton();

        HasText getTextString();

        Widget asWidget();
    }
}
