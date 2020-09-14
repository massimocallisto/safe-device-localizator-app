package it.filippetti.safe.localizator;

import android.app.Application;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.UUID;

import it.filippetti.safe.localizator.locator.RSSIDeviceLocator;
import it.filippetti.safe.localizator.model.DeviceIoT;
import it.filippetti.safe.localizator.mqtt.MqttHelper;

/**
 * Created by matteo on 22/09/2015.
 */
public class App extends Application {

    private static final String TAG = App.class.getSimpleName();
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private transient Long coordinatorIEEEAddress;
    private boolean commandIsRunning;
    private MqttHelper mqttHelper;
    private RSSIDeviceLocator rssiDeviceLocator;
    private MutableLiveData<List<DeviceIoT>> deviceIoT = new MutableLiveData<>();
    private MutableLiveData<LatLng> lastLocation = new MutableLiveData<>();


    String mqttserverUri = "tcp://test.mosquitto.org:1883";
    String mqttclientId = "ExampleAndroidClientSAFE";
    String mqttsubscriptionTopic = "safe/+";
    String mqttusername = "xxxxxxx";
    String mqttpassword = "yyyyyyy";

    public MutableLiveData<List<DeviceIoT>> getDeviceIoT() {
        return deviceIoT;
    }

    public void updateDeviceIoT(List<DeviceIoT> deviceList){
        deviceIoT.postValue(deviceList);
    }

    public MutableLiveData<LatLng> getLastLocation() {
        return lastLocation;
    }

    public void updateLocation(LatLng latLng){
        lastLocation.postValue(latLng);
    }



    @Override
    public void onCreate() {
        super.onCreate();

    }

    public MqttHelper getMQTTHelper() {
        return mqttHelper;
    }
    public void setMQTTHelper(MqttHelper mqttHelper) {
        this.mqttHelper = mqttHelper;
    }

    public RSSIDeviceLocator getRssiDeviceLocator() {
        return rssiDeviceLocator;
    }

    public void setRssiDeviceLocator(RSSIDeviceLocator rssiDeviceLocator) {
        this.rssiDeviceLocator = rssiDeviceLocator;
    }

    public String getMqttserverUri() {
        return mqttserverUri;
    }

    public void setMqttserverUri(String mqttserverUri) {
        this.mqttserverUri = mqttserverUri;
    }

    public String getMqttclientId() {
        return mqttclientId;
    }

    public void setMqttclientId(String mqttclientId) {
        this.mqttclientId = mqttclientId;
    }

    public String getMqttsubscriptionTopic() {
        return mqttsubscriptionTopic;
    }

    public void setMqttsubscriptionTopic(String mqttsubscriptionTopic) {
        this.mqttsubscriptionTopic = mqttsubscriptionTopic;
    }

    public String getMqttusername() {
        return mqttusername;
    }

    public void setMqttusername(String mqttusername) {
        this.mqttusername = mqttusername;
    }

    public String getMqttpassword() {
        return mqttpassword;
    }

    public void setMqttpassword(String mqttpassword) {
        this.mqttpassword = mqttpassword;
    }
}
