package it.filippetti.safe.localizator;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import it.filippetti.safe.localizator.locator.RSSIDeviceLocatorImpl;
import it.filippetti.safe.localizator.mqtt.MQTTService;
import it.filippetti.sp.android.driver.SPDriverService;

public class FirstFragment extends Fragment {
    private String TAG = "AAA_" + FirstFragment.class.getSimpleName();

    private static final String[] LOCATION_PERMS={
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    Observer<String> o2 = new Observer<String>() {
        @Override
        public void onChanged(String txt) {
            Log.d("MQTT status update", "? " + txt);
            View viewById = view.findViewById(R.id.mqtttxtstatus);
            if(viewById != null){
                ((TextView)viewById).setText(txt);
            }
        }
    };

    public void setMqttStatusUpdate() {
        MutableLiveData<String> mqttStatus = ((App)getActivity().getApplication()).getMQTTStatus();
        mqttStatus.observe(this, o2);
    }

    Observer<String> o = new Observer<String>() {
        @Override
        public void onChanged(String txt) {
            Log.d("Loc status update", "? " + txt);
            View viewById = view.findViewById(R.id.textViewGPS);
            if(viewById != null){
                ((TextView)viewById).setText(txt);
            }
        }
    };

    public void setLocalizationStatusUpdate() {
        MutableLiveData<String> gpsStatus = ((App)getActivity().getApplication()).getLocalizationStatus();
        gpsStatus.observe(this, o);
    }

    View view;

    void initLocalizator(App app){
        if(app.getRssiDeviceLocator() == null){
            RSSIDeviceLocatorImpl _rssiDeviceLocator = new RSSIDeviceLocatorImpl(app);
            app.setRssiDeviceLocator(_rssiDeviceLocator);

        }
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        setMqttStatusUpdate();
        setLocalizationStatusUpdate();

        String mqttServer = ((App) getActivity().getApplication()).getMqttserverUri();
        if(mqttServer != null){
            View viewById = view.findViewById(R.id.mqtttxtstatus);
            if(viewById != null){
                ((TextView)viewById).setText("Click to start listening on " + mqttServer);
            }
        }
        View viewById = view.findViewById(R.id.textViewGPS);
        if(viewById != null){
            ((TextView)viewById).setText("Click to start acquiring GPS location");
        }


        Button buttonMQTT = view.findViewById(R.id.mqttbtx1);
        buttonMQTT.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                System.out.println("Button MQTT Clicked");
                App app = (App)getActivity().getApplication();
                initLocalizator(app);
                final RSSIDeviceLocatorImpl rssiDeviceLocator = (RSSIDeviceLocatorImpl) app.getRssiDeviceLocator();

                ServiceResultReceiver deviceDataResultReceiver = new ServiceResultReceiver(new Handler());
                deviceDataResultReceiver.setReceiver(new ServiceResultReceiver.Receiver() {
                    @Override
                    public void onReceiveResult(int resultCode, Bundle resultData) {
                        String message = resultData.getString("message");
                        String topic = resultData.getString("topic");
                        rssiDeviceLocator.onNewMessage(topic, message);
                    }
                });
                MQTTService.enqueueNewWork(app, deviceDataResultReceiver, MQTTService.START_MQTT);
            }
        });
        Button btxlocalization = view.findViewById(R.id.btxlocalization);
        btxlocalization.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                System.out.println("Button btxlocalization Clicked");
                App app = (App)getActivity().getApplication();
                initLocalizator(app);
                final RSSIDeviceLocatorImpl rssiDeviceLocator = (RSSIDeviceLocatorImpl) app.getRssiDeviceLocator();

                // Start Location GPS
                requestPermissions(LOCATION_PERMS, 1337+3);
                Intent startServiceIntent = new Intent(app, SmartSetupService.class);
                ServiceResultReceiver locationDataResultReceiver = new ServiceResultReceiver(new Handler());
                locationDataResultReceiver.setReceiver(new ServiceResultReceiver.Receiver() {
                    @Override
                    public void onReceiveResult(int resultCode, Bundle resultData) {
                        Location location = resultData.getParcelable("location");
                        rssiDeviceLocator.onNewLocation(location);
                    }
                });
                startServiceIntent.putExtra("location_receiver", locationDataResultReceiver);
                getActivity().startService(startServiceIntent);
            }
        });

        final View view2 = view;
        Button btxUSB = view.findViewById(R.id.button_usb);
        btxUSB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "service starting");
                App app = (App)getActivity().getApplication();
                initLocalizator(app);
                final RSSIDeviceLocatorImpl rssiDeviceLocator = (RSSIDeviceLocatorImpl) app.getRssiDeviceLocator();

                ServiceResultReceiver deviceDataResultReceiver = new ServiceResultReceiver(new Handler());
                deviceDataResultReceiver.setReceiver(new ServiceResultReceiver.Receiver() {
                    @Override
                    public void onReceiveResult(int resultCode, Bundle resultData) {
                        // TODO
                        /*String message = resultData.getString("message");
                        String topic = resultData.getString("topic");
                        rssiDeviceLocator.onNewMessage(topic, message);*/

                        try {
                            String address = resultData.getString("address", "unknown_address");
                            long rssi = resultData.getLong("rssi",  Long.MIN_VALUE);
                            JSONObject sn = new JSONObject();
                            sn.put("ref", address);
                            JSONArray r = new JSONArray();
                            JSONObject rssiObj = new JSONObject();
                            rssiObj.put("k", "rssi");
                            rssiObj.put("v", rssi);
                            r.put(rssiObj);
                            sn.put("r", r);
                            rssiDeviceLocator.onNewMessage("some/topic", sn.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        System.out.println("Got message from USB");
                        String txt = "Last contact: " + new Date().toString();
                        View viewById = view2.findViewById(R.id.label_usb);
                        if(viewById != null){
                            ((TextView)viewById).setText(txt);
                        }
                    }
                });
                SPDriverService.enqueueNewWork(app, deviceDataResultReceiver, SPDriverService.START_SERVICE);
                String txt = "Waiting from USB...";
                View viewById = view2.findViewById(R.id.label_usb);
                if(viewById != null){
                    ((TextView)viewById).setText(txt);
                }
            }
        });




        // final View _view = view;
        Button buttonOne = view.findViewById(R.id.viewmap);
        if(buttonOne != null)
            buttonOne.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    System.out.println("Button Clicked");
                    Intent activity2Intent = new Intent(getActivity(), MapsActivity.class);
                    startActivity(activity2Intent);
                }
            });
    }
}