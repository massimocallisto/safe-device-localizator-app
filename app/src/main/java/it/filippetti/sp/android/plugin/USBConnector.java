package it.filippetti.sp.android.plugin;

import android.hardware.usb.UsbDeviceConnection;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialPort;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.UUID;

import it.filippetti.sp.android.bus.io.AsyncResult;
import it.filippetti.sp.android.bus.io.DeliveryOptions;
import it.filippetti.sp.android.bus.io.EventBus;
import it.filippetti.sp.android.bus.io.Future;
import it.filippetti.sp.android.bus.io.Handler;
import it.filippetti.sp.android.bus.io.Message;
import it.filippetti.sp.android.bus.io.MessageConsumer;
import it.filippetti.sp.android.util.BusUtil;
import it.filippetti.sp.android.util.IJZConstants;
import it.filippetti.sp.android.util.JsonObject;
//import it.filippetti.sp.android.driver.SPDriverService;
import it.smartspace.common.io.TokenizerListener;
import it.smartspace.common.util.ConversionUtility;
import it.smartspace.jz.communication.ZFrame;
import it.smartspace.jz.communication.ZPacket;
import it.smartspace.jz.communication.command.CoordinatorPingCommand;
import it.smartspace.jz.communication.component.RawBlock;
import it.smartspace.jz.communication.component.RawValue;
import it.smartspace.jz.communication.interfaces.IFrame;
import it.smartspace.jz.communication.message.EndDeviceRawBlockMessage;
import it.smartspace.jz.communication.message.EndDeviceRawValuesMessage;
import it.smartspace.jz.communication.message.PirReadingMessage;
import it.smartspace.jz.communication.message.RawValuesMessage;
import it.smartspace.jz.communication.message.RouterEnveloperMessage;
import it.smartspace.jz.communication.message.ZEnvelopedMessage;
import it.smartspace.jz.communication.util.ProtocolUtil;
import it.smartspace.jz.communication.util.TokenParser;

import static it.filippetti.sp.android.util.IAddressConstants.ADDRESS_SERVICE_GLOBAL_CALL;
import static it.filippetti.sp.android.util.IAddressConstants.ADDRESS_SERVICE_GLOBAL_PUBLISH;
import static it.filippetti.sp.android.util.IAddressConstants.ADDRESS_SERVICE_GLOBAL_REGISTER;
import static it.filippetti.sp.android.util.IAddressConstants.ADDRESS_SERVICE_GLOBAL_UNREGISTER;

public class USBConnector extends AbstractPlugin implements IJZConstants {
    private final String TAG = "AAA_SPDriverServiceUSB";
    private final UsbDeviceConnection connection;
    private final UsbSerialPort usbPort;
    private static final long USB_TIMER_PERIOD = 1000;
    public static final int DEFAULT_BAUD_RATE = 57600;

    private Set<Long> endpointCollection = new LinkedHashSet();
    /* */
    //private List<MessageConsumer> consumerCollection = new ArrayList<MessageConsumer>();

    /**/
    private boolean SLAVE = true;
    /**/
    private Timer usbTimer = null;
    private USBTimerTask usbTimerTask = null;
    //private EventBus eventBus = EventBus.eventBus();

    /**/
    public String TENANT;
    public String BUS;
    public ResultReceiver RECEIVER;
    private boolean AUTO_DISCOVER = true;

    public USBConnector(UsbDeviceConnection connection, UsbSerialPort port) {
        this.connection = connection;
        this.usbPort = port;
    }

