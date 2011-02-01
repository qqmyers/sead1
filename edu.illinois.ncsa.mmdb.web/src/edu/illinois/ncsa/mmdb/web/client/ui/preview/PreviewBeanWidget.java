package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.event.PreviewSectionChangedEvent;
import edu.uiuc.ncsa.cet.bean.PreviewBean;

public abstract class PreviewBeanWidget<T extends PreviewBean> {
    /** preview bean that is rendered. */
    private T                      pb = null;

    /** the actual widget that will hold the preview */
    private Widget                 widget;

    /** eventbus that is used when a new section is selected */
    protected final HandlerManager eventBus;

    public PreviewBeanWidget(HandlerManager eventBus) {
        this.eventBus = eventBus;
    }

    public void setPreviewBean(T pb) {
        this.pb = pb;
    }

    public T getPreviewBean() {
        return pb;
    }

    /**
     * Create a new unique instance of this preview widget.
     * 
     * @return a unique instance of this preview widget.
     */
    public abstract PreviewBeanWidget<T> newWidget();

    /**
     * Return the PreviewBean class this widget can show.
     * 
     * @return
     */
    public abstract Class<? extends PreviewBean> getPreviewBeanClass();

    public void setWidget(Widget widget) {
        this.widget = widget;
    }

    /**
     * Return the Widget that is responsible for showing the preview. Once the
     * widget needs to be actually filled out the show method will be called.
     * 
     * @return
     */
    public Widget getWidget() {
        return widget;
    }

    /**
     * Returns the unique id associated with the widget.
     * 
     * @return
     */
    public String getWidgetID() {
        return widget.getElement().getId();
    }

    /**
     * Checks to see which of the two objects will fit the given area best. The
     * object that is the best fit will be returned.
     * 
     * @param obj1
     *            the first Object to be checked.
     * @param obj2
     *            the second Object to be checed.
     * @param width
     *            the width of the space available, this can be negative if no
     *            width requirement is placed.
     * @param height
     *            the height of the space available, this can be negative if no
     *            height requirement is placed.
     * @return the object that best fits the width and height.
     */
    public T bestFit(T obj1, T obj2, int width, int height) {
        return obj2;
    }

    /**
     * Called when the preview widget is shown. This will first call showSection
     * followed by firing an event. If this function is overridden the new
     * function should call fireSectionChanged.
     */
    public void show() {
        showSection();
        fireSectionChanged();
    }

    /**
     * This function is called when the actual widget should be rendered.
     */
    protected abstract void showSection();

    /**
     * Called when the preview widget is hidden.
     */
    public void hide() {
    }

    /**
     * Fires a new event with the new current section on the eventbus.
     */
    public void fireSectionChanged() {
        PreviewSectionChangedEvent event = new PreviewSectionChangedEvent();
        event.setSection(getSection());
        eventBus.fireEvent(event);
    }

    /**
     * Sets the current location in the preview bean.
     * 
     * @param section
     *            the current section shown.
     */
    public abstract void setSection(String section);

    /**
     * Return the current section in the preview bean.
     * 
     * @return current section shown.
     */
    public abstract String getSection();

    /**
     * Label to be shown for this widget.
     * 
     * @return label shown to the user.
     */
    public abstract String getAnchorText();
}
