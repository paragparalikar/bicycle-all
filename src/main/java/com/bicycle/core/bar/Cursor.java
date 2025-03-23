package com.bicycle.core.bar;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public interface Cursor<T> extends AutoCloseable {
    
    public static final Cursor<Bar> EMPTY = new Cursor<>() {
        @Override public void close() throws Exception {}
        @Override public int size() { return 0; }
        @Override public void advance(Bar bar) {}
    };
    
    int size();

    void advance(T model);

    default List<T> toList(Supplier<T> modelBuilder){
        final int size = size();
        final List<T> models = new ArrayList<>(size);
        for(int index = 0; index < size; index++) {
            final T model = modelBuilder.get();
            advance(model);
            models.add(model);
        }
        return models;
    }
    
}
