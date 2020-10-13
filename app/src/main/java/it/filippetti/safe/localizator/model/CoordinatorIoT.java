package it.filippetti.safe.localizator.model;

import android.location.Location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoordinatorIoT extends DeviceIoT {
    private static final float MIN_CLOSED_METERS_DISTANCE = 1.0f;

    public CoordinatorIoT(){
        super();
        this.setName("coordinator");
        locationHistory = new HashMap<>();
    }

    public class DeviceLocation{
        double power;
        Location location;

        public DeviceLocation(double power, Location location) {
            this.power = power;
            this.location = location;
        }

        public double getPower() {
            return power;
        }

        public Location getLocation() {
            return location;
        }
    }

    public List<String> getTrackedDeviceNames(){
        return new ArrayList<>(locationHistory.keySet());
    }

    private Map<String, List<DeviceLocation>> locationHistory;

    public Map<String, List<DeviceLocation>> getLocationHistory() {
        return locationHistory;
    }

    public List<DeviceLocation> getTrackedDevice(DeviceIoT deviceIoT) {
        return getTrackedDevice(deviceIoT.getName());
    }

    public List<DeviceLocation> getTrackedDevice(String deviceName) {
        List<DeviceLocation> deviceLocations = locationHistory.get(deviceName);
        return deviceLocations != null
                ? deviceLocations : new ArrayList<DeviceLocation>();
    }

    public void trackDeviceLocation(DeviceIoT deviceIoT){
        List<DeviceLocation> deviceLocations = locationHistory.get(deviceIoT.getName());
        if(deviceLocations == null){
            deviceLocations = new ArrayList<>();
            locationHistory.put(deviceIoT.getName(), deviceLocations);
        }
        DeviceLocation d = getLocationByDistance(deviceLocations, deviceIoT.getLocation());
        if(d == null){
            deviceLocations.add(new DeviceLocation(deviceIoT.getPower(), deviceIoT.getLocation()));
        }else
            d.power = deviceIoT.getPower();
    }

    private DeviceLocation getLocationByDistance(List<DeviceLocation> deviceLocations, Location location){
        for(DeviceLocation d : deviceLocations){
            if(d.location.distanceTo(location) < MIN_CLOSED_METERS_DISTANCE){ // in meters
                return d;
            }
        }
        return null;
    }

    public void clearData(){
        this.locationHistory.clear();
    }

}
