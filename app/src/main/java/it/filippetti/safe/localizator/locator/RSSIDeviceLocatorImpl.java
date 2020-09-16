package it.filippetti.safe.localizator.locator;

import android.app.Application;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import it.filippetti.safe.localizator.App;
import it.filippetti.safe.localizator.SmartSetupService;
import it.filippetti.safe.localizator.model.CoordinatorIoT;
import it.filippetti.safe.localizator.model.DeviceIoT;

public class RSSIDeviceLocatorImpl implements RSSIDeviceLocator/*, ServiceResultReceiver.Receiver, LocationReceiver*/{
    Location lastKnowLocation;
    // TODO: to improve with cach look-up data structure
    private List<DeviceIoT> deviceList;
    private Application applicationContext;
    private CoordinatorIoT coordinatorIoT;

    @Override
    public Location getLastLocation() {
        return lastKnowLocation;
    }

    public void setLastKnowLocation(Location lastKnowLocation) {
        this.lastKnowLocation = lastKnowLocation;
    }

    public RSSIDeviceLocatorImpl(Application application) {
        this.applicationContext = application;
        this.deviceList = new ArrayList<>();
        this.coordinatorIoT = new CoordinatorIoT();
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

     @Override
    public CoordinatorIoT getCoordinatorIoT() {
        return coordinatorIoT;
    }

    public void setCoordinatorIoT(CoordinatorIoT coordinatorIoT) {
        this.coordinatorIoT = coordinatorIoT;
    }

    public void onNewMessage(String topic, String messageDevice) {
        // Convert to json
        try{
            JSONObject snapshot = new JSONObject(messageDevice);
            DeviceIoT deviceIoT = new DeviceIoT();
            deviceIoT.setName(snapshot.optString("ref", "unknown_device"));
            // Set device RSSI
            double power = getPower(snapshot);
            boolean hasPower = setPower(deviceIoT, power);
            boolean hasLocation = setLocation(deviceIoT, lastKnowLocation);
            if(hasPower && hasLocation) {
                // Census
                addOrUpdateDevice(deviceIoT);
                coordinatorIoT.trackDeviceLocation(deviceIoT);
                // notify
                //((App)this.applicationContext).updateDeviceIoT(getAllDeviceIoT());
                ((App)this.applicationContext).updateLastDeviceIoT(deviceIoT);
            }else{
                Log.w("device_census", !hasPower ?
                        "Cannot commit device with " + "null RSSI" :
                            "Cannot commit device with " + "null location");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private double getPower(JSONObject snapshot) throws JSONException {
        double power = Double.MIN_VALUE;
        JSONArray r = snapshot.optJSONArray("r");
        if(r != null){
            for(int i = 0; i < r.length(); i++){
                JSONObject o = (JSONObject) r.get(i);
                String s = o.optString("k", "unknown");
                if(s.equalsIgnoreCase("rssi")){
                    power = o.optDouble("v", Double.MIN_VALUE);
                }
            }
        }
        return power;
    }

    private boolean setPower(DeviceIoT deviceIoT, double power) {
        if(power != Double.MIN_VALUE){
            deviceIoT.setPower(power + 120); // normalize in order to have only positive values..
            return true;
        }
        return false;
    }

    private boolean setLocation(DeviceIoT deviceIoT, Location location) {
        if(location != null) {
            deviceIoT.setLatitude(location.getLatitude());
            deviceIoT.setLongitude(location.getLongitude());
            deviceIoT.setLocation(location);
            return true;
        }
        return false;
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
