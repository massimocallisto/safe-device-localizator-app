package it.filippetti.safe.localizator.locator;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.util.ArrayList;
import java.util.List;

import it.filippetti.safe.localizator.model.DeviceIoT;

public class HeatMapRSSIDeviceLocatorImpl {
    //private final RSSIDeviceLocator rssiDeviceLocator;

    /*public HeatMapRSSIDeviceLocatorImpl(RSSIDeviceLocator rssiDeviceLocator) {
        this.rssiDeviceLocator = rssiDeviceLocator;
    }*/

    /*public JSONArray getWeightedHeatMapAsJson(){
        JSONArray heatMapData = new JSONArray();
        for (DeviceIoT deviceIoT : rssiDeviceLocator.getAllDeviceIoT())
        {
            JSONObject o = new JSONObject();
            try {
                o.put("lat", deviceIoT.getLatitude());
                o.put("lon", deviceIoT.getLongitude());
                o.put("wgt", Math.round(Math.abs(deviceIoT.getPower())));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return heatMapData;
    }*/

    public static ArrayList<WeightedLatLng> getWeightedHeatMap(List<DeviceIoT> deviceIoTList){
        ArrayList<WeightedLatLng> list = new ArrayList<>();
        for (DeviceIoT deviceIoT : deviceIoTList) {
            list.add(new WeightedLatLng(new LatLng(
                    deviceIoT.getLatitude(),
                    deviceIoT.getLongitude()),
                    Math.round(Math.abs(deviceIoT.getPower()))));
        }
        return list;
    }
}
