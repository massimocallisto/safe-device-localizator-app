package it.filippetti.sp.android.bus.io;

public interface Handler<E> {
    public void handle(E result);
}
