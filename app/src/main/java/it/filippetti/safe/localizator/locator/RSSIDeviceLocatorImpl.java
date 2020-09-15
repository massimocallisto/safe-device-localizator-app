package it.filippetti.safe.localizator.locator;

import android.app.Application;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import it.filippetti.safe.localizator.App;
import it.filippetti.safe.localizator.SmartSetupService;
import it.filippetti.safe.localizator.model.DeviceIoT;

public class RSSIDeviceLocatorImpl implements RSSIDeviceLocator/*, ServiceResultReceiver.Receiver, LocationReceiver*/{
    Location lastKnowLocation;
    // TODO: to improve with cach look-up data structure
    private List<DeviceIoT> deviceList;
    private Application applicationContext;

    public SmartSetupService getLocationProvider() {
        return locationProvider;
    }

    public void setLocationProvider(SmartSetupService locationProvider) {
        this.locationProvider = locationProvider;
    }

    private SmartSetupService locationProvider;

    public RSSIDeviceLocatorImpl(Application application) {
        this.applicationContext = application;
        this.deviceList = new ArrayList<>();
    }

    /*@Override
    public boolean addDevice(DeviceIoT deviceIoT) {
        DeviceIoT device = getDeviceIoT(deviceIoT.getName());
        if(device == null){
            return deviceList.add(deviceIoT);
        }
        return false;
    }*/

    @Override
    public void addOrUpdateDevice(DeviceIoT deviceIoT) {
        Log.i("addOrUpdateDevice", "Add or update device " + deviceIoT.getName());
        Log.d("addOrUpdateDevice", deviceIoT.toString());

        for(int i = 0; i < deviceList.size(); i++){
            if(deviceList.get(i).getName().equals(deviceIoT.getName())){
                Log.d("addOrUpdateDevice", "Device " + deviceIoT.getName() + " found at index " + i);
                deviceList.set(i, deviceIoT);
                return;
            }
        }
        // not found, add new one
        Log.d("addOrUpdateDevice", "Device " + deviceIoT.getName() + " not found!");
        deviceList.add(deviceIoT);
    }

    @Override
    public void updateRSSI(DeviceIoT deviceIoT, Double rssi) {
        DeviceIoT device = getDeviceIoT(deviceIoT.getName());
        if(device != null){
            device.setPower(rssi);
        }
    }

    @Override
    public void updateLocation(DeviceIoT deviceIoT, Double lon, Double lat) {
        DeviceIoT device = getDeviceIoT(deviceIoT.getName());
        if(device != null){
            device.setLongitude(lon);
            device.setLatitude(lat);
        }
    }

    @Override
    public void sortByRSSI() {
        sortByRSSI(false);
    }

    @Override
    public void sortByRSSI(final boolean asc) {
        Collections.sort(deviceList, new Comparator<DeviceIoT>() {
            @Override
            public int compare(DeviceIoT o1, DeviceIoT o2) {
                return Double.compare(o1.getPower(), o2.getPower()) * (asc ? 1 : -1);
            }
        });
    }

    @Override
    public DeviceIoT getDeviceIoT(String name) {
        for(DeviceIoT d : deviceList)
            if(d.getName().equals(name))
                return d;
        return null;
    }

    @Override
    public List<DeviceIoT> getAllDeviceIoT() {
        return deviceList;
    }

    public void onNewMessage(String topic, String messageDevice) {
        // Convert to json
        try{
            JSONObject snapshot = new JSONObject(messageDevice);
            DeviceIoT deviceIoT = new DeviceIoT();
            deviceIoT.setName(snapshot.optString("ref", "unknown_device"));
            // Set device RSSI
            double power = Double.MIN_VALUE;
            JSONArray r = snapshot.optJSONArray("r");
            if(r != null){
                for(int i = 0; i < r.length(); i++){
                    JSONObject o = (JSONObject) r.get(i);
                    String s = o.optString("k", "unknown");
                    if(s.equalsIgnoreCase("rssi")){
                        double v = o.optDouble("v", Double.MIN_VALUE);
                    }
                }
            }
            if(power != Double.MIN_VALUE){
                deviceIoT.setPower(power);
                // also set location
            }

            // Location
            if(lastKnowLocation != null) {
                deviceIoT.setLatitude(lastKnowLocation.getLatitude());
                deviceIoT.setLongitude(lastKnowLocation.getLongitude());
            }
            if(deviceIoT.getPower() == Double.MIN_VALUE){
                Log.w("device_census", "Cannot commit device with " + "null RSSI");
                return;
            }
            if( lastKnowLocation == null){
                Log.w("device_census", "Cannot commit device with " + "null location");
                return;
            }
            // Census
            addOrUpdateDevice(deviceIoT);
            // notify
            ((App)this.applicationContext).updateDeviceIoT(getAllDeviceIoT());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onNewLocation(Location location) {
        if(location != null){
            Log.d("DeviceList:",
                String.format("Set new device location (Lat,Lon): \t(%.5f,%.5f)", location.getLatitude(), location.getLongitude()));
            lastKnowLocation = location;
            ((App)this.applicationContext).updateLocation(new LatLng(location.getLatitude(), location.getLongitude()));
        }
    }
}
