package it.filippetti.safe.localizator.locator;

import android.location.Location;

import java.util.List;

import it.filippetti.safe.localizator.model.CoordinatorIoT;
import it.filippetti.safe.localizator.model.DeviceIoT;

public interface RSSIDeviceLocator {
    //boolean addDevice(DeviceIoT deviceIoT);
    void addOrUpdateDevice(DeviceIoT deviceIoT);
    void updateRSSI(DeviceIoT deviceIoT, Double rssi);
    void updateLocation(DeviceIoT deviceIoT, Double lon, Double lat);
    void sortByRSSI();
    CoordinatorIoT getCoordinatorIoT();
    Location getLastLocation();
    void sortByRSSI(boolean asc);
    DeviceIoT getDeviceIoT(String name);
    List<DeviceIoT> getAllDeviceIoT();

}
