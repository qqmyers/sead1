package edu.illinois.ncsa.mmdb.web.client.presenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.mvp.Presenter;
import edu.illinois.ncsa.mmdb.web.client.mvp.View;

/**
 * Display some dismissable, context-sensitive help.
 * Add content to the display using the presenter's addContent method.
 * 
 * @author futrelle
 * 
 */
public class HelpPresenter implements Presenter {
    private final Display display;

    public HelpPresenter(Display display) {
        this.display = display;
    }

    @Override
    public void bind() {
        display.getDismissControl().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                display.dismiss();
            }
        });
        display.reveal();
    }

    @Override
    public View getView() {
        // TODO Auto-generated method stub
        return null;
    }

    public void addContent(Widget contentItem) {
        display.addContent(contentItem);
    }

    public interface Display {
        void setTitle(String title);

        void addContent(Widget content);

        void reveal();

        void dismiss();

        HasClickHandlers getDismissControl();
    }
}
