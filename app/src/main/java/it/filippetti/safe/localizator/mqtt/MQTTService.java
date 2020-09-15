// https://medium.com/@sambhaji2134/jobintentservice-android-example-7f58bd2720bf

package it.filippetti.safe.localizator.mqtt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.lifecycle.MutableLiveData;

import it.filippetti.safe.localizator.App;
import it.filippetti.safe.localizator.ServiceResultReceiver;

/**
 * Created by Sambhaji Karad on 01/11/18.
 */

public class MQTTService extends JobIntentService {

    private static final String TAG = MQTTService.class.getSimpleName();
    public static final String RECEIVER = "receiver";
    public static final int SHOW_RESULT = 123;
    /**
     * Result receiver object to send results
     */
    //private ResultReceiver mResultReceiver;
    /**
     * Unique job ID for this service.
     */
    static final int DOWNLOAD_JOB_ID = 1000;
    /**
     * Actions download
     */
    private static final String ACTION_DOWNLOAD = "action.DOWNLOAD_DATA";

    public static final String START_MQTT = "action.START_MQTT";
    //private MqttHelper mqttHelper;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, ServiceResultReceiver workerResultReceiver) {
        Intent intent = new Intent(context, MQTTService.class);
        intent.putExtra(RECEIVER, workerResultReceiver);
        intent.setAction(ACTION_DOWNLOAD);
        enqueueWork(context, MQTTService.class, DOWNLOAD_JOB_ID, intent);
    }

    public static void enqueueNewWork(Context context, ServiceResultReceiver workerResultReceiver, String action) {
        Intent intent = new Intent(context, MQTTService.class);
        intent.putExtra(RECEIVER, workerResultReceiver);
        intent.setAction(action);
        enqueueWork(context, MQTTService.class, DOWNLOAD_JOB_ID, intent);
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.d(TAG, "onHandleWork() called with: intent = [" + intent + "]");
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case START_MQTT:
                    MqttHelper mqttHelper = ((App)getApplication()).getMQTTHelper();
                    System.out.println("Start MQTT? " + (mqttHelper==null));
                    if(mqttHelper==null) {
                        ((App) getApplication()).setMQTTHelper(startMqtt((ResultReceiver) intent.getParcelableExtra(RECEIVER)));
                        ((App) getApplication()).updateMqttStatus("Connecting..");
                    }
                    break;
                case ACTION_DOWNLOAD:
                   /* mResultReceiver = intent.getParcelableExtra(RECEIVER);
                    for(int i=0;i<10;i++){
                        try {
                            Thread.sleep(1000);
                            Bundle bundle = new Bundle();
                            bundle.putString("data",String.format("Showing From JobIntent Service %d", i));
                            mResultReceiver.send(SHOW_RESULT, bundle);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }*/
                    break;
            }
        }
    }

    private MqttHelper startMqtt(final ResultReceiver resultReceiver){
        MqttHelper mqttHelper = new MqttHelper(getApplicationContext());

        mqttHelper.mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("Debug","Connected");
                ((App) getApplication()).updateMqttStatus("Connected");
            }

            @Override
            public void connectionLost(Throwable throwable) {
                ((App) getApplication()).updateMqttStatus("Not connected");
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Debug",mqttMessage.toString());
                if(mqttMessage.isRetained()) {
                    Log.w("Warn mwtt", "Message retained.. skip!");
                    return;
                }
                System.out.println("Debug: " + mqttMessage.toString());
                Bundle bundle = new Bundle();
                bundle.putString("message", mqttMessage.toString());
                bundle.putString("topic", topic);
                resultReceiver.send(1, bundle);
                //dataReceived.setText(mqttMessage.toString());
                //mChart.addEntry(Float.valueOf(mqttMessage.toString()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
        return mqttHelper;
    }
}