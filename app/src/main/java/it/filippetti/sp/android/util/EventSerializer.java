package it.filippetti.sp.android.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import it.filippetti.sp.snapshot.Channel;
import it.filippetti.sp.snapshot.Event;
import it.filippetti.sp.snapshot.IChannel;
import it.filippetti.sp.snapshot.Snapshot;
import it.filippetti.sp.snapshot.TimestampEntry;

public class EventSerializer {
    private static NumberFormat nf = NumberFormat.getInstance();

    public static Snapshot fromJson(JSONObject js) throws Exception {

        Snapshot obj = null;
        Long t = js.optLong("t");
        String tz = js.optString("tz");
        if (t != null || tz != null) {
            obj = new Snapshot();
            /**/
            if (t != null) {
                obj.setTimestamp(t);
            }
            if (tz != "") {
                Date date = TimestampEntry.ISO_8601_FORMATTER.parse(tz);
                obj.setTimestamp(date.getTime());
            }

            // Map<String, Object> jmap = js.getMap();
            // for (Entry<String, Object> entry : jmap.entrySet()) {
            // String ek = entry.getKey();
            // Object ev = entry.getValue();
            //
            // }

            /**/
            String uuid = js.optString("uuid");
            if (uuid != "") {
                obj.setUuid(UUID.fromString(uuid));
            }
            /**/
            String cuid = js.optString("cuid");
            if (cuid != "") {
                obj.setCuid(UUID.fromString(cuid));
            }
            /**/
            String ref = js.optString("ref");
            if (ref != "") {
                obj.setReference(URI.create(ref));
            }
            /**/
            String type = js.optString("type");
            if (type != "") {
                obj.setReferenceType(type);
            }
            /**/
            String cat = js.optString("cat");
            if (cat != "") {
                obj.setCategory(cat);
            }
            /**/
            JSONObject meta = js.optJSONObject("meta");
            if (meta != null) {
                obj.setMeta(toMap(meta));
            }
            /**/
            String ctx = js.optString("ctx");
            if (ctx != "") {
                obj.setContext(ctx);
            }
            /**/
            Long sn = js.optLong("sn");
            if (sn != null) {
                obj.setSequenceNumber(sn);
            }
            /**/
            Long rn = js.optLong("rn");
            if (rn != null) {
                obj.setResponseNumber(rn);
            }
            /**/
            JSONArray jam = js.optJSONArray("m");
            if (jam != null) {
                for (int i = 0; i < jam.length(); i++) {
                    JSONObject jom = jam.getJSONObject(i);
                    Channel m = fromJsonChannel(jom);
                    obj.putChannel(m);
                }

            }

        }
        return obj;
    }

    private static Channel fromJsonChannel(JSONObject js) throws Exception {
        Channel obj = new Channel();
        /**/
        Long t = js.optLong("t");
        if (t != null) {
            obj.setTimestamp(t);
        }
        /**/
        String tz = js.optString("tz");
        if (tz != "") {
            Date date = TimestampEntry.ISO_8601_FORMATTER.parse(tz);
            obj.setTimestamp(date.getTime());
        }
        /**/
        String k = js.optString("k");
        if (k != "") {
            obj.setKey(k);
        }
        /**/
        String s = js.optString("s");
        if (s != "") {
            obj.setSource(URI.create(s));
        }
        /**/
        String d = js.optString("d");
        if (d != "") {
            obj.setTarget(URI.create(d));
        }
        // /**/
        // Object v = js.getValue("v");
        // if (v != null) {
        // Number num = nf.parse(v.toString());
        // obj.setValue(num);
        // }
        /**/
        Double v = js.optDouble("v");
        if (v != null) {
            obj.setValue(v);
            if ((v == Math.floor(v)) && !Double.isInfinite(v)) {
                obj.setValue(v.longValue());
            }
        }
        /**/
        String u = js.optString("u");
        if (u != "") {
            obj.setUnit(u);
        }
        /**/
        String x = js.optString("x");
        if (x != "") {
            obj.setExtension(x);
        }
        /**/
        String ref = js.optString("ref");
        if (ref != "") {
            obj.setMetadata(URI.create(ref));
        }
        /**/
        JSONObject jmeta = js.optJSONObject("meta");
        if (jmeta != null) {
            obj.setMeta(toMap(jmeta));
        }

        return obj;
    }

    public static JSONObject toJson(Object object) throws JSONException {
        JSONObject JSONObject = null;

        if (object instanceof Snapshot) {
            JSONObject = toJSONObject((Snapshot) object);
        } else if (object instanceof Event) {
            JSONObject = toJSONObject((Event) object);
        } else if (object instanceof Channel) {
            JSONObject = toJSONObject((Channel) object);
        }

        return JSONObject;

    }

