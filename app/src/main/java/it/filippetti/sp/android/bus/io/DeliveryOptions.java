package it.filippetti.sp.android.bus.io;

import java.util.LinkedHashMap;
import java.util.Map;

public class DeliveryOptions {
    private long sendTimeout = 30000;
    private Map<String, String> headers = new LinkedHashMap();

    public void setSendTimeout(long sendTimeout) {
        this.sendTimeout = sendTimeout;
    }

    public void addHeader(String key, String val) {
        headers.put(key, val);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
