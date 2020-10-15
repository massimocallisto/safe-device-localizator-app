package it.filippetti.sp.android.driver;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.usb.UsbManager;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.app.NotificationCompat;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import it.filippetti.safe.localizator.App;
import it.filippetti.safe.localizator.ServiceResultReceiver;
import it.filippetti.safe.localizator.mqtt.MQTTService;
import it.filippetti.safe.localizator.mqtt.MqttHelper;
import it.filippetti.sp.android.bus.io.AsyncResult;
import it.filippetti.sp.android.bus.io.DeliveryOptions;
import it.filippetti.sp.android.bus.io.EventBus;
import it.filippetti.sp.android.bus.io.Future;
import it.filippetti.sp.android.bus.io.Message;
import it.filippetti.sp.android.bus.io.MessageConsumer;
import it.filippetti.sp.android.plugin.AbstractPlugin;
import it.filippetti.sp.android.plugin.IPlugin;
import it.filippetti.sp.android.plugin.USBPlugin;
import it.filippetti.sp.android.util.BusUtil;
import it.filippetti.sp.android.util.IAddressConstants;
import it.filippetti.sp.android.util.JsonObject;
/*import it.filippetti.sp.android.plugin.BLEPlugin;
import it.filippetti.sp.android.plugin.EnvironmentalPlugin;
import it.filippetti.sp.android.plugin.GPSPlugin;
import it.filippetti.sp.android.plugin.NFCPlugin;
import it.filippetti.sp.android.plugin.OrientationPlugin;
import it.filippetti.sp.android.plugin.ProximityPlugin;
import it.filippetti.sp.android.plugin.WAMPBusPlugin;*/
import it.filippetti.sp.android.util.EventSerializer;


import it.filippetti.sp.android.util.SnapshotUtil;
import it.filippetti.sp.snapshot.Channel;
import it.filippetti.sp.snapshot.Snapshot;


/*
 * service lifecycle started by startService():<br/>
 * onCreate()<br/>
 * onStart()<br/>
 * onDestroy()<br/>
 */
/*
 * service lifecycle created by bindService:<br/>
 * onCreate()<br/>
 * onBind()<br/>
 * onUnbind() (-> onRebind())<br/>
 * onDestroy()<br/>
 */


/* https://medium.com/@ankit_aggarwal/ways-to-communicate-between-activity-and-service-6a8f07275297 */
public class SPDriverService extends JobIntentService implements IAddressConstants {
    private String TAG = "AAA_" + SPDriverService.class.getSimpleName();
    public static final String START_SERVICE = "action.START_SERVICE";
    static final int DOWNLOAD_JOB_ID = 1002;

    /* */
    public static final int MSG_SERVICE_STARTED = 1;
    public static final int MSG_SERVICE_STOPPED = -1;


    /* */
    private EventBus eventBus = EventBus.eventBus();
    private List<MessageConsumer> consumerCollection = new ArrayList<>();

    /**/
    private String BUS = null;
    private String TENANT = null;

    /**/
    private String endpoint = null; // "wss://dev.smartplatform.io/wamp.2.json";
    private String realm = null; // "test.it";

    /* plugin */
    private USBPlugin usbPlugin = new USBPlugin();
    /*private WAMPBusPlugin wampBusPlugin = new WAMPBusPlugin();
    private NFCPlugin nfcPlugin = new NFCPlugin();
    private BLEPlugin blePlugin = new BLEPlugin();
    private GPSPlugin gpsPlugin = new GPSPlugin();
    private OrientationPlugin orientationPlugin = new OrientationPlugin();
    private EnvironmentalPlugin environmentalPlugin = new EnvironmentalPlugin();
    private ProximityPlugin proximityPlugin = new ProximityPlugin();*/

    /**/
//    private boolean enableGPS = false;
//    private boolean enableNFC = false;
//    private boolean enableOrientation = false;
//    private boolean enableBLE = false;
//    private boolean enableEnvironmental = false;
    /* */
    private String serial;

    /* */
    private String pid = "";
    private String uri = "";

    /**/

    /**/
    public static boolean started = false;

