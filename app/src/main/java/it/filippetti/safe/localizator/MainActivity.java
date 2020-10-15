package it.filippetti.safe.localizator;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.filippetti.safe.localizator.locator.RSSIDeviceLocatorImpl;
import it.filippetti.safe.localizator.mqtt.MQTTService;
import it.filippetti.safe.localizator.mqtt.MqttHelper;
import it.filippetti.sp.android.bus.io.EventBus;
import it.filippetti.sp.android.bus.io.MessageConsumer;

import static it.filippetti.safe.localizator.mqtt.MQTTService.SHOW_RESULT;

public class MainActivity extends AppCompatActivity  implements ServiceResultReceiver.Receiver {

    private EventBus eventBus = EventBus.eventBus();
    private List<MessageConsumer> consumerCollection = new ArrayList<>();

    private ServiceResultReceiver mServiceResultReceiver;
    private TextView mTextView;

    private static final String[] LOCATION_PERMS={
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        /*mServiceResultReceiver = new ServiceResultReceiver(new Handler());
        mServiceResultReceiver.setReceiver(this);
        mTextView = findViewById(R.id.textView);
        showDataFromBackground(MainActivity.this, mServiceResultReceiver);*/

//        Button buttonMQTT = findViewById(R.id.btxmqtt);
//        buttonMQTT.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                System.out.println("Button MQTT Clicked");
//                final RSSIDeviceLocatorImpl rssiDeviceLocator = new RSSIDeviceLocatorImpl(getApplication());
//                ((App)getApplication()).setRssiDeviceLocator(rssiDeviceLocator);
//
//                ServiceResultReceiver deviceDataResultReceiver = new ServiceResultReceiver(new Handler());
//                deviceDataResultReceiver.setReceiver(new ServiceResultReceiver.Receiver() {
//                    @Override
//                    public void onReceiveResult(int resultCode, Bundle resultData) {
//                        String message = resultData.getString("message");
//                        String topic = resultData.getString("topic");
//                        rssiDeviceLocator.onNewMessage(topic, message);
//                    }
//                });
//                MQTTService.enqueueNewWork(getApplicationContext(), deviceDataResultReceiver, MQTTService.START_MQTT);
//
//                // Start Location GPS
//               // requestPermissions(LOCATION_PERMS, 1337+3);
//                Intent startServiceIntent = new Intent(getApplicationContext(), SmartSetupService.class);
//                ServiceResultReceiver locationDataResultReceiver = new ServiceResultReceiver(new Handler());
//                locationDataResultReceiver.setReceiver(new ServiceResultReceiver.Receiver() {
//                    @Override
//                    public void onReceiveResult(int resultCode, Bundle resultData) {
//                        Location location = resultData.getParcelable("location");
//                        rssiDeviceLocator.onNewLocation(location);
//                    }
//                });
//                startServiceIntent.putExtra("location_receiver", locationDataResultReceiver);
//                startService(startServiceIntent);
////                Intent intent = new Intent(getApplicationContext(), MQTTService.class);
////                //intent.putExtra(MQTTService.START_MQTT, mServiceResultReceiver);
////                intent.setAction(MQTTService.START_MQTT);
////                startService(intent);
////                //startActivity(intent);
//            }
//        });
    }
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//
//
//        switch(requestCode) {
//
//            case 1337+3:
//              System.out.println("oooks");
//                break;
//        }
//    }


    private void showDataFromBackground(MainActivity mainActivity, ServiceResultReceiver mResultReceiver) {
        MQTTService.enqueueWork(mainActivity, mResultReceiver);
    }

    public void showData(String data) {
        if(mTextView != null)
            mTextView.setText(String.format("%s\n%s", mTextView.getText(), data));
        else
            System.err.println("TextView is empty");
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case SHOW_RESULT:
                if (resultData != null) {
                    showData(resultData.getString("data"));
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            System.out.println("Action set");
            Intent activity2Intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(activity2Intent);


            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}