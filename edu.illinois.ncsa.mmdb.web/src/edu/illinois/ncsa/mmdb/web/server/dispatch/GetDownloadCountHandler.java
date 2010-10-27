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

import java.util.HashSet;
import java.util.Set;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDownloadCount;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDownloadCountResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;

/**
 * Get license attached to a specific resource.
 * 
 * @author Rob Kooper
 * 
 */
public class GetDownloadCountHandler implements ActionHandler<GetDownloadCount, GetDownloadCountResult> {
    /** Commons logging **/
    private static Log log = LogFactory.getLog(GetDownloadCountHandler.class);

    @Override
    public GetDownloadCountResult execute(GetDownloadCount arg0, ExecutionContext arg1) throws ActionException {
        Resource dataset = Resource.uriRef(arg0.getResource());

        // get all download cases
        Unifier uf = new Unifier();
        uf.addPattern(dataset, MMDB.DOWNLOADED_BY, "download");
        uf.addPattern("download", Dc.CREATOR, "downloader");
        uf.setColumnNames("downloader");
        try {
            TupeloStore.getInstance().getContext().perform(uf);
        } catch (OperatorException e) {
            log.warn("Could not get download count for dataset.", e);
            throw (new ActionException("Could not get download count for dataset.", e));
        }
        int count = 0;
        Set<Resource> people = new HashSet<Resource>();
        for (Tuple<Resource> row : uf.getResult() ) {
            count++;
            people.add(row.get(0));
        }

        // done
        return new GetDownloadCountResult(count, people.size());
    }

    @Override
    public Class<GetDownloadCount> getActionType() {
        return GetDownloadCount.class;
    }

    @Override
    public void rollback(GetDownloadCount arg0, GetDownloadCountResult arg1, ExecutionContext arg2) throws ActionException {
    }

}
