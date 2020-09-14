package it.filippetti.safe.localizator;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
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
import it.filippetti.safe.localizator.model.DeviceIoT;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

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

    Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("MNaps creation start...");

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        LatLng location = setCurrentLocation(0, 0);
        lookAt(location);
        //LatLng sydney = new LatLng(0, 0);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in ?"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        //addHeatMap();*)
    }

    private void updateHeatMap(List<DeviceIoT> deviceIoTS) {
        System.out.println("addHeatMap!!");
        //List<LatLng> list = null;
        ArrayList<WeightedLatLng> data = new ArrayList<WeightedLatLng>();


        // Get the data: latitude/longitude positions of police stations.
        /*try {
            // TODO: list of rssi
            list = null;//readItems(R.raw.police_stations);
        } catch (JSONException e) {
            Toast.makeText(this, "Problem reading list of locations.", Toast.LENGTH_LONG).show();
        }*/

        // Create a heat map tile provider, passing it the latlngs of the police stations.
        try {
            // TODO: list of rssi
            ArrayList<WeightedLatLng> items = HeatMapRSSIDeviceLocatorImpl.getWeightedHeatMap(deviceIoTS);
            //list = null;//readItems(R.raw.police_stations);
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

    private ArrayList<WeightedLatLng> readItems(List<DeviceIoT> deviceIoTS) throws JSONException {
        //RSSIDeviceLocator rssiDeviceLocator = ((App) getApplication()).getRssiDeviceLocator();
        //HeatMapRSSIDeviceLocatorImpl heatMapRSSIDeviceLocator = new HeatMapRSSIDeviceLocatorImpl(rssiDeviceLocator);
        //return heatMapRSSIDeviceLocator.getWeightedHeatMap();
        return null;
//
//
//        ArrayList<WeightedLatLng> list = new ArrayList<>();
//        String js ="[{\"lat\":-37.1886,\"lng\":145.708,\"wgt\":20},{\"lat\":-37.8361,\"lng\":144.845,\"wgt\":25},{\"lat\":-38.4034,\"lng\":144.192,\"wgt\":2},{\"lat\":-38.7597,\"lng\":143.67,\"wgt\":2},{\"lat\":-36.9672,\"lng\":141.083,\"wgt\":57}]";
//
//        /*InputStream inputStream = getResources().openRawResource(resource);
//        String json = new Scanner(inputStream).useDelimiter("\\A").next();*/
//        JSONArray array = new JSONArray(js);
//        for (int i = 0; i < array.length(); i++) {
//            JSONObject object = array.getJSONObject(i);
//            double lat = object.getDouble("lat");
//            double lng = object.getDouble("lng");
//            int intensity = object.getInt("wgt");
//            list.add(new WeightedLatLng(new LatLng(lat, lng), intensity));
//        }
//        System.out.println("Got coordinates " + js);
//        return list;
    }

    // Declaring it


    public LatLng setCurrentLocation(double lan, double lon){
        return setCurrentLocation(new LatLng(lan, lon));
    }

    public LatLng setCurrentLocation(LatLng location){
        if(marker == null)
            marker = mMap.addMarker(new MarkerOptions().position(location).title("Marker in..."));
        else
            marker.setPosition(location);
        return location;
    }

    public void lookAt(LatLng location){
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
    }

    Observer<List<DeviceIoT>> o = new Observer<List<DeviceIoT>>() {
        @Override
        public void onChanged(List<DeviceIoT> deviceIoTS) {
            Log.d("LiveData", "Updated devices..");
            updateHeatMap(deviceIoTS);
        }
    };

    Observer<LatLng> o2 = new Observer<LatLng>() {
        @Override
        public void onChanged(LatLng lastLocation) {
            Log.d("LiveData", "Updated lastLocation..");
            if(lastLocation != null && mMap != null){
                setCurrentLocation(lastLocation);
                lookAt(lastLocation);
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