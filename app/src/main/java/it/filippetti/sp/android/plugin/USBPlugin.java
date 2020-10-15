package it.filippetti.sp.android.plugin;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import it.filippetti.sp.android.bus.io.AsyncResult;
import it.filippetti.sp.android.bus.io.DeliveryOptions;
import it.filippetti.sp.android.bus.io.EventBus;
import it.filippetti.sp.android.bus.io.Handler;
import it.filippetti.sp.android.bus.io.Message;
import it.filippetti.sp.android.bus.io.MessageConsumer;
import it.filippetti.sp.android.util.BusUtil;
import it.filippetti.sp.android.util.IJZConstants;
import it.filippetti.sp.android.util.JsonObject;
//import it.filippetti.sp.android.wamp.client.RegisterOptions;
import it.smartspace.common.util.ConversionUtility;
import it.smartspace.jz.communication.ZFrame;
import it.smartspace.jz.communication.ZPacket;
import it.smartspace.jz.communication.frame.FrameAppData;
import it.smartspace.jz.communication.frame.FramePacket;
import it.smartspace.jz.communication.frame.SSFFrame;
import it.smartspace.jz.communication.interfaces.IFrame;
import it.smartspace.jz.communication.message.IProtocolVersion25;
import it.smartspace.jz.communication.message.RouterEnveloperMessage;
import it.smartspace.jz.communication.message.RouterForwardMessage;
import it.smartspace.jz.communication.message.ZEnvelopedMessage;
import it.smartspace.jz.communication.message.ZEnvelopedProtocolHeader;
import it.smartspace.jz.communication.util.ProtocolUtil;
import it.smartspace.jz.communication.util.TokenParser;

import static it.filippetti.sp.android.util.IAddressConstants.ADDRESS_SERVICE_GLOBAL_CALL;
import static it.filippetti.sp.android.util.IAddressConstants.ADDRESS_SERVICE_GLOBAL_REGISTER;

public class USBPlugin extends AbstractPlugin implements IJZConstants {
    private String TAG = "AAA_" + USBPlugin.class.getSimpleName();
    ResultReceiver receiver;

    public ResultReceiver getReceiver() {
        return receiver;
    }

    public void setReceiver(ResultReceiver receiver) {
        this.receiver = receiver;
    }

    /**/
    public String BUS = "bu";
    public String TENANT = "tenant";

    /**/
    private boolean MASTER = true;

    /**/
    private UsbManager usbManager = null;

    /**/
    private List<USBConnector> usbConnectorCollection = new ArrayList<USBConnector>();
    //private List<MessageConsumer> consumerCollection = new ArrayList<>();

    @Override
    public JSONObject configure(JSONObject config) {
        return config;
    }

