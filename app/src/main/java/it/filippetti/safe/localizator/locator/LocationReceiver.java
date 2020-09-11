package it.filippetti.safe.localizator.locator;

import android.location.Location;

public interface LocationReceiver {
    void onNewLocation(Double lat, Double lon);
}
