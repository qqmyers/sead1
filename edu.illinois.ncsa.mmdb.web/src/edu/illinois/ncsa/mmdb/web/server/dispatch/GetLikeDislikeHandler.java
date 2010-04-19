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
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetLikeDislike;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetLikeDislikeResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

/**
 * Get like dislike information
 * 
 * @author Rob Kooper
 * 
 */
public class GetLikeDislikeHandler implements ActionHandler<GetLikeDislike, GetLikeDislikeResult> {
    // FIXME move to MMDB
    public static Resource MMDB_LIKE    = Cet.cet("mmdb/isLikedBy");
    public static Resource MMDB_DISLIKE = Cet.cet("mmdb/isDislikedBy");

    /** Commons logging **/
    private static Log     log          = LogFactory.getLog(GetLikeDislikeHandler.class);

    @Override
    public GetLikeDislikeResult execute(GetLikeDislike arg0, ExecutionContext arg1) throws ActionException {
        Resource dataset = Resource.uriRef(arg0.getResource());
        Resource person = Resource.uriRef(arg0.getPerson());

        // FIXME do we want to store the date the user viewed?
        Unifier uf = new Unifier();
        uf.addPattern(dataset, MMDB_LIKE, "like", true);
        uf.addPattern(dataset, MMDB_DISLIKE, "dislike", true);
        uf.setColumnNames("like", "dislike");
        try {
            TupeloStore.getInstance().getContext().perform(uf);
        } catch (OperatorException e) {
            log.warn("Could not get like/dislike count for dataset.", e);
            throw (new ActionException("Could not get like/dislike count for dataset.", e));
        }

        boolean like = false;
        int likeCount = 0;
        boolean dislike = false;
        int dislikeCount = 0;
        TripleWriter tw = new TripleWriter();
        for (Tuple<Resource> row : uf.getResult() ) {
            if (row.get(0) != null) {
                likeCount++;
                if (row.get(1).equals(person)) {
                    like = true;
                }
            }
            if (row.get(1) != null) {
                dislikeCount++;
                if (row.get(1).equals(person)) {
                    dislike = true;
                }
            }
        }

        // update the count
        switch (arg0.getState()) {
            case UNKNOWN:
                break;
            case NONE:
                if (like) {
                    tw.remove(dataset, MMDB_LIKE, person);
                    likeCount--;
                }
                if (dislike) {
                    tw.remove(dataset, MMDB_DISLIKE, person);
                    dislikeCount--;
                }
                like = false;
                dislike = false;
                break;
            case LIKE:
                if (dislike) {
                    tw.remove(dataset, MMDB_DISLIKE, person);
                    dislikeCount--;
                    tw.add(dataset, MMDB_LIKE, person);
                    likeCount++;
                }
                if (!like && !dislike) {
                    tw.add(dataset, MMDB_LIKE, person);
                    likeCount++;
                }
                like = true;
                dislike = false;
                break;
            case DISLIKE:
                if (like) {
                    tw.remove(dataset, MMDB_LIKE, person);
                    likeCount--;
                    tw.add(dataset, MMDB_DISLIKE, person);
                    dislikeCount++;
                }
                if (!like && !dislike) {
                    tw.add(dataset, MMDB_DISLIKE, person);
                    dislikeCount++;
                }
                like = false;
                dislike = true;
                break;
        }
        if ((tw.getToAdd().size() != 0) && (tw.getToRemove().size() != 0)) {
            try {
                TupeloStore.getInstance().getContext().perform(tw);
            } catch (OperatorException e) {
                log.warn("Could not update like/dislkie count for dataset.", e);
                throw (new ActionException("Could not update like/dislkie count for dataset.", e));
            }
        }

        // done
        GetLikeDislikeResult result = new GetLikeDislikeResult();
        result.setLike(like);
        result.setLikeCount(likeCount);
        result.setDislike(dislike);
        result.setDislikeCount(dislikeCount);

        // done
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
