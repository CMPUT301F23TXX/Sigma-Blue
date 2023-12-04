package com.example.sigma_blue.context;

import java.util.ArrayList;

/**
 * Holds a list of 'selectable' entities. used instead of an array list as it enforces only certain
 * operations on the items.
 * @param <T>
 */
public class SelectedEntities<T> {
    ArrayList<T> selected;
    public SelectedEntities() {
        selected = new ArrayList<>();
    }
    public void toggleHighlight(T e) {
        if (!this.selected.contains(e)){
            this.selected.add(e);
        } else {
            this.selected.remove(e);
        }
    }

    /**
     * Clearing the entire selected view. Needed for delete features.
     */
    public void clear() {
        this.selected.clear();
    }

    public ArrayList<T> getSelected() {
        return selected;
    }
    public void resetSelected() {
        selected = new ArrayList<T>();
    }
    public boolean empty() {
        return selected.size() == 0;
    }
    public int size() {
        return selected.size();
    }
    public void updateEntity(T newE, T oldE) {
        this.removeEntity(oldE);
        selected.add(newE);
    }
    private void removeEntity(T e) {
        ArrayList<T> newSelected = new ArrayList<>();
        for (T i : this.selected) {
            if (i != e) {
                newSelected.add(e);
            }
        }
        this.selected = newSelected;
    }
}
