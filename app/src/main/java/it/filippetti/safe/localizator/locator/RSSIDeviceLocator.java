package it.filippetti.safe.localizator.locator;

import java.util.List;

import it.filippetti.safe.localizator.model.DeviceIoT;

public interface RSSIDeviceLocator {
    boolean addDevice(DeviceIoT deviceIoT);
    void updateRSS(DeviceIoT deviceIoT, Double rssi);
    void updateLocation(DeviceIoT deviceIoT, Double lon, Double lat);
    void sortByRSSI();
    void sortByRSSI(boolean asc);
    DeviceIoT getDeviceIoT(String name);
    List<DeviceIoT> getAllDeviceIoT();

}
