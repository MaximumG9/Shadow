package com.maximumg9.shadow.util;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class SelectionRegistry<T> implements Collection<T> {
    private final List<T> values;
    private final List<SelectionCallback<T>> selectors = new ArrayList<>();

    public SelectionRegistry(List<T> startingList) {
        this.values = startingList;
    }

    public SelectionRegistry() {
        this(new ArrayList<>());
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return values.contains(o);
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return get().iterator();
    }

    @Override
    public Object @NotNull [] toArray() {
        return values.toArray();
    }

    @Override
    public <T1> T1 @NotNull [] toArray(T1 @NotNull [] a) {
        return values.toArray(a);
    }

    public boolean add(T value) {
        boolean result = values.add(value);
        sendUpdatedList();
        return result;
    }

    public boolean remove(Object value) {
        boolean result = values.remove(value);
        sendUpdatedList();
        return result;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        boolean result = values.containsAll(c);
        sendUpdatedList();
        return result;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        boolean result = values.addAll(c);
        sendUpdatedList();
        return result;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        boolean result = values.removeAll(c);
        sendUpdatedList();
        return result;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        boolean result = values.retainAll(c);
        sendUpdatedList();
        return result;
    }

    @Override
    public void clear() {
        values.clear();
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
