package it.filippetti.safe.localizator;

import android.app.Application;

import java.util.UUID;

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
}
