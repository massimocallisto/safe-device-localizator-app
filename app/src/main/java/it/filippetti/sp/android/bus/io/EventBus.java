package it.filippetti.sp.android.bus.io;

import android.util.Log;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EventBus {
    private String TAG = "AAA_" + EventBus.class.getSimpleName();

    private static org.greenrobot.eventbus.EventBus internalBus = org.greenrobot.eventbus.EventBus.getDefault();
    private static EventBus instance;
    private static boolean registered = false;

    private Map<String, List> consumerMap = new LinkedHashMap<String, List>();

    private EventBus() {

    }



    private static EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        if (!registered) {
            registered = true;
            internalBus.register(instance);
        }
        return instance;
    }

    public void close() {
        if (registered) {
            registered = false;
            internalBus.unregister(this);
        }
    }

//    public void close(Handler<AsyncResult<Void>> completionHandler) {
//        close();
//        AsyncResult<Void> result = new it.filippetti.sp.android.bus.io.impl.AsyncResult<Void>();
//        completionHandler.handle(result);
//    }

    public static EventBus getDefault() {

        return getInstance();
    }


    public static EventBus eventBus() {

        return getInstance();
    }

    public EventBus publish(String address, Object message) {
        return publish(address, message, null);
    }

    public EventBus publish(String address, Object message, DeliveryOptions options) {
        Message messageEvent = new Message();
        messageEvent.address = address;
        messageEvent.body = message;
        messageEvent.deliveryOptions = options;

        /**/
        internalBus.post(messageEvent);
        return this;
    }

    public <T> EventBus send(String address, Object message, DeliveryOptions options, Handler<AsyncResult<Message<T>>> replyHandler) {
        Message messageEvent = new Message();
        messageEvent.address = address;
        messageEvent.body = message;
        messageEvent.deliveryOptions = options;

        /**/
        messageEvent.handler = replyHandler;
        internalBus.post(messageEvent);

        // TODO... timeout??? run timer to check message state
        return this;
    }

    public <T> MessageConsumer<T> consumer(String address) {
        MessageConsumer<T> consumer = new MessageConsumer();
        consumer.setAddress(address);
        List list = consumerMap.get(address);
        if (list == null) {
            list = new ArrayList();

        }
        list.add(consumer);
        consumerMap.put(address, list);
        return consumer;
    }

    public <T> MessageConsumer<T> consumer(String address, Handler<Message<T>> handler) {
        MessageConsumer<T> consumer = consumer(address);
        consumer.handler(handler);
        return consumer;
    }

    /**/
//    public void register(Object object) {
//        internalBus.register(object);
//    }

    /**/
//    public void unregister(Object object) {
//        internalBus.unregister(object);
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(final Message event) throws Exception {
        /* Do something */
//        Log.d(TAG, "*** bus received on " + event.address + " | " + event.body + " | " + event.handler);

        List<MessageConsumer> list = consumerMap.get(event.address);
        if (list != null) {
            for (MessageConsumer consumer : list) {
                try {
//                    Log.d(TAG, "*** bus received on " + event.address + " | isRegistered " + consumer.isRegistered());
                    Handler handler = consumer.getHandler();
                    if (consumer.isRegistered() && handler != null) {
                        handler.handle(event);
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    e.printStackTrace();
                }
            }
        }

    }

    ;
}
