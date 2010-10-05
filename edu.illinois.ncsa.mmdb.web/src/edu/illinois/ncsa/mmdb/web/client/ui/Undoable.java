package edu.illinois.ncsa.mmdb.web.client.ui;

/**
 * A UI control that can be reverted to a previous state
 * 
 * @author futrelle
 * 
 */
public interface Undoable {
    /**
     * Mark current state for undo
     */
    void mark();

    /**
     * Revert to marked state
     */
    void undo();

    /**
     * Is control in the marked state, or has it changed?
     * 
     * @return
     */
    boolean hasChanged();
}