    // run on another Thread to avoid crash
    private Handler mHandler = new Handler();
   // private DriverManager driverManager;
    private Snapshot snapshot = null;
    private long sequenceNumber = 0;

    // This is the object that receives interactions from clients.
    private final WAMPServiceBinder wampBinder = new WAMPServiceBinder();

    /**/
    private static final String CHANNEL_ID = SPDriverService.class.getName();

    /**/
    private Set<IPlugin> pluginCollection = new LinkedHashSet<IPlugin>();

    public class WAMPServiceBinder extends Binder {
        public SPDriverService getService() {
            return SPDriverService.this;
        }

        public boolean pluginEnable(boolean enable, Class pluginClass) throws Exception {
            Log.d(TAG, "enabling " + enable + " plugin " + pluginClass);

            boolean running = false;
            for (IPlugin plugin : pluginCollection) {
                if (pluginClass.getName().equals(plugin.getClass().getName())) {
                    AbstractPlugin abstractPlugin = (AbstractPlugin) plugin;
                    running = abstractPlugin.toggle(enable);
                    break;
                }
            }

            return running;
        }

        public boolean isPluginRunning(Class pluginClass) throws Exception {
            Log.d(TAG, "enable " + pluginClass);

            boolean running = false;
            for (IPlugin plugin : pluginCollection) {
                if (pluginClass.getName().equals(plugin.getClass().getName())) {
                    AbstractPlugin abstractPlugin = (AbstractPlugin) plugin;
                    running = abstractPlugin.isRunning();
                    break;
                }
            }

            return running;
        }


    }

    public static final String RECEIVER = "spdriver_receiver";

    public static void enqueueNewWork(Context context, ServiceResultReceiver workerResultReceiver, String action) {
        Intent intent = new Intent(context, MQTTService.class);
        intent.putExtra(RECEIVER, workerResultReceiver);
        intent.setAction(action);
        enqueueWork(context, SPDriverService.class, DOWNLOAD_JOB_ID, intent);
    }

    /*@Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return wampBinder;
    }
*/

    //@Override
    public void _onCreate() {
        Log.d(TAG, "onCreate");

        super.onCreate();

//
//        /**/
//        {
//            it.filippetti.sp.android.bus.io.Handler<Message<JSONObject>> handler = new it.filippetti.sp.android.bus.io.Handler<Message<JSONObject>>() {
//                @Override
//                public void handle(Message<JSONObject> result) {
//                    Log.d(TAG, "*** " + result.address() + " | " + result.toString());
//                }
//            };
//            String busAddress = BusUtil.busAddress(ADDRESS_SERVICE_GLOBAL_CONNECTION, BUS);
//            MessageConsumer<JSONObject> messageConsumer = eventBus.consumer(busAddress, handler);
//            Log.d(TAG, "registered consumer on address " + messageConsumer.address());
//            consumerCollection.add(messageConsumer);
//
//        }
//
//        /**/
//        {
//            it.filippetti.sp.android.bus.io.Handler<Message<JSONObject>> handler = new it.filippetti.sp.android.bus.io.Handler<Message<JSONObject>>() {
//                @Override
//                public void handle(Message<JSONObject> result) {
//                    Log.d(TAG, "*** " + result.address() + " | " + result.toString());
//                }
//            };
//            String busAddress = BusUtil.busAddress(ADDRESS_SERVICE_GLOBAL_DISCONNECTION, BUS);
//            MessageConsumer<JSONObject> messageConsumer = eventBus.consumer(busAddress, handler);
//            Log.d(TAG, "registered consumer on address " + messageConsumer.address());
//            consumerCollection.add(messageConsumer);
//
//        }
//
//        {
//            it.filippetti.sp.android.bus.io.Handler<Message<JSONObject>> handler = new it.filippetti.sp.android.bus.io.Handler<Message<JSONObject>>() {
//                @Override
//                public void handle(Message<JSONObject> result) {
//                    /**/
//                    int color = 0xff00ff00;
//                    updateNotification(SPDriverService.class.getSimpleName() + " joined " + realm, 0xff00ff00);
//
//                    try {
//                        if (driverManager != null) {
//                           // driverAction(driverManager);
//
//                        }
//                    } catch (Exception e) {
//                        Log.e(TAG, e.getMessage());
//                        e.printStackTrace();
//                    }
//                }
//            };
//            String busAddress = BusUtil.busAddress(ADDRESS_SERVICE_GLOBAL_JOIN, BUS);
//            MessageConsumer<JSONObject> messageConsumer = eventBus.consumer(busAddress, handler);
//            Log.d(TAG, "registered consumer on address " + messageConsumer.address());
//            consumerCollection.add(messageConsumer);
//
//        }
//
//        {
//            it.filippetti.sp.android.bus.io.Handler<Message<JSONObject>> handler = new it.filippetti.sp.android.bus.io.Handler<Message<JSONObject>>() {
//                @Override
//                public void handle(Message<JSONObject> result) {
//                    /**/
//                    int color = 0xff00ff00;
//                    updateNotification(SPDriverService.class.getSimpleName() + " leaved " + realm, 0xff00ff00);
//                }
//            };
//            String busAddress = BusUtil.busAddress(ADDRESS_SERVICE_GLOBAL_LEAVE, BUS);
//            MessageConsumer<JSONObject> messageConsumer = eventBus.consumer(busAddress, handler);
//            Log.d(TAG, "registered consumer on address " + messageConsumer.address());
//            consumerCollection.add(messageConsumer);
//
//        }

        /* */
//        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
//            try {
//                final String deviceId = telephonyManager.getDeviceId();
//                //serial.setText(deviceId);
//                serial = deviceId;
//
//                /* initialize snapshot */
//                snapshot = buildSnapshot();
//
//
//                /**/
//                String bus = BUS;
//                String type = "android";
//                String instance = deviceId;
//                DriverManager _driverManager = new DriverManager(bus, type, instance);
//                _driverManager.setIdentifier("sp.driver:" + type + ":" + instance);
//                driverManager = _driverManager;
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }


