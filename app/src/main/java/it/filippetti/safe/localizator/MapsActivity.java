package it.filippetti.safe.localizator;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import it.filippetti.safe.localizator.locator.HeatMapRSSIDeviceLocatorImpl;
import it.filippetti.safe.localizator.locator.RSSIDeviceLocator;
import it.filippetti.safe.localizator.locator.RSSIDeviceLocatorImpl;
import it.filippetti.safe.localizator.model.CoordinatorIoT;
import it.filippetti.safe.localizator.model.DeviceIoT;
import it.filippetti.safe.localizator.mqtt.MQTTService;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, AdapterView.OnItemSelectedListener {

    /**
     * Alternative radius for convolution
     */
    private static final int ALT_HEATMAP_RADIUS = 10;

    /**
     * Alternative opacity of heatmap overlay
     */
    private static final double ALT_HEATMAP_OPACITY = 0.4;

    /**
     * Alternative heatmap gradient (blue -> red)
     * Copied from Javascript version
     */
    private static final int[] ALT_HEATMAP_GRADIENT_COLORS = {
            Color.argb(0, 0, 255, 255),// transparent
            Color.argb(255 / 3 * 2, 0, 255, 255),
            Color.rgb(0, 191, 255),
            Color.rgb(0, 0, 127),
            Color.rgb(255, 0, 0)
    };

    public static final float[] ALT_HEATMAP_GRADIENT_START_POINTS = {
            0.0f, 0.10f, 0.20f, 0.60f, 1.0f
    };

    public static final Gradient ALT_HEATMAP_GRADIENT = new Gradient(ALT_HEATMAP_GRADIENT_COLORS,
            ALT_HEATMAP_GRADIENT_START_POINTS);

    private GoogleMap mMap;
    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;
    private String trackedDevice = "";
    private Spinner spinner;

    Marker marker;
    private ArrayList<String> deviceList;
    private ArrayAdapter<String> deviceListAdapter;
    boolean isLocationUpdated = false;

    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        String s = (String)this.spinner.getItemAtPosition(position);
        System.out.println("Selected device list. Value; " + s);
        this.trackedDevice = s;
        refreshHeatMap();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("MNaps creation start...");

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        spinner = (Spinner)findViewById(R.id.trackdevicespinner);
        deviceList = new ArrayList<String>();

        deviceListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, deviceList);
        spinner.setAdapter(deviceListAdapter);
        spinner.setOnItemSelectedListener(this);
    }

    void updateTrackedDevice(List<DeviceIoT> deviceIoTList){
        deviceList.clear();
        deviceList.add("-----------");
        for(DeviceIoT d : deviceIoTList) {
            deviceList.add(d.getName());
        }
        deviceListAdapter.notifyDataSetChanged();
        makeSelected();
    }

    void makeSelected(){
        if(trackedDevice != null){
            for(int i = 0; i < deviceList.size(); i++){
                if(deviceList.get(i).equals(trackedDevice))
                    spinner.setSelection(i);
                return;
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng location = setCurrentLocation(43, 13);
        setCurrentLocation(location);
        lookAt(location);

        if(isLocationUpdated)
            lookAt();

        ImageButton buttonLocate = findViewById(R.id.Button01);
        buttonLocate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                lookAt();
            }
        });
        /*ImageButton ButtonClear = findViewById(R.id.ButtonClear);
        ButtonClear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("Maps", "Cleaning heatmap...");
                updateHeatMap(new ArrayList<CoordinatorIoT.DeviceLocation>());
            }
        });*/


        //LatLng sydney = new LatLng(0, 0);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in ?"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        //addHeatMap();*)
    }

    void clearHeatMap(){
        if(mProvider != null) {
            mOverlay.remove();
            mOverlay.clearTileCache();
        }
    }

    private void updateHeatMap(List<CoordinatorIoT.DeviceLocation> trackedDevice) {
        clearHeatMap();
        if(trackedDevice == null || trackedDevice .isEmpty()){
            return;
        }
        try {
            ArrayList<WeightedLatLng> items = HeatMapRSSIDeviceLocatorImpl.getWeightedHeatMap(trackedDevice);
            mProvider = new HeatmapTileProvider.Builder()
                    .weightedData(items)
                    .build();
            // Add a tile overlay to the map, using the heat map tile provider.
            mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        } catch (Exception e) {
            Toast.makeText(this, "Problem reading list of locations.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public LatLng setCurrentLocation(double lan, double lon){
        return setCurrentLocation(new LatLng(lan, lon));
    }

    public LatLng setCurrentLocation(LatLng location){
        if(marker == null) {
            marker = mMap.addMarker(new MarkerOptions().position(location).title("Marker in..."));
            lookAt(location);
        }else
            marker.setPosition(location);
        return location;
    }

    public void lookAt(){
        if(marker != null)
           lookAt(marker.getPosition());
    }

    public void lookAt(final LatLng location){
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
            }

            @Override
            public void onCancel() {

            }
        });

    }

    Observer<List<DeviceIoT>> o = new Observer<List<DeviceIoT>>() {
        @Override
        public void onChanged(List<DeviceIoT> deviceIoTS) {
            Log.d("LiveData", "Updated devices..");
            updateTrackedDevice(deviceIoTS);
            refreshHeatMap();
        }
    };

    void refreshHeatMap(){
        if(trackedDevice != null){
            CoordinatorIoT coordinatorIoT = ((App) getApplicationContext()).getRssiDeviceLocator().getCoordinatorIoT();
            // Get single device
            List<CoordinatorIoT.DeviceLocation> lastLocations = coordinatorIoT.getTrackedDevice(this.trackedDevice);
            updateHeatMap(lastLocations != null ? lastLocations : new ArrayList<CoordinatorIoT.DeviceLocation>());
        }
    }

    Observer<LatLng> o2 = new Observer<LatLng>() {
        @Override
        public void onChanged(LatLng lastLocation) {
            Log.d("LiveData", "Updated lastLocation..");
            if(lastLocation != null && mMap != null) {
                setCurrentLocation(lastLocation);
                isLocationUpdated = true;
            }
        }
    };

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        super.onAttachFragment(fragment);
        MutableLiveData<List<DeviceIoT>> liveDeviceIoT = ((App) getApplicationContext()).getDeviceIoT();
        liveDeviceIoT.observe(this, o);

        MutableLiveData<LatLng> lastLocation = ((App) getApplicationContext()).getLastLocation();
        lastLocation.observe(this, o2);
    }
}