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
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Beans;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Files;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.ListTable;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.TextFormatter;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SystemInfo;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SystemInfoResult;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.illinois.ncsa.mmdb.web.server.ContextSetupListener;
import edu.illinois.ncsa.mmdb.web.server.SEADRbac;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;

/**
 * Get license attached to a specific resource.
 * 
 * @author Rob Kooper
 * 
 */
public class SystemInfoHandler implements ActionHandler<SystemInfo, SystemInfoResult> {
    static private long             UNSIGNED_INT = (long) Math.pow(2, 32);

    static private long             last         = 0;
    static private SystemInfoResult result       = null;
    static private boolean          pending      = false;
    static private long             HISTORESIS   = 60 * 1000;                                 // 1 minute

    /** Commons logging **/
    private static Log              log          = LogFactory.getLog(SystemInfoHandler.class);

    @Override
    public SystemInfoResult execute(SystemInfo arg0, ExecutionContext arg1) throws ActionException {
        long historesis = HISTORESIS;
        String bigdata = TupeloStore.getInstance().getConfiguration(ConfigurationKey.BigData);
        if (bigdata.equals("true")) {
            historesis *= 1440; //once per day
        }
        if (last + historesis > System.currentTimeMillis()) {
            return result;
        }
        //if bigdata, return old info and calc new if over time
        // First time, pending is false and we set a bacground task and return current result
        //Another request before it completes and we skip both the asynch and sync updates and again return the current result
        //Fixme - alert receiver that updates are pending?
        if (bigdata.equals("true") && (result != null) && (!pending)) {
            pending = true;
            ContextSetupListener.updateSysInfoInBackground();
        } else if (!pending) {
            updateInfo();
        }
        return result;
    }

    public static void updateInfo() throws ActionException {
        SystemInfoResult info = new SystemInfoResult();

        Unifier uf = new Unifier();
        uf.addPattern("ds", Rdf.TYPE, "type");
        uf.addPattern("ds", Files.LENGTH, "size");
        uf.setColumnNames("ds", "type", "size");
        long datasetSize = 0;
        long derivedSize = 0;
        int datasetCount = 0;
        try {
            for (Tuple<Resource> row : TupeloStore.getInstance().unifyExcludeDeleted(uf, "ds") ) {
                long size = 0;
                if (row.get(2) != null) {
                    size = Long.parseLong(row.get(2).getString());
                    if (size < -3) {
                        size += UNSIGNED_INT;
                    }
                }
                if (Cet.DATASET.equals(row.get(1))) {
                    datasetCount++;
                    datasetSize += size;
                } else if (Beans.STORAGE_TYPE_BEAN_ENTRY.equals(row.get(1))) {
                    // don't count double
                } else {
                    derivedSize += size;
                }
            }
        } catch (OperatorException e) {
            throw (new ActionException("Could not count datasets."));
        }
        info.add("Datasets", "" + datasetCount);
        info.add("Bytes from uploaded dataset", TextFormatter.humanBytes(datasetSize));
        info.add("Bytes from derived data", TextFormatter.humanBytes(derivedSize));
        info.add("Total number of bytes", TextFormatter.humanBytes(datasetSize + derivedSize));

        Unifier uf2 = new Unifier();
        log.debug("Counting Collections");
        uf2.addPattern("cl", Rdf.TYPE, Resource.uriRef("http://cet.ncsa.uiuc.edu/2007/Collection"));
        uf2.addPattern("parent", Resource.uriRef("http://purl.org/dc/terms/hasPart"), "cl", true);
        uf2.addPattern("cl", Resource.uriRef("http://purl.org/dc/terms/issued"), "date", true);
        uf2.setColumnNames("cl", "parent", "date");
        long collCount = 0;
        long preprintCollCount = 0;
        int publishedCollCount = 0;

        SEADRbac rbac = new SEADRbac(TupeloStore.getInstance().getContext());
        Resource anon = PersonBeanUtil.getAnonymousURI();

        try {
            for (Tuple<Resource> row : TupeloStore.getInstance().unifyExcludeDeleted(uf2, "cl") ) {
                collCount++;
                // row.get(1) is null if there's no parent, i.e. a top level collection that should be counted if anon can see it
                // row.get(2) is not null if the collection has been published, i.e. should be added to the published count
                // for now, the logic requires that a published collection be visible in the ACR to be counted but this could be improved
                // once we have our lifecycle worked out (would be odd if a collection were published and not visible to anon. 
                // Eventually we may need to query the VA to find published collections if all record of them is flushed from an ACR.)
                if ((row.get(1) == null) || (row.get(2) != null)) {
                    //check permissions
                    if (rbac.checkPermission(anon, row.get(1), Resource.uriRef(Permission.VIEW_MEMBER_PAGES.getUri()))) {
                        if (rbac.checkAccessLevel(anon, row.get(0))) {
                            if (row.get(1) == null) {
                                preprintCollCount++;
                            }

                            if (row.get(2) != null) {
                                publishedCollCount++;
                            }
                        }
                    }
                }
            }
        } catch (OperatorException e) {
            log.debug(e.getMessage());
            throw (new ActionException("Could not count collections."));
        } catch (RBACException re) {
            log.debug(re.getMessage());
            throw (new ActionException("Could not count collections."));
        }
        info.add("Collections ", "" + collCount);
        info.add("Public Preprint Collections", "" + preprintCollCount);
        info.add("Published Collections", "" + publishedCollCount);

        Unifier uf3 = new Unifier();
        log.debug("Counting Views");

        uf3.addPattern("thing1", Resource.uriRef("http://cet.ncsa.uiuc.edu/2007/mmdb/isViewedBy"), "thing2");
        uf3.setColumnNames("thing1");
        try {
            TupeloStore.getInstance().getContext().perform(uf3);

            info.add("Total Views", "" + ((ListTable<Resource>) uf3.getResult()).getRows().size());
        } catch (OperatorException e) {
            log.debug("Views: " + e.getMessage());
            throw (new ActionException("Could not count views."));
        }

        Unifier uf4 = new Unifier();
        log.debug("Counting People");

        uf4.addPattern("person", Resource.uriRef("http://xmlns.com/foaf/0.1/name"), "name");
        uf4.addPattern("person", Resource.uriRef("http://cet.ncsa.uiuc.edu/2007/role/hasRole"), "role");

        uf4.setColumnNames("person");
        try {
            TupeloStore.getInstance().getContext().perform(uf4);

            info.add("Number of Users", "" + ((ListTable<Resource>) uf4.getResult()).getRows().size());
        } catch (OperatorException e) {
            log.debug("Users: " + e.getMessage());
            throw (new ActionException("Could not count users."));
        }

        // done
        pending = false;
        result = info;
        last = System.currentTimeMillis();
    }

    @Override
    public Class<SystemInfo> getActionType() {
        return SystemInfo.class;
    }

    @Override
    public void rollback(SystemInfo arg0, SystemInfoResult arg1, ExecutionContext arg2) throws ActionException {
    }
}
