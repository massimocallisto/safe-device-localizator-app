package it.filippetti.safe.localizator;

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

import it.filippetti.safe.localizator.locator.RSSIDeviceLocatorImpl;
import it.filippetti.safe.localizator.mqtt.MQTTService;

public class FirstFragment extends Fragment {

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
    View view;

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        setMqttStatusUpdate();


        Button buttonMQTT = view.findViewById(R.id.mqttbtx1);
        buttonMQTT.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                System.out.println("Button MQTT Clicked");
                App app = (App)getActivity().getApplication();
                final RSSIDeviceLocatorImpl rssiDeviceLocator = new RSSIDeviceLocatorImpl(app);
                app.setRssiDeviceLocator(rssiDeviceLocator);

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

                // Start Location GPS
                // requestPermissions(LOCATION_PERMS, 1337+3);
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
//                Intent intent = new Intent(getApplicationContext(), MQTTService.class);
//                //intent.putExtra(MQTTService.START_MQTT, mServiceResultReceiver);
//                intent.setAction(MQTTService.START_MQTT);
//                startService(intent);
//                //startActivity(intent);
            }
        });

        /*view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });*/

       // final View _view = view;
        Button buttonOne = view.findViewById(R.id.btxmap);
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