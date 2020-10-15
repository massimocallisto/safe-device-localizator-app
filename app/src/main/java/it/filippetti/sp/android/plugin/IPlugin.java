package it.filippetti.sp.android.plugin;

import org.json.JSONObject;

public interface IPlugin {
    JSONObject configure(JSONObject config);
    void start() throws Exception;
    void stop() throws Exception;
}
