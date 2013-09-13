package edu.illinois.ncsa.mmdb.web.common;

import java.util.EnumSet;

/**
 * Represents a permission in SEAD ACR (Medici + related apps). Permissions are
 * associated with roles;
 * everyone who is a member of that role has all the permissions of that role.
 * 
 * @author futrelle, jim
 * 
 */
public enum Permission {
    // old permissions
    /** Access content (non-login) pages in the web interface */
    VIEW_MEMBER_PAGES("View member pages", "ViewMemberPages"),
    /** Access administration pages in the web interface */
    VIEW_ADMIN_PAGES("View administrative pages", "ViewAdminPages"),
    /** Add/delete/rename roles, add/remove people from roles */
    EDIT_ROLES("Administer roles", "EditRoles"),
    /** Run a complete full-text indexing sweep (administrative action) */
    REINDEX_FULLTEXT("Rebuild full-text index", "ReindexFulltext"),
    /** Re-run extraction services (on a dataset) */
    RERUN_EXTRACTION("Rerun extraction", "RerunExtraction"),
    /** Upload datasets via the web interface or REST services */
    UPLOAD_DATA("Upload data", "UploadData"),
    /** Add/edit dataset metadata such as title */
    EDIT_METADATA("Edit metadata", "EditMetadata"),
    /** Add/edit user metadata fields */
    EDIT_USER_METADATA("Edit user metadata", "EditUserMetadata"),
    /** Delete datasets */
    DELETE_DATA("Delete data", "DeleteData"),
    /** Change licenses on datasets */
    CHANGE_LICENSE("Change license", "ChangeLicense"),
    /** Tag datasets */
    ADD_TAG("Add tags", "AddTag"),
    /** Delete tags on datasets */
    DELETE_TAG("Delete tags", "DeleteTag"),
    /** Add comments */
    ADD_COMMENT("Add comments", "AddComment"),
    /** Edit/delete comments */
    EDIT_COMMENT("Edit/delete comments", "EditComment"),
    /** Add relationships (between datsets) */
    ADD_RELATIONSHIP("Add relationships", "AddRelationship"),
    /** Delete relationships */
    DELETE_RELATIONSHIP("Delete relationships", "DeleteRelationship"),
    /** Add collections */
    ADD_COLLECTION("Add new collection", "AddCollection"),
    /** Delete collections */
    DELETE_COLLECTION("Delete collection", "DeleteCollection"),
    /** Edit collections */
    EDIT_COLLECTION("Edit collection", "EditCollection"),
    /** View data (e.g., access a dataset page in the web interface */
    VIEW_DATA("View data", "ViewData"),
    /** Download full datasets */
    DOWNLOAD("Download originals", "Download"),
    /** See geolocation information */
    VIEW_LOCATION("View geolocation", "ViewLocation"),
    /** See user activity (e.g., per-dataset) */
    VIEW_ACTIVITY("View user activity", "ViewActivity"),
    /** Access the Tupelo endpoint (Desktop users need this permission) */
    USE_DESKTOP("Use desktop client", "Desktop"),
    /**
     * Access the RestEasy services (needed for remote apps such as dashboard
     * and public repo interfaces)
     */
    USE_REMOTEAPI("Use remote api", "Remote");

    private final String label;
    private final String uri;

    static final String  PREFIX = "http://cet.ncsa.uiuc.edu/2007/mmdb/permission/";

    private Permission(String label, String id) {
        this.label = label;
        uri = PREFIX + id;
    }

    /**
     * The human-readable name of the permission
     * 
     * @return
     */
    public String getLabel() {
        return label;
    }

    /**
     * The URI of the permission
     * 
     * @return
     */
    public String getUri() {
        return uri;
    }

    /**
     * All permissions
     * 
     * @return
     */
    public static EnumSet<Permission> all() {
        return EnumSet.allOf(Permission.class);
    }
}