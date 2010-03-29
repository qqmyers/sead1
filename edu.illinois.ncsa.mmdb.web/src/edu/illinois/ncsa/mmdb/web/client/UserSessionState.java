package edu.illinois.ncsa.mmdb.web.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Holds information about the current state of the user's session.
 * Non-persistent upon login/logout.
 * @author futrelle
 */
public class UserSessionState {
	Map<String,String> preferences;
	String username;
	String sessionKey;
	Set<String> selectedDatasets;
	
	public Map<String, String> getPreferences() {
		if(preferences == null) {
			preferences = new HashMap<String,String>();
			initializePreferences();
		}
		return preferences;
	}
	public void setPreferences(Map<String, String> preferences) {
		this.preferences = preferences;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getSessionKey() {
		return sessionKey;
	}
	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}
	public Set<String> getSelectedDatasets() {
		if(selectedDatasets == null) {
			selectedDatasets = new HashSet<String>();
		}
		return selectedDatasets;
	}
	public void setSelectedDatasets(Set<String> selectedDatasets) {
		this.selectedDatasets = selectedDatasets;
	}
	
	private void initializePreferences() {
		preferences.put(MMDB.DATASET_VIEW_TYPE_PREFERENCE, PagingDcThingView.GRID_VIEW_TYPE);
		preferences.put(MMDB.COLLECTION_VIEW_TYPE_PREFERENCE, PagingDcThingView.LIST_VIEW_TYPE);
	}
}
