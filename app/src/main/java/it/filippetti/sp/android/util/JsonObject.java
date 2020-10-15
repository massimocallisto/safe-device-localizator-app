package it.filippetti.sp.android.util;

import org.json.JSONObject;

public class JsonObject extends JSONObject {
    public JsonObject() {
        super();
    }

    public JsonObject(String body) throws Exception {
        super(body);
    }
}
