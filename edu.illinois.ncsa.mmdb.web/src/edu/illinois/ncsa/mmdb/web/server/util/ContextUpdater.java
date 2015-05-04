package edu.illinois.ncsa.mmdb.web.server.util;

/*******************************************************************************
 * University of Michigan
 * Open Source License
 *
 * Copyright (c) 2013, University of Michigan.  All rights reserved.
 *
 * Developed by:
 * http://sead-data.net
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

import java.util.Date;

import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.Bid;
import org.tupeloproject.kernel.ContentStoreContext;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Transformer;
import org.tupeloproject.kernel.TripleMatcher;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPermissionsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.PermissionSetting;
import edu.illinois.ncsa.mmdb.web.common.DefaultRole;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.illinois.ncsa.mmdb.web.server.SEADRbac;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetPermissionsHandler;
import edu.uiuc.ncsa.cet.bean.rbac.medici.PermissionValue;
import edu.uiuc.ncsa.cet.bean.tupelo.context.ContextConvert;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBAC;

/**
 * This class updates contexts for SEAD, calling the original ContextConvert for
 * conversions up to version 7.
 *
 * @author myersjd@umich.edu
 *
 */
public class ContextUpdater {
    private static Log           log                 = LogFactory.getLog(ContextUpdater.class);

    public static final Resource CONTEXT_VERSION_URI = Resource.uriRef("tag:cet.ncsa.uiuc.edu,2008:/context/version"); //$NON-NLS-1$
    public static final int      CONTEXT_VERSION_NO  = 8;

    /**
     * This will update the context to version CONTEXT_VERSION_NO. If the
     * version of the context is larger than CONTEXT_VERSION_NO it will print an
     * warning message to the log.
     *
     * This will check the version number of the context and call the
     * appropriate functions to convert the context to the most up to date
     * context. This can be a potential expensive operation!
     *
     * @param context
     *            the context and child contexts to be updated.
     * @throws OperatorException
     *             throws OperatorException if there is an error updating the
     *             context.
     */
    public static void updateContext(Context context) throws OperatorException {
        updateContext(context, false);
    }

    /**
     * This will update the context to version CONTEXT_VERSION_NO. If the
     * version of the context is larger than CONTEXT_VERSION_NO it will print an
     * warning message to the log.
     *
     * This will check the version number of the context and call the
     * appropriate functions to convert the context to the most up to date
     * context. This can be a potential expensive operation!
     *
     * @param context
     *            the context and child contexts to be updated.
     * @param force
     *            the context will be forced to upgrade
     * @throws OperatorException
     *             throws OperatorException if there is an error updating the
     *             context.
     */
    public static void updateContext(Context context, boolean force) throws OperatorException {
        int version = ContextConvert.getVersionNumber(context);
        log.info("Current version of the context is " + version);
        if (!force && (version >= CONTEXT_VERSION_NO)) {
            return;
        }

        // Add new conversions here, always add the conversion at the end,
        // always make sure you do a less than so it can convert for example
        // from version 1 to version 5.

        //Updates for versions <7 are called from ContextConvert
        if (force || (version < 7)) {
            ContextConvert.updateContext(context);
        }

        if (force || (version < 8)) {
            removeNullUsers(context);
            addPermissionsToDefaultRoles(context);
            fixLabelClash(context);
            flattenPermissions(context);
        }

        // Mark context as updated removing all old versions first.
        TripleWriter tw = new TripleWriter();
        TripleMatcher tm = new TripleMatcher();
        tm.setSubject(CONTEXT_VERSION_URI);
        tm.setPredicate(Cet.HAS_VERSION_NUMBER);
        context.perform(tm);
        for (Triple t : tm.getResult() ) {
            tw.remove(t);
        }
        tw.add(Triple.create(CONTEXT_VERSION_URI, Cet.HAS_VERSION_NUMBER, CONTEXT_VERSION_NO));
        tw.add(Triple.create(CONTEXT_VERSION_URI, Dc.DATE, new Date()));
        updateVersion(context, tw);

        log.info("Context is updated to version " + CONTEXT_VERSION_NO);
    }

