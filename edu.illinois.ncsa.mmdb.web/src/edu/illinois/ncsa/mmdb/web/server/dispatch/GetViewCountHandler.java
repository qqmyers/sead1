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

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.ObjectResourceMapping;
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
    public static Resource MMDB_VIEWED = Cet.cet("mmdb/isViewedBy");

    /** Commons logging **/
    private static Log     log         = LogFactory.getLog(GetViewCountHandler.class);

    @Override
    public GetViewCountResult execute(GetViewCount arg0, ExecutionContext arg1) throws ActionException {
        Resource dataset = Resource.uriRef(arg0.getResource());
        Resource person = Resource.uriRef(arg0.getPerson());

        Unifier uf = new Unifier();
        uf.addPattern(dataset, Dc.CREATOR, "creator");
        uf.addPattern(dataset, MMDB_VIEWED, "viewed");
        uf.addPattern("viewed", Dc.CREATOR, "viewer");
        uf.setColumnNames("creator", "viewed", "viewer");
        try {
            TupeloStore.getInstance().getContext().perform(uf);
        } catch (OperatorException e) {
            log.warn("Could not get view count for dataset.", e);
            throw (new ActionException("Could not get view count for dataset.", e));
        }

        Resource viewed = null;
        int count = 0;
        Resource creator = null;
        for (Tuple<Resource> row : uf.getResult() ) {
            // Don't count creator.
            creator = row.get(0);
            if (!creator.equals(row.get(2))) {
                count++;
            }
            if (row.get(2).equals(person)) {
                viewed = row.get(1);
            }
        }

        // update the count
        TripleWriter tw = new TripleWriter();
        if (viewed == null) {
            if (!creator.equals(person)) {
                count++;
            }
            viewed = Resource.uriRef();
            tw.add(dataset, MMDB_VIEWED, viewed);
            tw.add(viewed, Dc.CREATOR, person);
            tw.add(viewed, Dc.DATE, new Date());
        } else {
            uf = new Unifier();
            uf.addPattern(viewed, Dc.DATE, "date");
            uf.setColumnNames("date");
            try {
                TupeloStore.getInstance().getContext().perform(uf);
            } catch (OperatorException e) {
                log.warn("Could not get view count for dataset.", e);
                throw (new ActionException("Could not get view count for dataset.", e));
            }
            Date last = new Date(new Date().getTime() - IGNORE);
            boolean skip = false;
            for (Tuple<Resource> row : uf.getResult() ) {
                Object o = ObjectResourceMapping.object(row.get(0));
                if (o instanceof Date) {
                    Date date = (Date) o;
                    if ((last == null) || (last.compareTo(date) < 0)) {
                        skip = true;
                        break;
                    }
                }
            }

            if (!skip) {
                tw.add(viewed, Dc.DATE, new Date());
            }
        }
        if (tw.getToAdd().size() > 0) {
            try {
                TupeloStore.getInstance().getContext().perform(tw);
            } catch (OperatorException e) {
                log.warn("Could not add a view to dataset.", e);
                throw (new ActionException("Could not add a view to dataset.", e));
            }
        }

        // done
        return new GetViewCountResult(count);
    }

    @Override
    public Class<GetViewCount> getActionType() {
        return GetViewCount.class;
    }

    @Override
    public void rollback(GetViewCount arg0, GetViewCountResult arg1, ExecutionContext arg2) throws ActionException {
    }

}