        /* register plugin */
        pluginCollection.add(usbPlugin);
        /*pluginCollection.add(wampBusPlugin);
        pluginCollection.add(nfcPlugin);
        pluginCollection.add(blePlugin);
        pluginCollection.add(gpsPlugin);
        pluginCollection.add(orientationPlugin);
        pluginCollection.add(proximityPlugin);
        pluginCollection.add(environmentalPlugin);*/

        // EventBus.getDefault().register(this);
        /* usb */
        try {
            UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
            usbPlugin.setUsbManager(usbManager);
            usbPlugin.BUS = BUS;
            usbPlugin.TENANT = TENANT;
//                usbPlugin.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
//
//        /* --- GPS --- */
//        try {
//            {
//                it.filippetti.sp.android.bus.io.Handler<Message<JSONObject>> handler = new it.filippetti.sp.android.bus.io.Handler<Message<JSONObject>>() {
//                    @Override
//                    public void handle(Message<JSONObject> result) {
//                        Log.d(TAG, "*** " + result.address() + " | " + result.toString());
//
//                        try {
//                            snapshot = buildSnapshot();
//
//                            Snapshot otherSnapshot = EventSerializer.fromJson(result.body());
//
//                            {
//                                String key = "latitude";
//                                SnapshotUtil.mergeChannel(key, otherSnapshot, snapshot);
//                            }
//                            {
//                                String key = "longitude";
//                                SnapshotUtil.mergeChannel(key, otherSnapshot, snapshot);
//                            }
//                            {
//                                String key = "elevation";
//                                SnapshotUtil.mergeChannel(key, otherSnapshot, snapshot);
//                            }
//
//                            if (!snapshot.getChannelCollection().isEmpty()) {
//                                publishSnapshot(snapshot);
//                            }
//
//
//                        } catch (Exception e) {
//                            Log.e(TAG, e.getMessage());
//                            e.printStackTrace();
//                        }
//                    }
//
//                };
//                String address = ADDRESS_SNAPSHOT_GPS;
//                MessageConsumer<JSONObject> messageConsumer = eventBus.consumer(address, handler);
//                Log.d(TAG, "registered consumer on address " + messageConsumer.address());
//                consumerCollection.add(messageConsumer);
//
//            }
////            if (enableGPS) {
////                gpsPlugin.start();
////            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        /* --- NFC --- */
//        try {
//            it.filippetti.sp.android.bus.io.Handler<Message<JSONObject>> handler = new it.filippetti.sp.android.bus.io.Handler<Message<JSONObject>>() {
//                @Override
//                public void handle(Message<JSONObject> result) {
//                    Log.d(TAG, "*** " + result.address() + " | " + result.toString());
//
//                    try {
//                        snapshot = buildSnapshot();
//
//                        JSONObject jbody = result.body();
//
//                        String tag = jbody.getString("tag");
//                        long tms = System.currentTimeMillis();
//                        String ref = snapshot.getReference().toString();
//
//                        {
//                            SnapshotUtil.removeChannel(snapshot, "rfid");
//
//                            int val = (tag.length() / 2);
//                            /**/
//                            Channel ch = new Channel();
//                            ch.setKey("rfid");
//                            ch.setValue(val);
//                            ch.setTimestamp(tms);
//                            ch.setSource(URI.create(ref));
//                            ch.putTarget(URI.create("sp:rfid:" + tag));
//                            snapshot.putChannel(ch);
//                        }
//
//                        if (snapshot.getChannelCollection().size() > 0) {
//                            publishSnapshot(snapshot);
//                        }
//
//                    } catch (Exception e) {
//                        Log.e(TAG, e.getMessage());
//                        e.printStackTrace();
//                    }
//                }
//
//            };
//            String address = ADDRESS_SENSOR_NFC;
//            MessageConsumer messageConsumer = eventBus.consumer(address, handler);
//            Log.d(TAG, "registered consumer on address " + messageConsumer.address());
//            consumerCollection.add(messageConsumer);
//
////            if (enableNFC) {
////                nfcPlugin.start();
////            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        /* --- BLE --- */
//        // Initializes Bluetooth adapter.
//        try {
//
//            {
//                it.filippetti.sp.android.bus.io.Handler<Message<JSONObject>> handler = new it.filippetti.sp.android.bus.io.Handler<Message<JSONObject>>() {
//                    @Override
//                    public void handle(Message<JSONObject> result) {
//                        Log.d(TAG, "*** " + result.address() + " | " + result.toString());
//
//                        try {
//                            snapshot = buildSnapshot();
//
//                            Snapshot otherSnapshot = EventSerializer.fromJson(result.body());
//
//                            {
//                                String key = "rssi";
//                                snapshot = SnapshotUtil.removeChannel(snapshot, key);
//                                snapshot = SnapshotUtil.appendChannel(key, otherSnapshot, snapshot);
//                            }
//
//                            if (!snapshot.getChannelCollection().isEmpty()) {
//                                publishSnapshot(snapshot);
//                            }
//
//
//                        } catch (Exception e) {
//                            Log.e(TAG, e.getMessage());
//                            e.printStackTrace();
//                        }
//                    }
//
//                };
//                String address = ADDRESS_SNAPSHOT_BLE;
//                MessageConsumer<JSONObject> messageConsumer = eventBus.consumer(address, handler);
//                Log.d(TAG, "registered consumer on address " + messageConsumer.address());
//                consumerCollection.add(messageConsumer);
//
//            }
//
//            final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//            blePlugin.setBluetoothAdapter(bluetoothManager.getAdapter());
////            if (enableBLE) {
////                blePlugin.start();
////            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        /* --- GPS ---*/
//        try {
//            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//            gpsPlugin.setLocationManager(locationManager);
////            if (enableGPS) {
////                gpsPlugin.start();
////            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//
//        /* --- sensor --- */
//        {
//            SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//            List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
//            for (Sensor sensor : deviceSensors) {
//                Log.d(TAG, "device sensor " + sensor.getName() + " | " + sensor.getType());
//            }
//        }
//        try {
//
//            {
//                it.filippetti.sp.android.bus.io.Handler<Message<JSONObject>> handler = new it.filippetti.sp.android.bus.io.Handler<Message<JSONObject>>() {
//                    @Override
//                    public void handle(Message<JSONObject> result) {
//                        Log.d(TAG, "*** " + result.address() + " | " + result.toString());
//
//                        try {
//                            snapshot = buildSnapshot();
//
//                            Snapshot otherSnapshot = EventSerializer.fromJson(result.body());
//
//                            {
//                                String key = "illuminance";
//                                SnapshotUtil.mergeChannel(key, otherSnapshot, snapshot);
//                            }
//
//
//                            if (!snapshot.getChannelCollection().isEmpty()) {
//                                publishSnapshot(snapshot);
//                            }
//                        } catch (Exception e) {
//                            Log.e(TAG, e.getMessage());
//                            e.printStackTrace();
//                        }
//                    }
//
//                };
//                String address = ADDRESS_SNAPSHOT_ENVIRONMENTAL;
//                MessageConsumer<JSONObject> messageConsumer = eventBus.consumer(address, handler);
//                Log.d(TAG, "registered consumer on address " + messageConsumer.address());
//                consumerCollection.add(messageConsumer);
//
//            }
//
//            SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//            environmentalPlugin.setSensorManager(sensorManager);
////            if (enableEnvironmental) {
////                environmentalPlugin.start();
////            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        try {
//            {
//                it.filippetti.sp.android.bus.io.Handler<Message<JSONObject>> handler = new it.filippetti.sp.android.bus.io.Handler<Message<JSONObject>>() {
//                    @Override
//                    public void handle(Message<JSONObject> result) {
//                        Log.d(TAG, "*** " + result.address() + " | " + result.body().toString());
//
//                        try {
//                            snapshot = buildSnapshot();
//
//                            Snapshot otherSnapshot = EventSerializer.fromJson(result.body());
//
//                            {
//                                String key = "horizontal";
//                                SnapshotUtil.mergeChannel(key, otherSnapshot, snapshot);
//                            }
//                            {
//                                String key = "orientation";
//                                SnapshotUtil.mergeChannel(key, otherSnapshot, snapshot);
//                            }
//
//
//                            if (!snapshot.getChannelCollection().isEmpty()) {
//                                publishSnapshot(snapshot);
//                            }
//                        } catch (Exception e) {
//                            Log.e(TAG, e.getMessage());
//                            e.printStackTrace();
//                        }
//                    }
//
//                };
//                String address = ADDRESS_SNAPSHOT_ORIENTATION;
//                MessageConsumer<JSONObject> messageConsumer = eventBus.consumer(address, handler);
//                Log.d(TAG, "registered consumer on address " + messageConsumer.address());
//                consumerCollection.add(messageConsumer);
//
//            }
//
//            SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//            orientationPlugin.setSensorManager(sensorManager);
////            if (enableOrientation) {
////                orientationPlugin.start();
////            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        try {
//            {
//                it.filippetti.sp.android.bus.io.Handler<Message<JSONObject>> handler = new it.filippetti.sp.android.bus.io.Handler<Message<JSONObject>>() {
//                    @Override
//                    public void handle(Message<JSONObject> result) {
//                        Log.d(TAG, "*** " + result.address() + " | " + result.body().toString());
//
//                        try {
//                            Snapshot otherSnapshot = EventSerializer.fromJson(result.body());
//
//                            {
//                                String key = "presence";
//                                SnapshotUtil.mergeChannel(key, otherSnapshot, snapshot);
//                            }
//
//                            if (!snapshot.getChannelCollection().isEmpty()) {
//                                publishSnapshot(snapshot);
//                            }
//                        } catch (Exception e) {
//                            Log.e(TAG, e.getMessage());
//                            e.printStackTrace();
//                        }
//                    }
//
//                };
//                String address = ADDRESS_SNAPSHOT_PROXIMITY;
//                MessageConsumer<JSONObject> messageConsumer = eventBus.consumer(address, handler);
//                Log.d(TAG, "registered consumer on address " + messageConsumer.address());
//                consumerCollection.add(messageConsumer);
//
//            }
//
//            SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//            proximityPlugin.setSensorManager(sensorManager);
////            if (enableOrientation) {
////                orientationPlugin.start();
////            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        /**/
        int color = 0xff0000ff;
        updateNotification(SPDriverService.class.getSimpleName() + " started", color);

        Log.d(TAG, this.getClass().getName() + " started" + " with serial " + serial);
    }
//
//    private void driverAction(final DriverManager driverManager) throws Exception {
//        String type = driverManager.getType();
//        String instance = driverManager.getInstance();
//        Future<JsonObject> future = Future.future();
//        future.setHandler(new it.filippetti.sp.android.bus.io.Handler<AsyncResult<JsonObject>>() {
//            @Override
//            public void handle(AsyncResult<JsonObject> result) {
//                try {
//                    Log.d(TAG, "... registering driver succeeded " + result.succeeded() + " error " + result.cause());
//
//                    if (result.succeeded()) {
//                        JsonObject jresult = result.result();
//                        JSONObject data = jresult.getJSONObject("data");
//                        pid = data.getString("pid");
//                        uri = data.getString("uri");
//                        Log.d(TAG, "PID is " + pid);
//
//                        Future fregpathout = Future.future();
//                        String pathOut = BusUtil.outputPath(realm, serial);
//                        String adt = "snapshot";
//
//                        driverManager.registerPathOut(fregpathout, pid, uri, pathOut, adt, null);
//
//                        Future fregpathin = Future.future();
//                        String pathIn = BusUtil.inputPath(realm, serial);
//
//                        driverManager.registerPathIn(fregpathin, pid, uri, pathIn, adt, null);
//
//                        Future<JsonObject> fregdevice = Future.future();
//
//                        driverManager.registerDevice(fregdevice, pid, uri, "android", serial);
//
//                        fregdevice.setHandler(new it.filippetti.sp.android.bus.io.Handler<AsyncResult<JsonObject>>() {
//                            @Override
//                            public void handle(AsyncResult<JsonObject> result) {
//                                try {
//                                    Future fregchannel = Future.future();
//                                    JSONObject deviceData = result.result().getJSONObject("data");
//                                    driverManager.registerChannel(fregchannel, pid, uri, deviceData.getString("uri"), "illuminance");
//                                    driverManager.registerChannel(fregchannel, pid, uri, deviceData.getString("uri"), "rssi");
//                                    driverManager.registerChannel(fregchannel, pid, uri, deviceData.getString("uri"), "latitude");
//                                    driverManager.registerChannel(fregchannel, pid, uri, deviceData.getString("uri"), "longitude");
//                                    driverManager.registerChannel(fregchannel, pid, uri, deviceData.getString("uri"), "elevation");
//                                } catch (Exception e) {
//                                    Log.e(TAG, e.getMessage());
//                                }
//                            }
//                        });
//
//                    } else {
//                        Log.e(TAG, "error: " + result.cause().getMessage());
//                    }
//                } catch (Exception e) {
//                    Log.e(TAG, e.getMessage());
//                }
//            }
//        });
//        Log.d(TAG, "registering driver " + driverManager.getType() + " " + driverManager.getInstance() + " ... ");
//        driverManager.registerDriver(future, type, instance, null);
//    }