    /**
     * Unchanged from ContextConvert (where this method is private)
     * Change version in each context. This will try and write the new version
     * number to each context it finds along the way.
     *
     * @param context
     *            the context (and its children) that needs updating.
     * @param tw
     *            the context information that needs updating.
     */
    private static void updateVersion(Context context, TripleWriter tw) {
        // special case for ContentStoreContext since it does not have children
        if (context instanceof ContentStoreContext) {
            context = ((ContentStoreContext) context).getMetadataContext();
        }

        // some contexts can not do TripleWriter
        try {
            if (context.propose(tw) == Bid.ACCEPT) {
                context.perform(tw);
            }
        } catch (OperatorException e) {
            log.warn("Could not update context.", e);
        }

        for (Context c : context.getChildren() ) {
            updateVersion(c, tw);
        }
    }

    /**
     * Bugs have allowed creation of a user accounts with null/empty emails or
     * that duplicate newer anonymous/admin account entries
     *
     * http://cet.ncsa.uiuc.edu/2007/person/null
     * http://cet.ncsa.uiuc.edu/2007/person/ - Anonymous
     * http://cet.ncsa.uiuc.edu/2007/person/admin - admin
     *
     * . This removes these users.
     *
     * @param context
     *            the context (and its children) that needs updating.
     * @throws OperatorException
     */
    private static void removeNullUsers(Context context) throws OperatorException {
        TripleMatcher tm = new TripleMatcher();
        tm.setSubject(Resource.uriRef("http://cet.ncsa.uiuc.edu/2007/person/null"));
        context.perform(tm);
        TripleWriter tw = new TripleWriter();
        for (Triple t : tm.getResult() ) {
            tw.remove(t);
            log.debug("Removed triple " + t.toString());
        }
        context.perform(tw);
        log.info("Removed user with URI http://cet.ncsa.uiuc.edu/2007/person/null");
        tm = new TripleMatcher();
        tm.setSubject(Resource.uriRef("http://cet.ncsa.uiuc.edu/2007/person/"));
        context.perform(tm);
        tw = new TripleWriter();
        for (Triple t : tm.getResult() ) {
            tw.remove(t);
            log.debug("Removed triple " + t.toString());
        }
        context.perform(tw);
        log.info("Removed user with URI http://cet.ncsa.uiuc.edu/2007/person/");
        tm = new TripleMatcher();
        tm.setSubject(Resource.uriRef("http://cet.ncsa.uiuc.edu/2007/person/admin"));
        context.perform(tm);
        tw = new TripleWriter();
        for (Triple t : tm.getResult() ) {
            tw.remove(t);
            log.debug("Removed triple " + t.toString());
        }
        context.perform(tw);
        log.info("Removed user with URI http://cet.ncsa.uiuc.edu/2007/person/admin");
    }

    private static void fixLabelClash(Context context) throws OperatorException {
        UriRef idRef = Resource.uriRef("http://purl.org/dc/terms/identifier");
        TripleWriter tWriter = new TripleWriter();
        tWriter.remove(new Triple(idRef, Rdfs.LABEL, "Identifier"));
        tWriter.add(new Triple(idRef, Rdfs.LABEL, "External Identifier"));
        context.perform(tWriter);
        log.info("Updated label for DCterms/identifier");
    }

