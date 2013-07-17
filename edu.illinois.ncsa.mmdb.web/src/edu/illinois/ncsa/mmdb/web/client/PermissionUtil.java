package edu.illinois.ncsa.mmdb.web.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * Get all roles in the given permission setting, as an ordered list
     * role name
     */
    public static List<PermissionSetting> getRoles(Collection<PermissionSetting> settings) {
        List<PermissionSetting> roles = new ArrayList<PermissionSetting>();
        Set<String> uris = new HashSet<String>();
        for (PermissionSetting role : settings ) {
            if (!uris.contains(role.getRoleUri())) {
                uris.add(role.getRoleUri());
                roles.add(role);
            }
        }
        Collections.sort(roles, new Comparator<PermissionSetting>() {
            @Override
            public int compare(PermissionSetting o1, PermissionSetting o2) {
                if (o1 == null) {
                    return +1;
                }
                if (o2 == null) {
                    return -1;
                }
                if (o1.getRoleName().equals(o2.getRoleName())) {
                    return o1.getRoleUri().compareTo(o2.getRoleUri());
                } else {
                    return o1.getRoleName().compareTo(o2.getRoleName());
                }
            }
        });
        return roles;
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
        doIfAllowed(p, objectUri, callback, MMDB.getUsername());
    }

    public void doIfAllowed(final Permission p, String objectUri, final PermissionCallback callback, String username) {
        Boolean cached = cache.get(p);
        if (cached != null) {
            doIf(cached, callback);
        } else {
            dispatch.execute(new HasPermission(username, objectUri, p), new AsyncCallback<HasPermissionResult>() {
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