    /**/
    private void publishSnapshot(Snapshot snapshot) throws Exception {
        JSONObject jsnapshot = EventSerializer.toJSONObject(snapshot);
        publishSnapshot(jsnapshot);
    }

    /**/
    private void publishSnapshot(final JSONObject jsnapshot) throws Exception {

        final String topic = BusUtil.outputPath(realm, serial);

        List args = new ArrayList<>();
        args.add(jsnapshot.toString());

        Log.d(TAG, "publishing on topic " + topic + " | " + jsnapshot.toString(3));


        String address = ADDRESS_SERVICE_GLOBAL_PUBLISH;
        JSONObject jbody = jsnapshot;
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.addHeader("topic", topic);
        eventBus.send(address, jbody, deliveryOptions, new it.filippetti.sp.android.bus.io.Handler<AsyncResult<Message<Object>>>() {
            @Override
            public void handle(AsyncResult<Message<Object>> result) {
                Log.d(TAG, "... publishing on topic " + topic + " | succeeded " + result.succeeded() + " | error " + result.cause());

            }
        });
    }

    /**/
    private Snapshot buildSnapshot() throws Exception {
        String refserial = serial;
        String ref = "sp:android:" + refserial;
        String type = "android";

        //OffsetDateTime tz = OffsetDateTime.now();
        //long tms = tz.toInstant().toEpochMilli();
        long tms = System.currentTimeMillis();
        if (this.snapshot == null) {
            snapshot = new Snapshot();
        }
        snapshot.setReference(URI.create(ref));
        snapshot.setReferenceType(type);
        snapshot.setTimestamp(tms);
        //jsnapshot.put("tz", tz.toString());
        snapshot.setUuid(UUID.randomUUID());
        snapshot.setSequenceNumber(sequenceNumber++);
        return snapshot;
    }


    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "onTaskRemoved");
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        //Toast.makeText(this, "service onDestroy", Toast.LENGTH_LONG).show();
        started = false;

        /* */
        for (IPlugin plugin : pluginCollection) {
            try {
                plugin.stop();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
        }

        for (MessageConsumer consumer : consumerCollection) {
            consumer.unregister();
        }

//        EventBus.getDefault().unregister(this);
        JSONObject jo = new JSONObject();
        eventBus.publish(ADDRESS_SP_DRIVER_STOP, jo.toString());
        eventBus.close();

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(0);

        super.onDestroy();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        // TODO
        System.out.println("__");
        this._onCreate();
        Log.d(TAG, "onHandleWork() called with: intent = [" + intent + "]");
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case START_SERVICE:
                    if(!SPDriverService.started){
                        try {
                            _onCreate();
                            ResultReceiver receiver = intent.getParcelableExtra(RECEIVER);
                            usbPlugin.setReceiver(receiver);
                            wampBinder.pluginEnable(true, USBPlugin.class);
                            started = true;
                            Log.i(TAG, "service is starting");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        Log.d(TAG, "onStartCommand " + flags + " " + startId);
        int result = super.onStartCommand(intent, flags, startId);

        if (!started) {
            started = true;
            Log.i(TAG, "service is starting");

            endpoint = intent.getStringExtra("endpoint");
            realm = intent.getStringExtra("realm");
            Log.d(TAG, "starting to" + " | endpoint " + endpoint + " | realm " + realm);
            TENANT = realm;

//            /* wamp */
//            try {
//                wampBusPlugin.setEndpoint(endpoint);
//                wampBusPlugin.setRealm(realm);
//                wampBusPlugin.start();
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

            /* usb */
            try {
                usbPlugin.BUS = BUS;
                usbPlugin.TENANT = TENANT;
               // usbPlugin.start();
            } catch (Exception e) {
                e.printStackTrace();
            }

            /**/

            //EventBus.getDefault().register(this);
            JSONObject jo = new JSONObject();
            eventBus.publish(ADDRESS_SP_DRIVER_START, jo.toString());


        } else {
            Log.e(TAG, "service is yet started");
        }


        return result;
    }

    /* */
    private void updateNotification(String content, int color) {
//        try {
//            if (started) {
//                //int color = getResources().getColor(R.color.my_notif_color);
//                //int color = ContextCompat.getColor(SPDriverService.this, R.color.my_notif_color);
//
//                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
//
//                Intent notificationIntent = new Intent(SPDriverService.this, SPDriverActivity.class);
//                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                PendingIntent contentIntent = PendingIntent.getActivity(SPDriverService.this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
////            Intent notificationIntent = new Intent(SPDriverService.this, SPDriverService.class);
////            PendingIntent contentIntent = PendingIntent.getActivity(SPDriverService.this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//                /**/
//                NotificationCompat.Builder builder = new NotificationCompat.Builder(SPDriverService.this, CHANNEL_ID)
//                        //NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
//                        .setSmallIcon(R.mipmap.ic_launcher)
//                        .setColor(color)
//                        .setContentTitle(SPDriverService.class.getSimpleName())
//                        .setContentText(content)
//                        //.setOngoing(true)
//                        .setContentIntent(contentIntent)
//                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//                // notificationID allows you to update the notification later on.
//                int notificationId = 0;
//                Notification notification = builder.build();
//                mNotificationManager.notify(notificationId, notification);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }


//    @Override
//    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//        Log.d(TAG, "onAccuracyChanged " + String.valueOf(sensor.getType()));
//
//    }

}
