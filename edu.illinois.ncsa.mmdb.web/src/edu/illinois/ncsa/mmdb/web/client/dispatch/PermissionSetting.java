package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.io.Serializable;

import edu.uiuc.ncsa.cet.bean.rbac.medici.Permission;
import edu.uiuc.ncsa.cet.bean.rbac.medici.PermissionValue;

@SuppressWarnings("serial")
public class PermissionSetting implements Serializable {
    String          roleUri;
    Permission      permission;
    PermissionValue value;
    String          roleName;

    public PermissionSetting() {
    }

    public PermissionSetting(String roleUri, Permission permission, PermissionValue value) {
        this.roleUri = roleUri;
        this.permission = permission;
        this.value = value;
    }

    public PermissionSetting(String roleUri, Permission permission, PermissionValue value, String roleName) {
        this.roleUri = roleUri;
        this.permission = permission;
        this.value = value;
        this.roleName = roleName;
    }

    public String getRoleUri() {
        return roleUri;
    }

    public Permission getPermission() {
        return permission;
    }

    public PermissionValue getValue() {
        return value;
    }

    // optional
    public String getRoleName() {
        return roleName;
    }

    public void setRoleUri(String r) {
        roleUri = r;
    }

    public void setPermission(Permission p) {
        permission = p;
    }

    public void setValue(PermissionValue v) {
        value = v;
    }

    public void setRoleName(String r) {
        roleName = r;
    }
}
