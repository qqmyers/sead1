package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.LinkedList;
import java.util.List;

import net.customware.gwt.dispatch.shared.Result;

@SuppressWarnings("serial")
public class GetPermissionsResult implements Result {
    List<PermissionSetting> settings;

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
}
