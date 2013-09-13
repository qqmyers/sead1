package edu.illinois.ncsa.mmdb.web.common;

import java.util.EnumSet;

/**
 * DefaultRole is an enum of roles that are provided as defaults in Medici.
 * Each role is associated with a set of permissions that are enabled for it by
 * default.
 * 
 * @author futrelle
 */
public enum DefaultRole {
    /**
     * Administrator, with permissions to administer other user's role
     * memberships and permissions
     */
    ADMINISTRATOR("Administrator", Permission.all()),
    /** Authors can create content as well as viewing it */
    AUTHOR("Author", author()),
    /**
     * The owner of an object has special privileges when operating on that
     * object
     */
    OWNER("Owner", owner()), // special role in Medici which a user automatically belongs to when taking action on an object they own
    /** Unauthenticated users are always in this role */
    ANONYMOUS("Anonymous", anonymous()), // special role in Medici for unauthenticated users
    /** Viewers can look at content but cannot create it */
    VIEWER("Viewer", viewer()),
    /** A reviewer can view content and comment on it / tag it / edit metadata */
    REVIEWER("Reviewer", reviewer());

    private String              name;
    public static final String  PREFIX = "http://medici.ncsa.illinois.edu/ns/";
    private EnumSet<Permission> permissions;                                   // all permissions which are allowed

    private DefaultRole(String name, EnumSet<Permission> permissions) {
        this.name = name;
        this.permissions = permissions;
    }

    /**
     * The human-readable name of the role
     * 
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * The role's URI
     * 
     * @return
     */
    public String getUri() {
        return PREFIX + getName();
    }

    /**
     * The permissions which are enabled by default for this role
     * 
     * @return
     */
    public EnumSet<Permission> getPermissions() {
        return permissions;
    }

    static EnumSet<Permission> author() {
        return EnumSet.of(Permission.ADD_COMMENT,
                Permission.ADD_RELATIONSHIP,
                Permission.ADD_TAG,
                Permission.ADD_COLLECTION,
                Permission.DELETE_COLLECTION,
                Permission.DELETE_RELATIONSHIP,
                Permission.DELETE_TAG,
                Permission.EDIT_COMMENT,
                Permission.EDIT_COLLECTION,
                Permission.EDIT_METADATA,
                Permission.EDIT_USER_METADATA,
                Permission.UPLOAD_DATA,
                Permission.USE_REMOTEAPI,
                Permission.VIEW_DATA,
                Permission.VIEW_MEMBER_PAGES
                );
    }

    static EnumSet<Permission> reviewer() {
        return EnumSet.of(Permission.ADD_COMMENT,
                Permission.ADD_RELATIONSHIP,
                Permission.ADD_TAG,
                Permission.ADD_COLLECTION,
                Permission.DELETE_COLLECTION,
                Permission.DELETE_RELATIONSHIP,
                Permission.DELETE_TAG,
                Permission.EDIT_COMMENT,
                Permission.EDIT_COLLECTION,
                Permission.EDIT_METADATA,
                Permission.EDIT_USER_METADATA,
                Permission.UPLOAD_DATA,
                Permission.VIEW_DATA,
                Permission.VIEW_MEMBER_PAGES);
    }

    static EnumSet<Permission> viewer() {
        return EnumSet.of(Permission.ADD_TAG, Permission.DELETE_TAG,
                Permission.VIEW_DATA, Permission.VIEW_MEMBER_PAGES);
    }

    static EnumSet<Permission> anonymous() {
        return EnumSet.noneOf(Permission.class);
    }

    static EnumSet<Permission> owner() {
        return EnumSet.of(Permission.ADD_COMMENT,
                Permission.ADD_RELATIONSHIP,
                Permission.ADD_TAG,
                Permission.ADD_COLLECTION,
                Permission.CHANGE_LICENSE,
                Permission.DELETE_COLLECTION,
                Permission.DELETE_DATA,
                Permission.DELETE_RELATIONSHIP,
                Permission.DELETE_TAG,
                Permission.DOWNLOAD,
                Permission.EDIT_COLLECTION,
                Permission.EDIT_COMMENT,
                Permission.EDIT_METADATA,
                Permission.EDIT_USER_METADATA,
                Permission.UPLOAD_DATA,
                Permission.USE_REMOTEAPI,
                Permission.VIEW_DATA,
                Permission.VIEW_MEMBER_PAGES);
    }

    // these are special roles where membership is not controlled by
    // an administrator but is computed based on the authentication state, operation, and object(s) the operation applies to.
    // 

    /**
     * Special roles are roles whose membership is not controlled by an
     * administrator but is computed based on the authentication state,
     * operation, and object(s) the operation applies to. e.g., owner, which
     * depends on whether the authenticated user owns the object being operated
     * on, and anonymous, which unauthenticated users belong to.
     */
    public static EnumSet<DefaultRole> special() {
        return EnumSet.of(DefaultRole.OWNER, DefaultRole.ANONYMOUS);
    }
}