    @Override
    public void start() throws Exception {
        super.start();

        /* usb */
        try {
            /**/
            List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);
            if (!availableDrivers.isEmpty()) {
                for (UsbSerialDriver driver : availableDrivers) {
                    try {
                        // Open a connection to the first available driver.

                        UsbDeviceConnection connection = usbManager.openDevice(driver.getDevice());
                        if (connection != null) {
                            //Toast.makeText(this, "### " + availableDrivers.size(), Toast.LENGTH_SHORT).show(); // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
                            // Read some data! Most have just one port (port 0).
                            try {
                                UsbSerialPort _port = driver.getPorts().get(0);

                                if (_port != null) {

                                    USBConnector usbConnector = new USBConnector(connection, _port);
                                    usbConnector.TENANT = TENANT;
                                    usbConnector.BUS = BUS;
                                    usbConnector.RECEIVER = receiver;
                                    usbConnector.start();

                                    usbConnectorCollection.add(usbConnector);
                                }
                            } catch (Exception e) {
                                // Deal with error.
                                Log.e(TAG, "error " + e.getMessage(), e);

                                //                        try {
                                //                            if(port != null) {
                                //                                port.close();
                                //                            }
                                //                        } catch (Exception e) {
                                //                            e.printStackTrace();
                                //                        }
                            }
                        } else {
//                            Toast.makeText(this, "usb connection failed", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "usb connection failed");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "error: " + e.getMessage());
                    }
                }
            } else {
//                Toast.makeText(this, R.string.usb_not_supported, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "usb connected device not supported");
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Log.e(TAG, "error: " + e.getMessage(), e);
        }


        if (MASTER) {
            /* master message */

            {
                Handler<Message<JSONObject>> handler = new Handler<Message<JSONObject>>() {
                    @Override
                    public void handle(Message<JSONObject> request) {
                        try {
                            Log.d(TAG, "received invocation on address " + request.address());
                            JSONObject jbody = request.body();
                            String hex = jbody.optString("h");

                            byte[] token = ConversionUtility.fromHexString(hex);

                            // TODO... do master logic: keep faster/stronger endpoint

                            request.reply(jbody);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(TAG, e.getMessage());
                            request.fail(HttpURLConnection.HTTP_INTERNAL_ERROR, e.getMessage());
                        }
                    }
                };
                final String address = BusUtil.busAddress(ADDRESS_JZ_DRIVER_MASTER_MESSAGE, BUS);
                MessageConsumer messageConsumer = EventBus.eventBus().consumer(address, handler);
                Log.d(TAG, "registered consumer on address " + messageConsumer.address());
                consumerCollection.add(messageConsumer);

                {
                    final String procedure = TOPIC_JZ_DATA_FRAME_MASTER_MESSAGE;
                    Object message = null;
                    DeliveryOptions options = new DeliveryOptions();
                    options.addHeader("procedure", procedure);
                    options.addHeader("address", address);
                    //options.addHeader("invoke", RegisterOptions.INVOKE_RANDOM); // TODO: removed for WAMP
                    //options.addHeader("match", RegisterOptions.MATCH_EXACT); // TODO: removed for WAMP
                    String busAddress = BusUtil.busAddress(ADDRESS_SERVICE_GLOBAL_REGISTER, BUS);

                    Log.d(TAG, "mapping procedure " + procedure + " to address " + address);

                    eventBus.eventBus().send(busAddress, message, options, new Handler<AsyncResult<Message<JSONObject>>>() {
                        @Override
                        public void handle(AsyncResult<Message<JSONObject>> result) {
                            Log.d(TAG, "... mapping procedure " + procedure + " on address " + address + " succeeded " + result.succeeded() + " error " + result.cause());

                        }
                    });
                }
            }

            /* master command */
            /* SSS protocol ping command */
            /* 53535300000000000000000000080000000008 */
            /* 535353E10D0000FFFFFFFF00000800010000E5 -> global address 0xffffffff00000de1 = {"index": -4294963743} */
            /* 53535331040000FFFFFFFF000008000100003C -> global address 0xffffffff00000431 = {"index": -4294966223} */
            /* SSF protocol ping command */
            /* 5353460000000000000000FFFF0000FF0700000000F8 */
            /* 535346E18D0000E18D00006966000000080000000007 */
            /* 5353460000318400000000FFFF0000FF07000000004D */
            {
                /* this client register procedure to handle command as master with "random" invoke policy to allow the router to use the "index" policy.
                 * es. | /jz/master/frame/command | exact | random |
                 * another client MUST invoke this procedure with protocol option {"index": $index} where $index is the global address of the target device.
                 * the router will select the $index master and will send the command to that master.
                 * the master will parse the token, extract the protocol endpoint address and invoke the "last" registered procedure to that the real endpoint address */
                Handler<Message<JSONObject>> handler = new Handler<Message<JSONObject>>() {
                    @Override
                    public void handle(final Message<JSONObject> request) {
                        try {
                            Log.d(TAG, "received invocation on address " + request.address());
                            final JSONObject jbody = request.body();
                            String hexToken = jbody.optString("h");


                            byte[] token = ConversionUtility.fromHexString(hexToken);

                            /**/
                            Long endpoint = ProtocolUtil.getTargetEndpointAddress(token);
                            String hendpoint = ConversionUtility.getHex(endpoint);

                            /* message */
                            final JsonObject message = new JsonObject();
                            // json.putBinary("data", token);
                            message.put("uuid", UUID.randomUUID().toString());
                            message.put("h", hexToken);
                            message.put("t", System.currentTimeMillis());
                            // message.put("u", connector);
                            // message.put("e", endpoint);

                            if (!(ProtocolUtil.isEndpointReserved(endpoint))) {
                                final String path = TOPIC_JZ_DATA_FRAME_MANAGE_COMMAND + TOPIC_SEPARATOR + hendpoint;

                                DeliveryOptions deliveryOptions = new DeliveryOptions();
                                deliveryOptions.setSendTimeout(SEND_TIMEOUT);
                                deliveryOptions.addHeader("procedure", path);
                                //deliveryOptions.addHeader("invoke", RegisterOptions.INVOKE_LAST); //TODO: removed for no wamp

                                Log.d(TAG, "delegating to target endpoint manager on path " + path + " ... ");

                                String busAddress = BusUtil.busAddress(ADDRESS_SERVICE_GLOBAL_CALL, BUS);
                                eventBus.send(busAddress, jbody, deliveryOptions, new Handler<AsyncResult<Message<JSONObject>>>() {
                                    @Override
                                    public void handle(AsyncResult<Message<JSONObject>> result) {
                                        Log.d(TAG, "... delegating to target endpoint manager on path " + path + " succeeded " + result.succeeded() + " error " + result.cause());
                                        if (result.succeeded()) {
                                            request.reply(message);
                                        } else {
                                            Log.e(TAG, "error: " + result.cause().getMessage());
                                            request.fail(HttpURLConnection.HTTP_INTERNAL_ERROR, result.cause().getMessage());
                                        }
                                    }
                                });
                            } else {
//                                Exception e = new IllegalArgumentException(hendpoint);
//                                request.fail(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());

                                String busAddress = BusUtil.busAddress(ADDRESS_ENDPOINT_DISCOVER, BUS);
                                eventBus.publish(busAddress, message);
                                /**/
                                request.reply(message);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "error: " + e.getMessage());
                            request.fail(HttpURLConnection.HTTP_INTERNAL_ERROR, e.getMessage());
                        }
                    }
                };
                final String address = BusUtil.busAddress(ADDRESS_JZ_DRIVER_MASTER_COMMAND, BUS);
                MessageConsumer messageConsumer = EventBus.eventBus().consumer(address, handler);
                Log.d(TAG, "registered consumer on address " + messageConsumer.address());
                consumerCollection.add(messageConsumer);

                {
                    final String procedure = TOPIC_JZ_DATA_FRAME_MASTER_COMMAND;
                    JSONObject jbody = new JSONObject();
                    DeliveryOptions options = new DeliveryOptions();
                    options.addHeader("address", address);
                    options.addHeader("procedure", procedure);
                    //options.addHeader("invoke", RegisterOptions.INVOKE_RANDOM); //TODO: removed for no wamp
                    //options.addHeader("match", RegisterOptions.MATCH_EXACT); //TODO: removed for no wamp
                    String busAddress = BusUtil.busAddress(ADDRESS_SERVICE_GLOBAL_REGISTER, BUS);

                    Log.d(TAG, "mapping procedure " + procedure + " to address " + address);

                    eventBus.eventBus().send(busAddress, jbody, options, new Handler<AsyncResult<Message<JSONObject>>>() {
                        @Override
                        public void handle(AsyncResult<Message<JSONObject>> result) {
                            Log.d(TAG, "... mapping procedure " + procedure + " on address " + address + " succeeded " + result.succeeded() + " error " + result.cause());

                        }
                    });
                }
            }
        }

        Log.i(TAG, this.getClass().getName() + " started");
    }

    public static long resolveSourceGlobalAddress(byte[] token) throws Exception {
        return resolveSourceGlobalAddress(TokenParser.parse(token));
    }

    public static long resolveSourceGlobalAddress(IFrame frame) throws Exception {
        long address = 0;
        if (frame instanceof ZFrame) {
            ZFrame fr = ((ZFrame) frame);

            ZPacket packet = fr.getZPacket();
            // byte sequence = packet.getSequenceNumber();

            if (packet instanceof RouterForwardMessage) {
                RouterForwardMessage app = (RouterForwardMessage) packet;
                ZEnvelopedMessage message = app.getEnvelopedMessage();
                ZEnvelopedProtocolHeader header = message.getProtocolHeader();
                byte protocolVersion = message.getProtocolVersion();
                if (protocolVersion >= (byte) 0x23) {
                    address = message.getEndDeviceAddress();

                }

            } else if (packet instanceof RouterEnveloperMessage) {
                RouterEnveloperMessage app = (RouterEnveloperMessage) packet;
                if (app.getEnvelopedCollection().size() == 1) {
                    ZEnvelopedMessage enveloped = app.getEnvelopedCollection().get(0);
                    ZEnvelopedProtocolHeader header = enveloped.getProtocolHeader();

                    byte protocolVersion = enveloped.getProtocolVersion();
                    if (enveloped instanceof IProtocolVersion25 && enveloped.getProtocolVersion() >= 0x25 && enveloped.getProtocolVersion() < 0x30) {
                        IProtocolVersion25 msg = (IProtocolVersion25) enveloped;
                        address = enveloped.getEndDeviceAddress();
                    }
                    if (protocolVersion >= (byte) 0x30) {
                        address = enveloped.getEndDeviceAddress();
                    }

                }
            }
        } else if (frame instanceof SSFFrame) {
            SSFFrame fr = ((SSFFrame) frame);

            FramePacket packet = fr.getPacket();

            if (packet instanceof FrameAppData) {
                FrameAppData app = (FrameAppData) packet;
                address = ConversionUtility.unsigned(fr.getSourceAddress());
                // address = app.getDeviceId();
            }
        }
        return address;
    }


    public UsbManager getUsbManager() {
        return usbManager;
    }

    public void setUsbManager(UsbManager manager) {
        this.usbManager = manager;
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        /**/
        for (USBConnector usbConnector : usbConnectorCollection) {
            try {
                usbConnector.stop();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }



        Log.i(TAG, this.getClass().getName() + " stopped");
    }
}
