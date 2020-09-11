package it.filippetti.safe.localizator.locator;

import android.location.Location;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import it.filippetti.safe.localizator.ServiceResultReceiver;
import it.filippetti.safe.localizator.SmartSetupService;
import it.filippetti.safe.localizator.model.DeviceIoT;

import static it.filippetti.safe.localizator.mqtt.MQTTService.SHOW_RESULT;

public class RSSIDeviceLocatorImpl implements RSSIDeviceLocator, ServiceResultReceiver.Receiver, LocationReceiver{
    Location lastKnowLocation;
    // TODO: to improve with cach look-up data structure
    private List<DeviceIoT> deviceList;

    public SmartSetupService getLocationProvider() {
        return locationProvider;
    }

    public void setLocationProvider(SmartSetupService locationProvider) {
        this.locationProvider = locationProvider;
    }

    private SmartSetupService locationProvider;

    public RSSIDeviceLocatorImpl() {
        this.deviceList = new ArrayList<>();
    }

    @Override
    public boolean addDevice(DeviceIoT deviceIoT) {
        DeviceIoT device = getDeviceIoT(deviceIoT.getName());
        if(device == null){
            return deviceList.add(deviceIoT);
        }
        return false;
    }

    @Override
    public void updateRSS(DeviceIoT deviceIoT, Double rssi) {
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
    public void onReceiveResult(int resultCode, Bundle resultData) {
        // New Data
        System.out.println("New IoT data!");
        switch (resultCode) {
            case SHOW_RESULT:
                if (resultData != null) {
                    //showData(resultData.getString("data"));
                    //TODO
                }
                break;
        }
    }



    @Override
    public void onNewLocation(Double lat, Double lon) {
        //lastKnowLocation = location;
        System.out.println("New Location!");
    }
}
