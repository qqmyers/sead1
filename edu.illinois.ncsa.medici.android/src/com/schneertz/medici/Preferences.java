package com.schneertz.medici;

import com.schneertz.medici.R;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;

public class Preferences extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        
        for(String key : new String[] { "medici", "username" }) {
        	findPreference(key).setSummary(PreferenceManager.getDefaultSharedPreferences(this).getString(key, ""));
        
        	findPreference(key).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
        		public boolean onPreferenceChange(Preference pref, Object arg1) {
        			pref.setSummary(arg1+"");
        			return true;
        		}
        	});
        }
    }
}