    /**
     * A bug allowed creation of a user with uri
     * http://cet.ncsa.uiuc.edu/2007/person/null. This removes that user.
     *
     * @param context
     *            the context (and its children) that needs updating.
     * @throws OperatorException
     */
    private static void addPermissionsToDefaultRoles(Context context) throws OperatorException {
        SEADRbac rbac = new SEADRbac(context);
        UriRef admin = Resource.uriRef(DefaultRole.ADMINISTRATOR.getUri());
        UriRef author = Resource.uriRef(DefaultRole.AUTHOR.getUri());
        UriRef viewer = Resource.uriRef(DefaultRole.VIEWER.getUri());
        UriRef reviewer = Resource.uriRef(DefaultRole.REVIEWER.getUri());
        UriRef anonymous = Resource.uriRef(DefaultRole.ANONYMOUS.getUri());
        UriRef owner = Resource.uriRef(DefaultRole.OWNER.getUri());

        UriRef sysinfoRefPermission = Resource.uriRef(Permission.VIEW_SYSTEM.getUri());
        UriRef viewPublishedPermission = Resource.uriRef(Permission.VIEW_PUBLISHED.getUri());
        UriRef manageMetadataPermission = Resource.uriRef(Permission.MANAGE_METADATA.getUri());

        rbac.setPermissionValue(admin, sysinfoRefPermission, RBAC.ALLOW);
        rbac.setPermissionValue(author, sysinfoRefPermission, RBAC.ALLOW);
        rbac.setPermissionValue(viewer, sysinfoRefPermission, RBAC.ALLOW);
        rbac.setPermissionValue(reviewer, sysinfoRefPermission, RBAC.ALLOW);
        rbac.setPermissionValue(anonymous, sysinfoRefPermission, RBAC.ALLOW);
        rbac.setPermissionValue(owner, sysinfoRefPermission, RBAC.ALLOW);

        rbac.setPermissionValue(admin, viewPublishedPermission, RBAC.ALLOW);
        rbac.setPermissionValue(author, viewPublishedPermission, RBAC.ALLOW);
        rbac.setPermissionValue(viewer, viewPublishedPermission, RBAC.ALLOW);
        rbac.setPermissionValue(reviewer, viewPublishedPermission, RBAC.ALLOW);
        rbac.setPermissionValue(anonymous, viewPublishedPermission, RBAC.ALLOW);
        rbac.setPermissionValue(owner, viewPublishedPermission, RBAC.ALLOW);

        rbac.setPermissionValue(admin, manageMetadataPermission, RBAC.ALLOW);

        log.info("Updated default roles with new permissions");
    }

    private static void flattenPermissions(Context c) throws OperatorException {
        SEADRbac rbac = new SEADRbac(c);
        //Get current role/permission assignments (flatten through GetPermissionsHandler)
        try {
            GetPermissionsResult gpr = new GetPermissionsHandler().execute(null, null);
            for (PermissionSetting p : gpr.getSettings() ) {
                log.warn("Role: " + p.getRoleName() + " has " + p.getValue().getName() + " for " + p.getPermission().getLabel());
            }
            //Remove role assignments
            // first, pick up the trash
            Transformer t = new Transformer();
            t.addInPattern("role", RBAC.HAS_PERMISSION_VALUE, "pv");
            t.addInPattern("role", Rdf.TYPE, RBAC.ROLE);
            t.addOutPattern("role", RBAC.HAS_PERMISSION_VALUE, "pv");
            c.perform(t);
            c.removeTriples(t.getResult());

            //Create unique permissionValue set (one per permission)
            for (Permission p : Permission.values() ) {
                log.warn("Removing old values for permission " + p.getLabel());
                rbac.deletePermission(Resource.uriRef(p.getUri())); //removes many permissionValues
                log.warn("Adding unique values for permission " + p.getLabel());
                rbac.createPermission(Resource.uriRef(p.getUri()), p.getLabel()); //adds one set of permission Values
            }

            //Set role/permission assignments on unique permissions.
            for (PermissionSetting p : gpr.getSettings() ) {
                log.warn("Setting Role: " + p.getRoleName() + " to have " + p.getValue().getName() + " for " + p.getPermission().getLabel());
                rbac.setPermissionValue(Resource.uriRef(p.getRoleUri()), Resource.uriRef(p.getPermission().getUri()), getUriRefForPermissionValue(p.getValue()));
            }
            log.warn("Permissions Flattened");
        } catch (ActionException a) {
            throw new OperatorException("Failed to retrieve Role/Permission settings");
        }
    }

    private static Resource getUriRefForPermissionValue(PermissionValue pv) {
        if (pv.equals(PermissionValue.ALLOW)) {
            return RBAC.ALLOW;
        } else if (pv.equals(PermissionValue.DO_NOT_ALLOW)) {
            return RBAC.DO_NOT_ALLOW;
        } else if (pv.equals(PermissionValue.DENY)) {
            return RBAC.DENY;
        }
        return null;
    }

}
