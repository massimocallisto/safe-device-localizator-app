package it.filippetti.safe.localizator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import it.filippetti.safe.localizator.locator.RSSIDeviceLocator;
import it.filippetti.safe.localizator.model.CoordinatorIoT;

public class MapsViewSettings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_view_settings);

        final View viewById = findViewById(R.id.text_cleardata);
        if(viewById != null){
            ((TextView)viewById).setText("Click to clear data");
        }

        Button buttonClearData = (Button)findViewById(R.id.button_cleardata);
        buttonClearData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RSSIDeviceLocator rssiDeviceLocator = ((App) getApplicationContext()).getRssiDeviceLocator();
                if(rssiDeviceLocator != null){
                    CoordinatorIoT coordinatorIoT = rssiDeviceLocator.getCoordinatorIoT();
                    if(coordinatorIoT != null) {
                        coordinatorIoT.clearData();
                        if(viewById != null){
                            ((TextView)viewById).setText("Cleared!");
                        }
                    }
                }
            }
        });
    }
}