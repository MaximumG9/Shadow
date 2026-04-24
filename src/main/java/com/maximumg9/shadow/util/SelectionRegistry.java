package com.maximumg9.shadow.util;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public class SelectionRegistry<T> {
    private final List<T> values;
    private final List<SelectionCallback<T>> selectors = new ArrayList<>();

    public SelectionRegistry(List<T> startingList) {
        this.values = startingList;
    }

    public SelectionRegistry() {
        this(new ArrayList<>());
    }

    public void add(T value) {
        values.add(value);
        sendUpdatedList();
    }

    public void remove(T value) {
        values.remove(value);
        sendUpdatedList();
    }

    public ImmutableList<T> get() { return ImmutableList.copyOf(values); }

    public void subscribe(SelectionCallback<T> sel) {
        selectors.add(sel);
    }

    public void unsubscribe(SelectionCallback<T> sel) {
        selectors.remove(sel);
    }

    private void sendUpdatedList() {
        selectors.forEach(selectionCallback -> selectionCallback.updateSelection(get()));
    }

    public interface SelectionCallback<T> {
        void updateSelection(List<T> newValues);
    }
}
