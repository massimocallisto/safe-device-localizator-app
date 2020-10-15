package it.filippetti.sp.android.bus.io;

public class MessageConsumer<T> {
    private String address;
    private Handler<Message<T>> handler;
    private boolean registered;

    public MessageConsumer<T> handler(Handler<Message<T>> handler) {
        this.handler = handler;
        registered = true;
        return this;
    }


    public void unregister() {
        this.handler = null;
        registered = false;
    }

    public Handler<Message<T>> getHandler() {
        return handler;
    }

    void unregister(Handler<AsyncResult<Void>> completionHandler) {
        unregister();
        AsyncResult<Void> result = new it.filippetti.sp.android.bus.io.impl.AsyncResult();
        completionHandler.handle(result);
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public String getAddress() {
        return address;
    }

    public String address() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
