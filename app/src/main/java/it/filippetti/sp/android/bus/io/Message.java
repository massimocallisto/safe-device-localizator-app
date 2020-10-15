package it.filippetti.sp.android.bus.io;

import java.util.Map;
import java.util.UUID;

import it.filippetti.sp.android.bus.io.impl.AsyncResult;

public class Message<T> {
    protected String address;
    protected T body;
    //public Map<String, String> headers = new LinkedHashMap();
    public String replyAddress = UUID.randomUUID().toString();

    public DeliveryOptions deliveryOptions = new DeliveryOptions();

    //public boolean isSend;
    public Handler handler;

    public T body() {
        return body;
    }

    public Map<String, String> headers() {
        return deliveryOptions.getHeaders();
    }

    public String address() {
        return address;
    }

    boolean isSend() {
        return handler != null;
    }

    public void reply(Object reply) {
        if (handler != null) {
            Message message = new Message();
            message.address = replyAddress;
            message.body = reply;
            AsyncResult asyncResult = new AsyncResult<>();
            asyncResult.result = message;
            handler.handle(asyncResult);
        }
    }


    public void fail(int failureCode, String data) {
        Message message = new Message();
        message.address = replyAddress;

        AsyncResult asyncResult = new AsyncResult<>();
        asyncResult.result = message;
        // TODO... use ReplyException
        asyncResult.cause = new Exception(data);
        handler.handle(asyncResult);
    }
}
