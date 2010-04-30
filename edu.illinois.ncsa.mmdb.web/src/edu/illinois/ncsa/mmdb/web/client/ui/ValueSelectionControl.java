package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;

/**
 * Any control that allows a user to select from a list of labels, the
 * underlying value of said label
 */
public interface ValueSelectionControl<T> extends HasValueChangeHandlers<T> {
    void addItem(String title, T value);

    void removeItem(String title);

    void setSelected(T value);

    T getSelected();
}
