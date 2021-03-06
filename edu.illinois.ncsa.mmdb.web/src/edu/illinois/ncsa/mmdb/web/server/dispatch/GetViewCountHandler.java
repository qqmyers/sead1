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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetViewCount;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetViewCountResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

/**
 * Get license attached to a specific resource.
 * 
 * @author Rob Kooper
 * 
 */
public class GetViewCountHandler implements ActionHandler<GetViewCount, GetViewCountResult> {
    private static int     IGNORE      = 5 * 60 * 1000;                               // 5 minutes

    // FIXME move to MMDB
    public static Resource MMDB_VIEWED = Cet.cet("mmdb/isViewedBy");                  //$NON-NLS-1$

    /** Commons logging **/
    private static Log     log         = LogFactory.getLog(GetViewCountHandler.class);

    @Override
    public GetViewCountResult execute(GetViewCount arg0, ExecutionContext arg1) throws ActionException {
        Resource dataset = Resource.uriRef(arg0.getResource());
        Resource person = Resource.uriRef(arg0.getPerson());

        // get all view cases
        Unifier uf = new Unifier();
        uf.addPattern(dataset, MMDB_VIEWED, "viewed"); //$NON-NLS-1$
        uf.addPattern("viewed", Dc.CREATOR, "viewer"); //$NON-NLS-1$//$NON-NLS-2$
        uf.addPattern("viewed", Dc.DATE, "date"); //$NON-NLS-1$//$NON-NLS-2$
        uf.setColumnNames("viewed", "viewer", "date"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        try {
            TupeloStore.getInstance().getContext().perform(uf);
        } catch (OperatorException e) {
            log.warn("Could not get view count for dataset.", e);
            throw (new ActionException("Could not get view count for dataset.", e));
        }

        Date recent = new Date(new Date().getTime() - IGNORE);
        boolean isRecent = false;
        Set<Resource> users = new HashSet<Resource>();
        for (Tuple<Resource> row : uf.getResult() ) {
            users.add(row.get(1));
            if (row.get(1).equals(person)) {
                Date date = (Date) row.get(2).asObject();
                if (date.compareTo(recent) > 0) {
                    isRecent = true;
                }
            }
        }

        // update the count
        if (!isRecent) {
            TripleWriter tw = new TripleWriter();
            Resource viewed = Resource.uriRef();
            tw.add(dataset, MMDB_VIEWED, viewed);
            tw.add(viewed, Dc.CREATOR, person);
            tw.add(viewed, Dc.DATE, new Date());
            try {
                TupeloStore.getInstance().getContext().perform(tw);
            } catch (OperatorException e) {
                log.warn("Could not add a view to dataset.", e);
                throw (new ActionException("Could not add a view to dataset.", e));
            }
        }

        // done
        return new GetViewCountResult(users.size());
    }

    @Override
    public Class<GetViewCount> getActionType() {
        return GetViewCount.class;
    }

    @Override
    public void rollback(GetViewCount arg0, GetViewCountResult arg1, ExecutionContext arg2) throws ActionException {
    }

}
