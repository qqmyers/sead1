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
import org.tupeloproject.rdf.terms.Gis;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GeoSearch;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GeoSearchResult;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.server.SEADRbac;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

/**
 * Text base search of the repository.
 * 
 * @author Luigi Marini
 * 
 */
public class GeoSearchHandler implements ActionHandler<GeoSearch, GeoSearchResult> {
    final int RESULT_COUNT_LIMIT = 20;                                       // FIXME this is a hack, we need paging

    Log       log                = LogFactory.getLog(GeoSearchHandler.class);

    @Override
    public GeoSearchResult execute(GeoSearch arg0, ExecutionContext arg1)
            throws ActionException {
        GeoSearchResult searchResult = new GeoSearchResult();
        long then = System.currentTimeMillis();

        SEADRbac rbac = new SEADRbac(TupeloStore.getInstance().getContext());
        int userlevel = rbac.getUserAccessLevel(Resource.uriRef(arg0.getUser()));
        int defaultlevel = Integer.parseInt(TupeloStore.getInstance().getConfiguration(ConfigurationKey.AccessLevelDefault));
        String pred = TupeloStore.getInstance().getConfiguration(ConfigurationKey.AccessLevelPredicate);

        Unifier u = new Unifier();
        u.setColumnNames("d");
        u.addPattern("d", Gis.HAS_GEO_POINT, "p");
        u.addPattern("d", Rdf.TYPE, Cet.DATASET);
        u.addPattern("d", Resource.uriRef(pred), "access", true);
        try {
            for (Tuple<Resource> row : TupeloStore.getInstance().unifyExcludeDeleted(u, "d") ) {
                int datasetlevel = (row.get(1) != null) ? Integer.parseInt(row.get(1).getString()) : defaultlevel;
                if (datasetlevel < userlevel) {
                    continue;
                }
                searchResult.addHit(row.get(0).getString());
            }
        } catch (OperatorException e) {
            throw new ActionException(e);
        }

        long elapsed = System.currentTimeMillis() - then;
        log.debug("GeoSearch for '" + arg0.getQuery() + "' took " + elapsed + "ms" + " and returned " + searchResult.getHits().size() + " hits");
        return searchResult;
    }

    @Override
    public Class<GeoSearch> getActionType() {
        return GeoSearch.class;
    }

    @Override
    public void rollback(GeoSearch arg0, GeoSearchResult arg1, ExecutionContext arg2)
            throws ActionException {
        // TODO Auto-generated method stub

    }

}
