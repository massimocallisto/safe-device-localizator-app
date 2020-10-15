package it.filippetti.safe.localizator;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import it.filippetti.safe.localizator.locator.HeatMapRSSIDeviceLocatorImpl;
import it.filippetti.safe.localizator.locator.RSSIDeviceLocator;
import it.filippetti.safe.localizator.locator.RSSIDeviceLocatorImpl;
import it.filippetti.safe.localizator.model.CoordinatorIoT;
import it.filippetti.safe.localizator.model.DeviceIoT;
import it.filippetti.safe.localizator.mqtt.MQTTService;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    /**
     * Alternative radius for convolution
     */
    //private static final int ALT_HEATMAP_RADIUS = 10;

    /**
     * Alternative opacity of heatmap overlay
     */
    private static final double ALT_HEATMAP_OPACITY = 0.4;
    private static final float DEFAULT_ZOOM_LEVEL = 15;

    /**
     * Alternative heatmap gradient (blue -> red)
     * Copied from Javascript version
     */
    /*private static final int[] ALT_HEATMAP_GRADIENT_COLORS = {
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
            ALT_HEATMAP_GRADIENT_START_POINTS);*/

    private GoogleMap mMap;
    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;
    private String trackedDevice = "";
    private Spinner spinner;
    private boolean useHeatmap = false;

    Marker marker;
    private ArrayList<String> deviceList;
    private ArrayAdapter<String> deviceListAdapter;
    boolean isLocationUpdated = false;
    Set<String> devicesMap = new HashSet<>();

    /*@Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        String s = (String)this.spinner.getItemAtPosition(position);
        System.out.println("Selected device list. Value; " + s);
        this.trackedDevice = s;
        refreshHeatMap();
    }*/

   /* @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub
    }*/


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
        spinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        //showToast("Spinner1: position=" + position + " id=" + id);
                        String s = (String)spinner.getItemAtPosition(position);
                        System.out.println("Selected device list. Value; " + s);
                        trackedDevice = s;
                        flyToDevice(trackedDevice);
                        refreshHeatMap();
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        //showToast("Spinner1: unselected");
                    }
                });
        //spinner.setOnItemSelectedListener(this);
        updateTrackedDevice();

        /*Button btnSetting = (Button)findViewById(R.id.btn_setting_map);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapsViewSettings.class);
                startActivity(intent);
            }
        });*/
        ImageButton btnClear = (ImageButton)findViewById(R.id.map_clean);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RSSIDeviceLocator rssiDeviceLocator = ((App) getApplicationContext()).getRssiDeviceLocator();
                if(rssiDeviceLocator != null){
                    CoordinatorIoT coordinatorIoT = rssiDeviceLocator.getCoordinatorIoT();
                    if(coordinatorIoT != null) {
                        coordinatorIoT.clearData();
                        trackedDevice = null;
                        updateTrackedDevice();
                        refreshHeatMap();

                    }
                }
            }
        });
        ImageButton btnRefresh = (ImageButton)findViewById(R.id.map_refresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshHeatMap();
                lookAt();
            }
        });

        ImageButton btnHeatmap  = (ImageButton)findViewById(R.id.map_heatmap);
        btnHeatmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                useHeatmap = !useHeatmap;
                refreshHeatMap();
            }
        });

    }

    void updateTrackedDevice(){
        String currentDevice = trackedDevice;
        deviceList.clear();
        RSSIDeviceLocator rssiDeviceLocator = ((App) getApplicationContext()).getRssiDeviceLocator();
        if(rssiDeviceLocator != null){
            List<String> names = rssiDeviceLocator.getCoordinatorIoT().getTrackedDeviceNames();
            deviceList.add("Select a device..");
            for(String d : names) {
                deviceList.add(d);
            }
        }else
            deviceList.add("Listener device not active!");
        deviceListAdapter.notifyDataSetChanged();
        //makeSelected(currentDevice);
        spinner.setSelection(deviceListAdapter.getPosition(currentDevice));
    }

    /*void makeSelected(String currentDevice){
        if(currentDevice != null){
            for(int i = 0; i < deviceList.size(); i++){
                if(deviceList.get(i).equals(currentDevice))
                    spinner.setSelection(i);
                return;
            }
        }
    }*/

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
        RSSIDeviceLocator rssiDeviceLocator = ((App) getApplicationContext()).getRssiDeviceLocator();
        if(rssiDeviceLocator != null) {
            Location lastLocation = rssiDeviceLocator.getLastLocation();
            if (lastLocation != null) {
                setCurrentLocation(lastLocation.getLatitude(), lastLocation.getLongitude());
                lookAt();
            }
        }

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
        for(Circle p : points){
            p.remove();
        }
        points.clear();
    }

    private void updateHeatMap(List<CoordinatorIoT.DeviceLocation> trackedDevice) {
        clearHeatMap();
        if(trackedDevice == null || trackedDevice .isEmpty()){
            return;
        }


        try {
            if(useHeatmap){
                // Create the gradient.
                int[] colors = {
                        Color.rgb(102, 225, 0), // green
                        Color.rgb(255, 247, 0),    // yellow
                        Color.rgb(255, 0, 0)   // red
                };

                float[] startPoints = {
                        0.1f, 0.4f, 07f
                };

                Gradient gradient = new Gradient(colors, startPoints);
                ArrayList<WeightedLatLng> items = HeatMapRSSIDeviceLocatorImpl.getWeightedHeatMap(trackedDevice);
                mProvider = new HeatmapTileProvider.Builder()
                        .weightedData(items)
                        .radius(50)
                        .gradient(gradient)
                        .build();
                // Add a tile overlay to the map, using the heat map tile provider.
                mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
            }else{
                /*{
                    double sizeCircle = Double.parseDouble( ((App) getApplicationContext()).getSizeHeatmap());
                    for(CoordinatorIoT.DeviceLocation d : trackedDevice){
                        points.add(drawCircle(
                                new LatLng(d.getLocation().getLatitude(), d.getLocation().getLongitude()),
                                new Double(d.getPower()/120).floatValue(),
                                sizeCircle
                        ));
                    }
                }*/
                {
                    {
                        double sizeCircle = Double.parseDouble( ((App) getApplicationContext()).getSizeHeatmap());
                        for(CoordinatorIoT.DeviceLocation d : trackedDevice){
                            float percentage = 0.0f;//new Double(d.getPower()/120).floatValue();
                            float power = new Double(-1 * (d.getPower()-120)).floatValue();
                            if(power< 40){
                                percentage = 1;
                            }else if(power > 70){
                                percentage = 0.1f;
                            }else{
                                percentage =  new Double(0.99 -(power-40)/30).floatValue();
                            }
                            points.add(drawCircle(
                                    new LatLng(d.getLocation().getLatitude(), d.getLocation().getLongitude()),
                                    percentage,
                                    sizeCircle
                            ));
                        }
                    }
                }
            }


        } catch (Exception e) {
            Toast.makeText(this, "Problem reading list of locations.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private float interpolate(float a, float b, float proportion) {
        return (a + ((b - a) * proportion));
    }

    ArrayList<Circle> points = new ArrayList<>();

    /** Returns an interpoloated color, between <code>a</code> and <code>b</code> */
    private int interpolateColor(int a, int b, float proportion) {
        float[] hsva = new float[3];
        float[] hsvb = new float[3];
        Color.colorToHSV(a, hsva);
        Color.colorToHSV(b, hsvb);
        for (int i = 0; i < 3; i++) {
            hsvb[i] = interpolate(hsva[i], hsvb[i], proportion);
        }
        return Color.HSVToColor(hsvb);
    }

    private Circle drawCircle(LatLng point, float value, double sizeCircle ){

        // Instantiating CircleOptions to draw a circle around the marker
        CircleOptions circleOptions = new CircleOptions()
                .center(point)
                .radius(sizeCircle) //.radius(10)
                .strokeColor(Color.LTGRAY)
                .fillColor(interpolateColor(Color.GREEN, Color.RED, value))
                .strokeWidth(2);
        // Adding the circle to the GoogleMap
        Circle circle = mMap.addCircle(circleOptions);
        return circle;
    }

    public LatLng setCurrentLocation(double lan, double lon){
        return setCurrentLocation(new LatLng(lan, lon));
    }

    public LatLng setCurrentLocation(LatLng location){
        if(marker == null) {
            marker = mMap.addMarker(new MarkerOptions().position(location).title("Marker in..."));
            lookAt(location, DEFAULT_ZOOM_LEVEL);
        }else
            marker.setPosition(location);
        return location;
    }

    public void lookAt(){
        lookAt(DEFAULT_ZOOM_LEVEL);
    }
    public void lookAt(float zoomLevel){
        if(marker != null)
           lookAt(marker.getPosition(), zoomLevel);
    }

    public void lookAt(final LatLng location, float zoomLevel){
        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel), 2000, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
            }

            @Override
            public void onCancel() {

            }
        });

    }

    /*Observer<List<DeviceIoT>> o = new Observer<List<DeviceIoT>>() {
        @Override
        public void onChanged(List<DeviceIoT> deviceIoTS) {
            Log.d("LiveData", "Updated devices..");
            updateTrackedDevice(deviceIoTS);
            refreshHeatMap();
        }
    };*/
    Observer<DeviceIoT> _o = new Observer<DeviceIoT>() {
        @Override
        public void onChanged(DeviceIoT deviceIoT) {
            Log.d("LiveData", "Updated device.. " + deviceIoT.getName());
            updateTrackedDevice();
            if(deviceIoT.getName().equals(trackedDevice)) {
                refreshHeatMap();
            }

        }
    };
    void refreshHeatMap(){
        RSSIDeviceLocator rssiDeviceLocator = ((App) getApplicationContext()).getRssiDeviceLocator();
        if(trackedDevice != null && rssiDeviceLocator != null){
            CoordinatorIoT coordinatorIoT = rssiDeviceLocator.getCoordinatorIoT();
            // Get single device
            List<CoordinatorIoT.DeviceLocation> lastLocations = coordinatorIoT.getTrackedDevice(this.trackedDevice);
            updateHeatMap(lastLocations != null ? lastLocations : new ArrayList<CoordinatorIoT.DeviceLocation>());
        }
    }

    void flyToDevice(String trackedDevice){
        RSSIDeviceLocator rssiDeviceLocator = ((App) getApplicationContext()).getRssiDeviceLocator();
        if(trackedDevice != null && rssiDeviceLocator != null){
            CoordinatorIoT coordinatorIoT = rssiDeviceLocator.getCoordinatorIoT();
            // Get single device
            List<CoordinatorIoT.DeviceLocation> lastLocations = coordinatorIoT.getTrackedDevice(trackedDevice);
            if(lastLocations != null && lastLocations.size() > 0){
                CoordinatorIoT.DeviceLocation deviceLocation = lastLocations.get(lastLocations.size()  - 1); // get LAST
                Location location = deviceLocation.getLocation();
                lookAt(new LatLng(location.getLatitude(), location.getLongitude()), 25);
            }
        }
    }

    Observer<LatLng> o2 = new Observer<LatLng>() {
        @Override
        public void onChanged(LatLng lastLocation) {
            Log.d("LiveData", "Updated lastLocation..");
            if(lastLocation != null && mMap != null) {
                setCurrentLocation(lastLocation);

            }
        }
    };

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        super.onAttachFragment(fragment);
        //MutableLiveData<List<DeviceIoT>> liveDeviceIoT = ((App) getApplicationContext()).getDeviceIoT();
        MutableLiveData<DeviceIoT> liveDeviceIoT = ((App) getApplicationContext()).getLastDeviceIoT();
        liveDeviceIoT.observe(this, _o);

        MutableLiveData<LatLng> lastLocation = ((App) getApplicationContext()).getLastLocation();
        lastLocation.observe(this, o2);
    }
}