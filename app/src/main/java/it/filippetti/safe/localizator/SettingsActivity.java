package it.filippetti.safe.localizator;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.textfield.TextInputEditText;

import it.filippetti.safe.localizator.locator.RSSIDeviceLocatorImpl;
import it.filippetti.safe.localizator.mqtt.MQTTService;

public class SettingsActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TITLE_TAG = "settingsActivityTitle";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new HeaderFragment())
                    .commit();
        } else {
            setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
        }
        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    @Override
                    public void onBackStackChanged() {
                        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                            setTitle(R.string.title_activity_settings);
                        }
                    }
                });
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        View mqttbroketurl = findViewById(R.id.mqttbroketurl);
        if(mqttbroketurl != null){
            _setText(mqttbroketurl, ((App)getApplicationContext()).getMqttserverUri());
        }
        View mqttclientid = findViewById(R.id.mqttclientid);
        if(mqttclientid != null){
            _setText(mqttclientid, ((App)getApplicationContext()).getMqttclientId());
        }
        View mqtttopicurl = findViewById(R.id.mqtttopicurl);
        if(mqtttopicurl != null){
            _setText(mqtttopicurl, ((App)getApplicationContext()).getMqttsubscriptionTopic());
        }

        View mqttSave = findViewById(R.id.mqttsave);
        if(mqttSave != null){
            mqttSave.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    App app = (App) getApplicationContext();
                    app.setMqttserverUri(((TextInputEditText)findViewById(R.id.mqttbroketurl)).getText().toString());
                    app.setMqttclientId(((TextInputEditText)findViewById(R.id.mqttclientid)).getText().toString());
                    app.setMqttsubscriptionTopic(((TextInputEditText)findViewById(R.id.mqtttopicurl)).getText().toString());
                    System.out.println("Saved..");
                }
            });
        }
    }

    private void _setText(View view, String value){
        ((TextInputEditText)view).setText(value);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, getTitle());
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        }
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        // Instantiate the new Fragment
        final Bundle args = pref.getExtras();
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment());
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);
        // Replace the existing Fragment with the new Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings, fragment)
                .addToBackStack(null)
                .commit();
        setTitle(pref.getTitle());
        return true;
    }

    public static class HeaderFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.header_preferences, rootKey);
        }
    }

    public static class MessagesFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.messages_preferences, rootKey);
        }
    }

    public static class SyncFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.sync_preferences, rootKey);
        }
    }
}