    public static JSONObject toJSONObject(Event object) throws JSONException {
        JSONObject j = new JSONObject();
        /**/
        Long t = object.getTimestamp();
        if (t != null) {
            j.put("t", t);
        }
        /**/
        Calendar tz = object.getCalendar();
        if (tz != null) {
            Date time = object.getCalendar().getTime();

            int timezone = time.getTimezoneOffset();
            int h = timezone / 60;
            int m = timezone % 60;

            String symbol = h>=0 ? "+" : "-";

            String hour = String.format("%02d", Math.abs(h));
            String minute = String.format("%02d",m);

            j.put("tz", TimestampEntry.ISO_8601_FORMATTER.format(object.getCalendar().getTime())+symbol+hour+":"+minute);
        }

        /**/
        UUID uuid = object.getUuid();
        if (uuid != null) {
            j.put("uuid", uuid.toString());
        } /**/
        UUID cuid = object.getCuid();
        if (cuid != null) {
            j.put("cuid", cuid.toString());
        }

        // Map<String, Object> meta = object.getMeta();
        // if (meta != null && !meta.isEmpty()) {
        // j.put("meta", new JSONObject(meta));
        // }

        /**/
        LinkedHashSet<UUID> ruidSet = object.getRuid();
        if (ruidSet != null && !ruidSet.isEmpty()) {
            JSONArray ruid = new JSONArray();
            for (UUID u : ruidSet) {
                ruid.put(uuid.toString());
            }
            j.put("ruid", ruid);
        }
        return j;
    }

    public static JSONObject toJSONObject(Channel object) throws JSONException {
        JSONObject j = new JSONObject();
        /**/
        Long t = object.getTimestamp();
        if (t != null) {
            j.put("t", t);
        }
        /**/
        Calendar tz = object.getCalendar();
        if (tz != null) {

            Date time = object.getCalendar().getTime();
            int timezone = time.getTimezoneOffset();
            int h = timezone / 60;
            int m = timezone % 60;

            String symbol = h>=0 ? "+" : "-";

            String hour = String.format("%02d", Math.abs(h));
            String minute = String.format("%02d",m);

            j.put("tz", TimestampEntry.ISO_8601_FORMATTER.format(object.getCalendar().getTime())+symbol+hour+":"+minute);
        }
        /**/
        String k = object.getKey();
        if (k != null) {
            j.put("k", k);
        }
        /**/
        URI s = object.getSource();
        if (s != null) {
            j.put("s", s.toString());
        }
        URI d = object.getTarget();
        if (d != null) {
            j.put("d", d.toString());
        }
        /**/
        Number v = object.getValue();
        if (v != null) {
            j.put("v", v);
        }
        /**/
        String u = object.getUnit();
        if (u != null) {
            j.put("u", u);
        }
        /**/
        String x = object.getExtension();
        if (x != null) {
            j.put("x", x);
        }
        /**/
        URI ref = object.getMetadata();
        if (ref != null) {
            j.put("ref", ref.toString());
        }

        Map meta = object.getMeta();
        if (meta != null && !meta.isEmpty()) {
            j.put("meta", new JSONObject(meta));
        }
        return j;
    }

    public static JSONObject toJSONObject(Snapshot object) throws JSONException {
        JSONObject j = toJSONObject((Event) object);

        /**/
        URI ref = object.getReference();
        if (ref != null) {
            j.put("ref", ref.toString());
        }
        /**/
        String type = object.getReferenceType();
        if (type != null) {
            j.put("type", type);
        }
        /**/
        String cat = object.getCategory();
        if (cat != null) {
            j.put("cat", cat);
        }
        /**/
        String ctx = object.getContext();
        if (ctx != null) {
            j.put("ctx", ctx);
        }
        /**/
        Long sn = object.getSequenceNumber();
        if (sn != null) {
            j.put("sn", sn);
        }
        /**/
        Long rn = object.getResponseNumber();
        if (rn != null) {
            j.put("rn", rn);
        }
        /**/
        List<IChannel> mm = object.getChannelCollection();
        if (mm != null && !mm.isEmpty()) {
            JSONArray jm = new JSONArray();
            for (IChannel ch : mm) {
                Channel flow = (Channel) ch;
                JSONObject jflow = toJSONObject(flow);
                jm.put(jflow);
            }
            j.put("m", jm);
        }

        Map<String, Object> meta = object.getMeta();
        if (meta != null && !meta.isEmpty()) {
            j.put("meta", new JSONObject(meta));
        }

        // List<IFlowState> fm = snapshot.getFlowStateCollection();

        return j;
    }

    public static JSONArray toJSONArray(List list) {
        JSONArray ja = new JSONArray(list);
        return ja;
    }

    /* */
    public static List toList(JSONArray jargs) {
        List args = new ArrayList();
        for (int i = 0; i < jargs.length(); i++) {
            try {
                args.add(jargs.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return args;
    }

    public static JSONObject toJSONObject(Map map) {
        JSONObject ja = new JSONObject(map);
        return ja;
    }

    /* */
    public static Map toMap(JSONObject jargs) {
        Map argskw = new LinkedHashMap();
        Iterator<String> keys = jargs.keys();
        while (keys.hasNext()) {
            try {
                String k = keys.next();
                argskw.put(k, jargs.get(k));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return argskw;
    }
}
