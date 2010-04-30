package edu.illinois.ncsa.mmdb.web.client.presenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;

import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;

public abstract class EditableUserMetadataPresenter extends UserMetadataPresenter {
    Display editDisplay;

    public EditableUserMetadataPresenter(MyDispatchAsync dispatch, Display display) {
        super(dispatch, display);
        editDisplay = display;
    }

    public void bind() {
        super.bind();

        if (editDisplay.getSubmitControl() != null) {
            editDisplay.getSubmitControl().addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    String predicate = editDisplay.getSelectedField();
                    String value = editDisplay.getValue();
                    onSetMetadataField(predicate, value);
                }
            });
        }
    }

    public interface Display extends UserMetadataPresenter.Display {
        /**
         * Called by the presenter to get the uri of the currently-selected
         * predicate.
         */
        String getSelectedField();

        /**
         * Called by the presenter to get the value that the user wants to set
         * the currently-selected predicate to
         */
        String getValue();

        /**
         * Called by the presenter to get the control that sets a field/value.
         */
        HasClickHandlers getSubmitControl();

        /** Called by the presenter when the edit operation succeeds */
        void onSuccess();

        /** Called by the presenter when the edit operation fails */
        void onFailure();
    }

    /**
     * Implement this to handle a user edit action.
     * 
     * @param predicate
     *            the predicate being set
     * @param value
     *            the value to to set it to (null to clear the value)
     */
    protected abstract void onSetMetadataField(String predicate, String value);
}
