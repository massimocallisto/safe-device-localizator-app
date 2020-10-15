package it.filippetti.sp.android.bus.io.impl;

public class AsyncResult<T> implements it.filippetti.sp.android.bus.io.AsyncResult {
    public T result;
    public Throwable cause;

    @Override
    public T result() {
        return result;
    }

    @Override
    public boolean succeeded() {
        return cause != null ? false : true;
    }

    @Override
    public boolean failed() {
        return !succeeded();
    }

    @Override
    public Throwable cause() {
        return cause;
    }




}
