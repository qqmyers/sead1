package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.customware.gwt.dispatch.shared.Result;

@SuppressWarnings("serial")
public class GetPermissionsResult implements Result {
    List<PermissionSetting>      settings;
    private Map<String, Integer> accessLevel;

    public GetPermissionsResult() {
    }

    public GetPermissionsResult(PermissionSetting... settings) {
        for (PermissionSetting s : settings ) {
            addSetting(s);
        }
    }

    public void addSetting(PermissionSetting setting) {
        getSettings().add(setting);
    }

    public List<PermissionSetting> getSettings() {
        if (settings == null) {
            settings = new LinkedList<PermissionSetting>();
        }
        return settings;
    }

    public Map<String, Integer> getAccessLevel() {
        if (accessLevel == null) {
            accessLevel = new HashMap<String, Integer>();
        }
        return accessLevel;
    }

    public void setAccessLevel(Map<String, Integer> accessLevel) {
        this.accessLevel = accessLevel;
    }
}
