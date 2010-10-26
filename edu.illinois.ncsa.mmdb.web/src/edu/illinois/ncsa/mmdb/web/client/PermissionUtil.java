package edu.illinois.ncsa.mmdb.web.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.illinois.ncsa.mmdb.web.client.dispatch.HasPermission;
import edu.illinois.ncsa.mmdb.web.client.dispatch.HasPermissionResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.PermissionSetting;
import edu.uiuc.ncsa.cet.bean.rbac.medici.Permission;

public class PermissionUtil {
    DispatchAsync            dispatch;
    Map<Permission, Boolean> cache;

    public static abstract class PermissionCallback {
        public abstract void onAllowed(); // this one must be implemented

        public void onDenied() {
        }

        public void onFailure() {
        }
    }

    public static abstract class PermissionsCallback {
        public abstract void onPermissions(HasPermissionResult permissions);

        public void onFailure() {
        }
    }

    public PermissionUtil(DispatchAsync dispatchAsync) {
        dispatch = dispatchAsync;
        cache = new HashMap<Permission, Boolean>();
    }

    /**
     * Get all roles in the given permission setting, as a map of role uri ->
     * name
     * role name
     */
    public static Map<String, String> getRoles(Collection<PermissionSetting> settings) {
        Map<String, String> uriToName = new HashMap<String, String>();
        for (PermissionSetting setting : settings ) {
            uriToName.put(setting.getRoleUri(), setting.getRoleName());
        }
        return uriToName;
    }

    void doIf(boolean condition, PermissionCallback callback) {
        if (condition) {
            callback.onAllowed();
        } else {
            callback.onDenied();
        }
    }

    public void doIfAllowed(Permission p, PermissionCallback callback) {
        doIfAllowed(p, null, callback);
    }

    public void doIfAllowed(final Permission p, String objectUri, final PermissionCallback callback) {
        Boolean cached = cache.get(p);
        if (cached != null) {
            doIf(cached, callback);
        } else {
            dispatch.execute(new HasPermission(MMDB.getUsername(), objectUri, p), new AsyncCallback<HasPermissionResult>() {
                @Override
                public void onFailure(Throwable caught) {
                    callback.onFailure();
                }

                @Override
                public void onSuccess(HasPermissionResult result) {
                    cache.put(p, result.isPermitted());
                    doIf(result.isPermitted(), callback);
                }
            });
        }
    }

    public void withPermissions(String objectUri, final PermissionsCallback callback, Permission... permissions) {
        dispatch.execute(new HasPermission(MMDB.getUsername(), objectUri, permissions), new AsyncCallback<HasPermissionResult>() {
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure();
            }

            @Override
            public void onSuccess(HasPermissionResult result) {
                callback.onPermissions(result);
            }
        });
    }

    public void withPermissions(PermissionsCallback callback, Permission... permissions) {
        withPermissions(null, callback, permissions);
    }
}