    @Override
    public void start() throws Exception {
        super.start();

        Log.d(TAG, "opening port " + usbPort);
        usbPort.open(connection);
        usbPort.setParameters(DEFAULT_BAUD_RATE, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
        usbPort.setDTR(false);
        usbPort.setRTS(false);

        /**/
        Handler<Message<JSONObject>> handler = new Handler<Message<JSONObject>>() {
            @Override
            public void handle(Message<JSONObject> request) {
                try {
                    Log.d(TAG, "received invocation on address " + request.address());
                    JSONObject jbody = request.body();
//                    String hex = jbody.optString("h");
//
//                    byte[] token = ConversionUtility.fromHexString(hex);

                    discover();

                    request.reply(jbody);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                    request.fail(HttpURLConnection.HTTP_INTERNAL_ERROR, e.getMessage());
                }
            }
        };
        final String address = BusUtil.busAddress(ADDRESS_ENDPOINT_DISCOVER, BUS);
        MessageConsumer messageConsumer = EventBus.eventBus().consumer(address, handler);
        Log.d(TAG, "registered consumer on address " + messageConsumer.address());
        consumerCollection.add(messageConsumer);

        /**/
        USBTimerTask _usbTimerTask = new USBTimerTask(usbPort);
        _usbTimerTask.getTokenizerInput().register(inputTokenizerListerner);
        _usbTimerTask.getTokenizerOutput().register(outputTokenizerListerner);
        usbTimerTask = _usbTimerTask;
        /* */
        if (usbTimer != null) {
            usbTimer.cancel();
        } else {
            usbTimer = new Timer();
        }
        usbTimer.scheduleAtFixedRate(_usbTimerTask, 0, USB_TIMER_PERIOD);

        /* */
        if (AUTO_DISCOVER) {
            try {
                Log.d(TAG, "pinging endpoint ... ");
                discover();
            } catch (Exception e) {
                Log.e(TAG, "error " + e.getMessage());
            }
        }
        Log.i(TAG, this.getClass().getSimpleName() + " started on " + usbPort);

    }
    /**/
//    private Catalog catalog = new Catalog();
//    private Factory factory = new Factory(catalog);
//    private SnapshotMessageEncoder messageEncoder = new SnapshotMessageEncoder(factory);
//    private SnapshotCommandDecoder commandDecoder = new SnapshotCommandDecoder(factory);

    //    SnapshotSerializer networkSnapshotSerializer = new SnapshotSerializer();
//
//    private void handleEntry(DeviceSnapshotEntry entry) throws Exception {
//        // TODO...
//        NetworkDevice device = entry.getDevice();
//        it.filippetti.jz.network.snapshot.Snapshot networkSnapshot = entry.getSnapshot();
//        String jnetworkSnapshot = networkSnapshotSerializer.toJson(networkSnapshot).toString();
//
//        Snapshot snasphot = EventSerializer.fromJson(new JSONObject(jnetworkSnapshot));
//        JSONObject jsnapshot = EventSerializer.toJSONObject(snasphot);
//        Log.d(TAG, "WOW " + jsnapshot.toString(3));
//    }

    /**/
    private TokenizerListener inputTokenizerListerner = new TokenizerListener() {
        @Override
        public void onToken(byte[] token, boolean error) {
            try {
                Log.d(TAG, "I = #" + token.length + " [B] : " + ConversionUtility.toHexString(token) + " | error " + error);
                if (!error) {
                    /* publish token */
                    publishToken(token);

                    /**/
                    handleToken(token);

                    /**/
                    sendParsedToken(token);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private TokenizerListener outputTokenizerListerner = new TokenizerListener() {
        @Override
        public void onToken(byte[] token, boolean error) {
            try {
                Log.d(TAG, "O = #" + token.length + " [B] : " + ConversionUtility.toHexString(token) + " | error " + error);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void sendParsedToken(byte[] token) throws Exception {
        IFrame frame = TokenParser.parse(token);
        long address = ProtocolUtil.resolveApplicationAddress(frame);
        long rssi = ProtocolUtil.resolveRssi(frame) - 220;
        short nt_address = (short)(address & 0x000000000000FFFF);
        String hex = ConversionUtility.getHex(nt_address);
        if(frame instanceof ZFrame){
            ZFrame z = (ZFrame) frame;
            ZPacket zPacket = z.getZPacket();
            if(zPacket instanceof RouterEnveloperMessage){
                RouterEnveloperMessage router = (RouterEnveloperMessage) zPacket;
                for(ZEnvelopedMessage m : router.getEnvelopedCollection()){

                    if(m instanceof EndDeviceRawValuesMessage){
                        EndDeviceRawValuesMessage raw1 = (EndDeviceRawValuesMessage) m;
                        short specificClusterID = raw1.getSpecificClusterID();
                        for(RawValue v : raw1.getRawCollection()){
                            v.getLabel();
                        }
                    }
                    if(m instanceof EndDeviceRawBlockMessage){
                        EndDeviceRawBlockMessage raw1 = (EndDeviceRawBlockMessage) m;
                        short specificClusterID = raw1.getSpecificClusterID();
                        for(RawBlock v : raw1.getRawCollection()){
                            v.getLabel();
                        }
                    }
                    if(m instanceof PirReadingMessage){

                    }

                }
            }

        }


        String endpoint = ConversionUtility.getHex(ProtocolUtil.getSourceEndpointAddress(token));


        // Inject receiver
        Bundle bundle = new Bundle();
        bundle.putString("message", "device");
        bundle.putLong("rssi", rssi);
        bundle.putString("address", hex);
        bundle.putString("topic", "topic");
        if(RECEIVER != null)
            RECEIVER.send(1, bundle);

       /* ZFrame zf = new ZFrame(token);
        zf.deserialize(zf.getToken());
        //System.out.println("T = " + ITimestampEntry.ISO_8601_FORMATTER.format( dateTime.toDate().getTime()));

        if(!zf.isFCSPassed()){
            Log.w(TAG, "Packet error due to not isFCSPassed");
        }else if(!zf.isWellFormed()){
            Log.w(TAG, "Packet error due to not isWellFormed");
        }else{
            /// TODO: ??
        }*/

    }
    private void handleToken(byte[] token) throws Exception {
        IFrame frame = TokenParser.parse(token);

        long endpointAddress = ProtocolUtil.getSourceEndpointAddress(token);
        /* attach endpoint */
        if (!ProtocolUtil.isEndpointReserved(endpointAddress) && !endpointCollection.contains(endpointAddress)) {
            endpointCollection.add(endpointAddress);
            Future<Void> future = Future.future();
            onEndpointAttach(future, endpointAddress);
        }

        Future future = Future.future();
        if (SLAVE) {
            // TODO: act as slave: delegate to master (path registered to receive data)
            delegateMaster(future, token, frame);
        } else {
            //
//                        Collection<DeviceSnapshotEntry> deviceSnapshotCollection = messageEncoder.parse(frame);
//                        for (DeviceSnapshotEntry entry : deviceSnapshotCollection) {
//                            handleEntry(future, entry);
//                        }
        }
    }

    /**/
//    private Handler<Message<Buffer>> commandConsumerHandler = new Handler<Message<Buffer>>() {
//
//        @Override
//        public void handle(Message<Buffer> request) {
//            boolean send = true;
//            Buffer body = request.body();
//            byte[] data = body.getBytes();
//            /* check send condition */
//            MultiMap h = request.headers();
//            if (!h.isEmpty()) {
//                /* check target connector */
//                String targetConnector = h.get("target");
//                if (targetConnector != null) {
//                    if (!connectorURI.equalsIgnoreCase(targetConnector)) {
//                        Logger.getRootLogger().error("Connector " + connectorURI + " received target " + targetConnector);
//                        send = false;
//                    }
//                }
//                String hex = h.get("hex");
//                if (hex != null && Boolean.valueOf(hex)) {
//                    data = ConversionUtility.fromHexString(body.toString());
//                }
//            }
//
//            /* */
//            if (send) {
//                try {
//                    SSTokenizer.SSTokenizerState state = outputTokenizer.process(data);
//                    if (!SSTokenizerState.WAITING_SOP_STATE.equals(state)) {
//                        vertx.cancelTimer(ssOutputTimeoutTimer);
//                        ssOutputTimeoutTimer = vertx.setTimer(tokenizerTimeout, ssfoutputTimeoutHandler);
//                    }
//
//                    /* */
//                    if (request.isSend() && data.length > 0) {
//                        boolean sopValid = SSTokenizer.sopValid(data);
//
//                        if (sopValid && SSTokenizerState.WAITING_SOP_STATE.equals(state)) {
//                            DeliveryOptions options = new DeliveryOptions();
//                            options.addHeader("connector", connectorURI);
//                            request.reply(body, options);
//                        }
//                        else {
//                            Exception e = new IllegalArgumentException();
//                            request.fail(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
//                        }
//                    }
//
//                }
//                catch (Exception e) {
//                    request.fail(ReplyFailure.RECIPIENT_FAILURE.toInt(), e.getMessage());
//                    Logger.getRootLogger().error(e);
//                }
//            }
//            else {
//                request.fail(ReplyFailure.RECIPIENT_FAILURE.toInt(), new IllegalArgumentException().getMessage());
//                Logger.getRootLogger().warn("Check send condition failed for " + connectorURI + "... ");
//            }
//        }
//    };

    protected void onEndpointAttach(Future<Void> future, Long endpointAddress) throws Exception {
        String hendpoint = ConversionUtility.getHex(endpointAddress, false);
        //String tz = onow.toString();

        final String address = BusUtil.endpoint(ADDRESS_JZ_DRIVER_MANAGE_COMMAND, hendpoint);
        Handler<Message<JSONObject>> handler = new Handler<Message<JSONObject>>() {
            @Override
            public void handle(Message<JSONObject> request) {
                try {
                    Log.d(TAG, "received invocation on address " + address);
                    JSONObject jbody = request.body();
                    String hex = jbody.optString("h");
                    byte[] token = ConversionUtility.fromHexString(hex);

                    usbTimerTask.write(token);

                    request.reply(jbody);
                } catch (Exception e) {
                    e.printStackTrace();
                    request.fail(HttpURLConnection.HTTP_INTERNAL_ERROR, e.getMessage());
                }
            }
        };
        MessageConsumer messageConsumer = EventBus.eventBus().consumer(address, handler);
        Log.d(TAG, "registered consumer on address " + messageConsumer.address());
        consumerCollection.add(messageConsumer);
        // endpointCollection.put(endpointAddress, messageConsumer);

        Log.d(TAG, "registered consumer on address " + messageConsumer.address());

        {
            String ENDPOINT = hendpoint;
            {
                final String procedure = TOPIC_JZ_DATA_FRAME_MANAGE_COMMAND + TOPIC_SEPARATOR + ENDPOINT;
                Object message = null;
                DeliveryOptions options = new DeliveryOptions();
                options.addHeader("procedure", procedure);
                options.addHeader("address", address);
                options.addHeader("invoke", "last");
                String busAddress = BusUtil.busAddress(ADDRESS_SERVICE_GLOBAL_REGISTER, BUS);

                Log.d(TAG, "registering procedure " + procedure + " on address " + address);

                eventBus.eventBus().send(busAddress, message, options, new Handler<AsyncResult<Message<JSONObject>>>() {
                    @Override
                    public void handle(AsyncResult<Message<JSONObject>> result) {
                        if (result.succeeded()) {
                            Log.d(TAG, "... registering procedure " + procedure + " on address " + address + " succeeded " + result.succeeded());
                        } else {
                            Log.e(TAG, "error: " + result.cause().getMessage());
                        }
                    }
                });
            }
        }


        future.complete();
    }


    private void onEndpointDetach(Future future, Long endpointAddress) {
        String hendpoint = ConversionUtility.getHex(endpointAddress, false);
        final String address = BusUtil.endpoint(ADDRESS_JZ_DRIVER_MANAGE_COMMAND, hendpoint);

        Iterator<MessageConsumer> iterator = consumerCollection.iterator();
        while (iterator.hasNext()) {
            MessageConsumer messageConsumer = iterator.next();
            if (address.equals(messageConsumer.address())) {
                messageConsumer.unregister();
                iterator.remove();
            }
        }

        {
            String ENDPOINT = hendpoint;
            {
                final String procedure = TOPIC_JZ_DATA_FRAME_MANAGE_COMMAND + TOPIC_SEPARATOR + ENDPOINT;
                Object message = null;
                DeliveryOptions options = new DeliveryOptions();
                options.addHeader("procedure", procedure);
                options.addHeader("address", address);
                options.addHeader("invoke", "last");
                String busAddress = BusUtil.busAddress(ADDRESS_SERVICE_GLOBAL_UNREGISTER, BUS);

                Log.d(TAG, "unregistering procedure " + procedure + " on address " + address);

                eventBus.eventBus().send(busAddress, message, options, new Handler<AsyncResult<Message<JSONObject>>>() {
                    @Override
                    public void handle(AsyncResult<Message<JSONObject>> result) {
                        if (result.succeeded()) {
                            Log.d(TAG, "... registering procedure " + procedure + " on address " + address + " succeeded " + result.succeeded());
                        } else {
                            Log.e(TAG, "error: " + result.cause().getMessage());
                        }
                    }
                });
            }
        }

        future.complete();
    }

    private void delegateMaster(final Future future, byte[] token, IFrame frame) throws Exception {
        String endpoint = ConversionUtility.getHex(ProtocolUtil.getSourceEndpointAddress(token));
        String hexToken = ConversionUtility.toHexString(token);
        //String connector = usbPort.getSerial();

        long index = USBPlugin.resolveSourceGlobalAddress(frame);
        String hindex = Long.toString(index);

        String path = TOPIC_JZ_DATA_FRAME_MASTER_MESSAGE;
        String busAddress = BusUtil.busAddress(ADDRESS_SERVICE_GLOBAL_CALL, BUS);

        DeliveryOptions options = new DeliveryOptions();
        options.setSendTimeout(DEFAULT_SEND_TIMEOUT);
        options.addHeader("path", path);
        options.addHeader("endpoint", endpoint);
        options.addHeader("index", hindex);

        /* message */
        JsonObject message = new JsonObject();
        // json.putBinary("data", token);
        message.put("uuid", UUID.randomUUID().toString());
        message.put("h", hexToken);
        message.put("t", System.currentTimeMillis());
        //message.put("u", connector);
        message.put("e", endpoint);

        Log.d(TAG, "delegating to master on path " + path + " ... ");
        eventBus.send(busAddress, message, options, new it.filippetti.sp.android.bus.io.Handler<AsyncResult<Message<JSONObject>>>() {
            @Override
            public void handle(AsyncResult<Message<JSONObject>> result) {
                try {
                    Log.d(TAG, "... delegating to master" + " | succeeded " + result.succeeded() + " | error " + result.cause());
                    if (result.succeeded()) {
                        future.complete(result.result().body());
                    } else {
                        Log.d(TAG, "error " + result.cause().getMessage());
                        future.fail(result.cause());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    future.fail(e);
                }
            }
        });

    }

    private void publishToken(byte[] token) throws Exception {

        Log.d(TAG, "publishing data for " + TENANT);
        if (TENANT != null) {
            String endpoint = ConversionUtility.getHex(ProtocolUtil.getSourceEndpointAddress(token));
            String path = "/" + TENANT;
//            path += "/jz/data/frame/message";
            path += "/jz/data/frame/input";
            path += "/" + endpoint;

            String address = BusUtil.busAddress(ADDRESS_SERVICE_GLOBAL_PUBLISH, BUS);
            DeliveryOptions options = new DeliveryOptions();
            options.addHeader("path", path);
            options.addHeader("endpoint", endpoint);

            Long t = System.currentTimeMillis();
            String hex = ConversionUtility.toHexString(token);
            JSONObject j = new JSONObject();
            j.put("t", t);
            j.put("h", hex);

            Log.d(TAG, "publishing to " + address + " -> " + path);
            eventBus.publish(address, j, options);
        }
    }

    public void discover() throws Exception {
        CoordinatorPingCommand pingCommand = new CoordinatorPingCommand();

        ZFrame zFrame = new ZFrame();
        zFrame.setZPacket(pingCommand);

        usbTimerTask.write(zFrame);
    }


    @Override
    public void stop() throws Exception {
        super.stop();
        try {
            if (usbTimer != null) {
                usbTimer.cancel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (usbPort != null) {
                usbPort.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Iterator<Long> iterator = endpointCollection.iterator();
        while (iterator.hasNext()) {
            Long endpoint = iterator.next();
            Future future = Future.future();
            onEndpointDetach(future, endpoint);
            iterator.remove();
        }

        Log.i(TAG, this.getClass().getSimpleName() + " started on " + usbPort);

    }

}
