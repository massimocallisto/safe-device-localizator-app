package it.filippetti.sp.android.util;

import java.util.Iterator;

import it.filippetti.sp.snapshot.IChannel;
import it.filippetti.sp.snapshot.Snapshot;

public class SnapshotUtil {
    private final static String TAG = "AAA_" + SnapshotUtil.class.getSimpleName();


    public static IChannel getChannel(Snapshot snapshot, String channelKey, String source, String target) {
        IChannel channel = null;
        Iterator<IChannel> channelIterator = snapshot.getChannelCollection().iterator();
        while (channelIterator.hasNext()) {
            IChannel temp = channelIterator.next();

            boolean check = channelKey.equalsIgnoreCase(temp.getKey());
            check &= source != null && temp.getSource() != null ? source.equalsIgnoreCase(temp.getSource().toString()) :  (source == null && temp.getSource() != null) || (source != null && temp.getSource() == null) ? false : true;
            check &= target != null && temp.getTarget() != null ? target.equalsIgnoreCase(temp.getTarget().toString()) :  (target == null && temp.getTarget() != null) || (target != null && temp.getTarget() == null) ? false : true;
            if (check) {
                channel = temp;
            }
        }
        return channel;
    }

    public static Snapshot removeChannel(Snapshot snapshot, String channelKey) {
        Iterator<IChannel> channelIterator = snapshot.getChannelCollection().iterator();
        while (channelIterator.hasNext()) {
            IChannel channel = channelIterator.next();
            if (channelKey.equalsIgnoreCase(channel.getKey())) {
                // Log.d(TAG, "removing " + channelKey + " | " + channel);
                channelIterator.remove();
            }
        }
        return snapshot;
    }


    public static void mergeChannel(String key, Snapshot other, Snapshot target) {
        IChannel channel = SnapshotUtil.getChannel(other, key, null, null);

        if (channel != null) {
            SnapshotUtil.removeChannel(target, key);
            if(channel.getSource() != null) {
                channel.setSource(target.getReference());
            }
            target.putChannel(channel);
        }
    }


    public static Snapshot appendChannel(String key, Snapshot other, Snapshot destination) {
        IChannel channel = null;
        Iterator<IChannel> channelIterator = other.getChannelCollection().iterator();
        while (channelIterator.hasNext()) {
            IChannel temp = channelIterator.next();

            destination.putChannel(temp);
        }
        return destination;
    }

//    private void removeChannel(JSONObject jsnapshot, String channel) throws Exception {
//        JSONArray channels = jsnapshot.optJSONArray("m");
//        Log.d(TAG, "checking channels " + channels);
//        if (channels != null) {
//            Log.d(TAG, "checking channels #" + channels.length());
//            for (int i = 0; i < channels.length(); i++) {
//                try {
//                    JSONObject ch = channels.getJSONObject(i);
//                    Log.d(TAG, "channel " + channel + " vs " + ch.get("k"));
//                    if (channel.equals(ch.getString("k"))) {
//                        channels.remove(i);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

//    private void removeChannel(JSONObject jsnapshot, String channel, String source, String target) throws JSONException {
//        JSONArray channels = jsnapshot.optJSONArray("m");
//        if (channels != null) {
//            for (int i = 0; i < channels.length(); i++) {
//                try {
//                    JSONObject ch = channels.getJSONObject(i);
//                    if (channel.equals(ch.get("k")) && source.equals(ch.get("s")) && target.equals(ch.get("d"))) {
//                        channels.remove(i);
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

}
