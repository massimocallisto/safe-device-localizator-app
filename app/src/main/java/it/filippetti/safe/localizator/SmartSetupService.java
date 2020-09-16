package it.filippetti.safe.localizator;

import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.util.Log;

import java.util.Date;


public class SmartSetupService extends Service {

    //private LocalBroadcastManager localBroadcast;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private ResultReceiver locationReceiver;

    public SmartSetupService() { }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        Log.d("SmartSetupService", "onCreate");
        //localBroadcast = LocalBroadcastManager.getInstance(this);
        startAcquiringLocation();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("ionStartCommand SmartSetup");
        if(intent != null){
            locationReceiver  = intent.getParcelableExtra("location_receiver");
            //startAcquiringLocation();
        }
         return super.onStartCommand(intent, flags, startId);

    }

    private void startAcquiringLocation(){
        Log.d("SmartSetupService", "startAcquiringLocation");
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        try {
            locationListener = new InternalLocationListener();
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);  //requestLocationUpdates(0, 5, criteria, pi);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);  //requestLocationUpdates(0, 5, criteria, pi);
            ((App)getApplicationContext()).updateLocalizationStatus("Localized listener started at " + new Date().toString());
        } catch (SecurityException ignored) {
            System.err.println("Error loading location info handler");
            //sendMessage("Cannot acquire location", null);
            ((App)getApplicationContext()).updateLocalizationStatus("Localization error: " + ignored.getMessage());
        }
    }


    private void stopAcquiringLocation(){
        Log.d("SmartSetupService", "stopAcquiringLocation");
        if(locationManager != null){
            try{
                locationManager.removeUpdates( locationListener );
            }catch(SecurityException ignored){
                ignored.printStackTrace();
            }
            finally {
                locationListener = null;
                locationManager = null;
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d("SmartSetupService", "onDestroy");
        stopAcquiringLocation();
        super.onDestroy();
    }

    public void updateLocation(Location location){
        ((App)getApplicationContext()).updateLocalizationStatus("Localized at " + new Date().toString());
        if(location != null && locationReceiver != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("location", location);
            //bundle.putString("lon_lat",String.format("Showing From JobIntent Service %d", 100));
            locationReceiver.send(0, bundle);
        }
        //    locationReceiver.onNewLocation(location);
    }

    /*private void sendLocation(Location location){
        Log.d("SmartSetupService", "sendLocation");
        Intent intent = new Intent("LocationUpdate");
        if(location != null) {
            intent.putExtra("location", location);
        }
        localBroadcast.sendBroadcast(intent);
    }*/

   /* private void sendMessage(String txt, Location location){
        Intent intent = new Intent("displayMessage");
        intent.putExtra("message", txt);
        if(location != null){
            intent.putExtra("location", location);
        }
        localBroadcast.sendBroadcast(intent);
    }*/

    private class InternalLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            updateLocation( location );
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
           // startAcquiringLocation();
        }

        @Override
        public void onProviderDisabled(String provider) {
           // stopAcquiringLocation();
           // sendLocation( null );
        }
    }
}
