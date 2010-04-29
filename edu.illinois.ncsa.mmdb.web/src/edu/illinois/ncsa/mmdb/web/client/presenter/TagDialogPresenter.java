package edu.illinois.ncsa.mmdb.web.client.presenter;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.dispatch.TagResource;
import edu.illinois.ncsa.mmdb.web.client.dispatch.TagResourceResult;
import edu.illinois.ncsa.mmdb.web.client.mvp.Presenter;
import edu.illinois.ncsa.mmdb.web.client.mvp.View;

/**
 * @author Luigi Marini
 * 
 */
public class TagDialogPresenter implements Presenter {

    private final MyDispatchAsync dispatch;
    private final HandlerManager  eventBus;
    private final Display         display;
    private Set<String>           selectedResources;
    private final boolean         delete;           // should we delete the tags instead?

    public TagDialogPresenter(MyDispatchAsync dispatch, HandlerManager eventBus, Display display) {
        this(dispatch, eventBus, display, false);
    }

    public TagDialogPresenter(MyDispatchAsync dispatch, HandlerManager eventBus, Display display, boolean delete) {
        this.dispatch = dispatch;
        this.eventBus = eventBus;
        this.display = display;
        this.selectedResources = new HashSet<String>();
        this.delete = delete;
    }

    @Override
    public void bind() {
        display.getSubmitButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                tag();
                display.hide();
            }
        });

        display.getCancelButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                display.hide();
            }
        });

    }

    /**
     * Tag resources if tag is not empty.
     */
    protected void tag() {
        Set<String> tagSet = new HashSet<String>();
        for (String s : display.getTagString().getText().split(",") ) {
            if (!s.equals("")) {
                tagSet.add(s);
            }
        }
        if (!tagSet.isEmpty()) {
            for (String id : selectedResources ) {

                dispatch.execute(new TagResource(id, tagSet, delete), new AsyncCallback<TagResourceResult>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Failed tagging resource", caught);
                    }

                    @Override
                    public void onSuccess(TagResourceResult result) {
                        GWT.log("Resource successfully tagged", null);
                    }
                });
            }
        }
    }

    @Override
    public View getView() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setSelectedResources(Set<String> selectedResources) {
        this.selectedResources = selectedResources;
    }

    public Set<String> getSelectedResources() {
        return selectedResources;
    }

    public interface Display {
        HasClickHandlers getSubmitButton();

        void hide();

        HasClickHandlers getCancelButton();

        HasText getTagString();

        Widget asWidget();
    }

}
