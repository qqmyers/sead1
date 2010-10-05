package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.CheckBox;

public class UndoableCheckBox extends CheckBox implements Undoable {
    private boolean markedState;

    public UndoableCheckBox() {
        super();
        // TODO Auto-generated constructor stub
    }

    public UndoableCheckBox(Element elem) {
        super(elem);
        // TODO Auto-generated constructor stub
    }

    public UndoableCheckBox(String label, boolean asHTML) {
        super(label, asHTML);
        // TODO Auto-generated constructor stub
    }

    public UndoableCheckBox(String label) {
        super(label);
        // TODO Auto-generated constructor stub
    }

    public void mark() {
        markedState = getValue();
    }

    public void undo() {
        setValue(markedState);
    }

    public boolean hasChanged() {
        return getValue() != markedState;
    }
}
