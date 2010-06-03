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
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Files;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.ListTable;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.TextFormatter;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SystemInfo;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SystemInfoResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

/**
 * Get license attached to a specific resource.
 * 
 * @author Rob Kooper
 * 
 */
public class SystemInfoHandler implements ActionHandler<SystemInfo, SystemInfoResult> {
    static private long             last       = 0;
    static private SystemInfoResult result     = null;
    static private long             HISTORESIS = 60 * 1000;                                 // 1 minute

    /** Commons logging **/
    private static Log              log        = LogFactory.getLog(SystemInfoHandler.class);

    @Override
    public SystemInfoResult execute(SystemInfo arg0, ExecutionContext arg1) throws ActionException {
        if (last + HISTORESIS > System.currentTimeMillis()) {
            return result;
        }

        SystemInfoResult info = new SystemInfoResult();

        Unifier uf = new Unifier();
        uf.addPattern("ds", Rdf.TYPE, Cet.DATASET);
        uf.addPattern("ds", Files.LENGTH, "size");
        uf.setColumnNames("ds", "size");
        long size = 0;
        int count = 0;
        try {
            ListTable<Resource> result = TupeloStore.getInstance().unifyExcludeDeleted(uf, "ds");
            for (Tuple<Resource> row : result ) {
                count++;
                long l = Long.parseLong(row.get(1).getString());
                if (l > 0) {
                    size += l;
                }
            }
        } catch (OperatorException e) {
            throw (new ActionException("Could not count datasets."));
        }
        info.add("Datasets", "" + count);
        info.add("Total Bytes", TextFormatter.humanBytes(size));

        // done
        result = info;
        last = System.currentTimeMillis();
        return info;
    }

    @Override
    public Class<SystemInfo> getActionType() {
        return SystemInfo.class;
    }

    @Override
    public void rollback(SystemInfo arg0, SystemInfoResult arg1, ExecutionContext arg2) throws ActionException {
    }
}
