package edu.illinois.ncsa.medici.handheld.android;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Setup the initial values
        setSummaryString("server", "");
        setSummaryString("username", "");

        // Set up a listener whenever a key changes            
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister the listener whenever a key changes            
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);    
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("server")) {        	        	
        	setSummaryString("server", "");
        } else if (key.equals("username")) {
        	setSummaryString("username", "");
        }
    }
    
    private void setSummaryString(String key, String defValue) {
    	SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
    	Preference preference = getPreferenceScreen().findPreference(key);
    	preference.setSummary(sharedPreferences.getString(key, defValue));
    }
}