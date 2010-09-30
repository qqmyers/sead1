/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010, NCSA.  All rights reserved.
 *
 * Developed by:
 * Cyberenvironments and Technologies (CET)
 * http://cet.ncsa.illinois.edu/
 *
 * National Center for Supercomputing Applications (NCSA)
 * http://www.ncsa.illinois.edu/
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the names of CET, University of Illinois/NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *******************************************************************************/
/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client;

/**
 * @author lmarini
 * 
 */
public class Permissions {
    public enum Permission {
        // old permissions
        VIEW_MEMBER_PAGES("View member pages", "ViewMemberPages"),
        VIEW_ADMIN_PAGES("View administrative pages", "ViewAdminPages"),
        // administration
        EDIT_ROLES("Administer roles", "EditRoles"),
        REINDEX_FULLTEXT("Rebuild full-text index", "ReindexFulltext"),
        RERUN_EXTRACTION("Rerun extraction", "RerunExtraction"),
        // authoring
        UPLOAD_DATA("Upload data", "UploadData"),
        EDIT_METADATA("Edit metadata", "EditMetadata"),
        EDIT_USER_METADATA("Edit user metadata", "EditUserMetadata"),
        DELETE_DATA("Delete data", "DeleteData"),
        CHANGE_LICENSE("Change license", "ChangeLicense"),
        // review
        ADD_TAG("Add tags", "AddTag"),
        DELETE_TAG("Delete tags", "DeleteTag"),
        ADD_COMMENT("Add comments", "AddComment"),
        EDIT_COMMENT("Edit/delete comments", "EditComment"),
        ADD_RELATIONSHIP("Add relationships", "AddRelationship"),
        DELETE_RELATIONSHIP("Delete relationships", "DeleteRelationship"),
        // access
        VIEW_DATA("View data", "ViewData"),
        DOWNLOAD("Download originals", "Download");

        private final String label;
        private final String uri;

        static final String  PREFIX = "http://cet.ncsa.uiuc.edu/2007/mmdb/permission/";

        private Permission(String label, String id) {
            this.label = label;
            uri = PREFIX + id;
        }

        public String getLabel() {
            return label;
        }

        public String getUri() {
            return uri;
        }
    }
}
