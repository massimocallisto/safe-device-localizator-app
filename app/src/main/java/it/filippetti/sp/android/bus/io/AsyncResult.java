package it.filippetti.sp.android.bus.io;

public interface AsyncResult<T>  {
    T result();

    boolean succeeded();
    boolean failed();

    Throwable cause();

}
