package it.filippetti.safe.localizator.locator;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.heatmaps.WeightedLatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import it.filippetti.safe.localizator.model.DeviceIoT;

public class HeatMapRSSIDeviceLocatorImpl extends RSSIDeviceLocatorImpl {
    public JSONArray getWeightedHeatMapAsJson(){
        JSONArray heatMapData = new JSONArray();
        for (DeviceIoT deviceIoT : getAllDeviceIoT())
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
    }

    public ArrayList<WeightedLatLng> getWeightedHeatMap(){
        ArrayList<WeightedLatLng> list = new ArrayList<>();
        for (DeviceIoT deviceIoT : getAllDeviceIoT()) {
            list.add(new WeightedLatLng(new LatLng(
                    deviceIoT.getLatitude(),
                    deviceIoT.getLongitude()),
                    Math.round(Math.abs(deviceIoT.getPower()))));
        }
        return list;
    }

    public void buildHeatMap(){
        // TODO
     }

}
