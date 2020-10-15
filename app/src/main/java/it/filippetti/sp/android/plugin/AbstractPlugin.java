package it.filippetti.sp.android.plugin;

import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.filippetti.sp.android.bus.io.EventBus;
import it.filippetti.sp.android.bus.io.MessageConsumer;

public abstract class AbstractPlugin implements IPlugin {
    private String TAG = "AAA_" + AbstractPlugin.class.getSimpleName();

    /**/
    public static final long DEFAULT_SEND_TIMEOUT = 5000;
    protected EventBus eventBus = EventBus.eventBus();
    protected List<MessageConsumer> consumerCollection = new ArrayList<>();
    protected boolean running;

    @Override
    public JSONObject configure(JSONObject config) {
        return config;
    }

    @Override
    public void start() throws Exception {
        running = true;
    }

    @Override
    public void stop() throws Exception {
        for (MessageConsumer consumer : consumerCollection) {
            try {
                consumer.unregister();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
        running = false;
    }

    public boolean toggle(boolean enable) throws Exception {
        if (enable) {
            if (running) {
                stop();
            }
            start();
        } else {
            stop();
        }

        return running;
    }

    public boolean isRunning() {
        return running;
    }
}
