package com.bicycle.core.bar;

public interface Cursor<T> extends AutoCloseable {
    
    public static final Cursor<Bar> EMPTY = new Cursor<>() {
        @Override public void close() throws Exception {}
        @Override public int size() { return 0; }
        @Override public void advance(Bar bar) {}
    };
    
    int size();

    void advance(T model);
    
}
