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
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetLikeDislike;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetLikeDislikeResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetLikeDislike.LikeDislike;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

/**
 * Get like dislike information
 * 
 * @author Rob Kooper
 * 
 */
public class GetLikeDislikeHandler implements ActionHandler<GetLikeDislike, GetLikeDislikeResult> {
    // FIXME move to MMDB
    public static Resource MMDB_LIKE_DISLIKE       = Cet.cet("mmdb/isLikedDislikedBy");
    public static Resource MMDB_LIKE_DISLIKE_STATE = Cet.cet("mmdb/likedDislikedState");

    /** Commons logging **/
    private static Log     log                     = LogFactory.getLog(GetLikeDislikeHandler.class);

    @Override
    public GetLikeDislikeResult execute(GetLikeDislike arg0, ExecutionContext arg1) throws ActionException {
        Resource dataset = Resource.uriRef(arg0.getResource());
        Resource person = Resource.uriRef(arg0.getPerson());

        Unifier uf = new Unifier();
        uf.addPattern(dataset, MMDB_LIKE_DISLIKE, "like");
        uf.addPattern("like", Dc.CREATOR, "likee");
        uf.addPattern("like", MMDB_LIKE_DISLIKE_STATE, "state");
        uf.addPattern("like", Dc.DATE, "date");
        uf.setColumnNames("like", "likee", "state", "date");
        try {
            TupeloStore.getInstance().getContext().perform(uf);
        } catch (OperatorException e) {
            log.warn("Could not get like/dislike count for dataset.", e);
            throw (new ActionException("Could not get like/dislike count for dataset.", e));
        }

        LikeDislike user = LikeDislike.UNKNOWN;
        int likeCount = 0;
        int dislikeCount = 0;
        TripleWriter tw = new TripleWriter();
        for (Tuple<Resource> row : uf.getResult() ) {
            if (row.get(1).equals(person) && (arg0.getState() != LikeDislike.UNKNOWN)) {
                // remove everything
                tw.remove(dataset, MMDB_LIKE_DISLIKE, row.get(0));
                tw.remove(row.get(0), Dc.CREATOR, row.get(1));
                tw.remove(row.get(0), MMDB_LIKE_DISLIKE_STATE, row.get(2));
                tw.remove(row.get(0), Dc.DATE, row.get(3));
            } else {
                LikeDislike cur = LikeDislike.valueOf(row.get(2).getString());
                if (row.get(1).equals(person)) {
                    user = cur;
                }
                if (cur == LikeDislike.LIKE) {
                    likeCount++;
                }
                if (cur == LikeDislike.DISLIKE) {
                    dislikeCount++;
                }
            }
        }

        if (arg0.getState() == LikeDislike.LIKE) {
            Resource uri = Resource.uriRef();
            tw.add(dataset, MMDB_LIKE_DISLIKE, uri);
            tw.add(uri, Dc.CREATOR, person);
            tw.add(uri, MMDB_LIKE_DISLIKE_STATE, arg0.getState().toString());
            tw.add(uri, Dc.DATE, new Date());
            user = arg0.getState();
            likeCount++;
        }
        if (arg0.getState() == LikeDislike.DISLIKE) {
            Resource uri = Resource.uriRef();
            tw.add(dataset, MMDB_LIKE_DISLIKE, uri);
            tw.add(uri, Dc.CREATOR, person);
            tw.add(uri, MMDB_LIKE_DISLIKE_STATE, arg0.getState().toString());
            tw.add(uri, Dc.DATE, new Date());
            user = arg0.getState();
            dislikeCount++;
        }
        if ((tw.getToAdd().size() != 0) || (tw.getToRemove().size() != 0)) {
            try {
                TupeloStore.getInstance().getContext().perform(tw);
            } catch (OperatorException e) {
                log.warn("Could not update like/dislike count for dataset.", e);
                throw (new ActionException("Could not update like/dislike count for dataset.", e));
            }
        }

        // done
        GetLikeDislikeResult result = new GetLikeDislikeResult();
        result.setState(user);
        result.setLikeCount(likeCount);
        result.setDislikeCount(dislikeCount);
        return result;
    }

    @Override
    public Class<GetLikeDislike> getActionType() {
        return GetLikeDislike.class;
    }

    @Override
    public void rollback(GetLikeDislike arg0, GetLikeDislikeResult arg1, ExecutionContext arg2) throws ActionException {
    }

}
