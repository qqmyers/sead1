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
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.dispatch.IsPreviewPending;
import edu.illinois.ncsa.mmdb.web.client.dispatch.IsPreviewPendingResult;
import edu.illinois.ncsa.mmdb.web.rest.RestServlet;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

public class IsPreviewPendingHandler implements ActionHandler<IsPreviewPending, IsPreviewPendingResult> {
    Log log = LogFactory.getLog(IsPreviewPendingHandler.class);

    @Override
    public IsPreviewPendingResult execute(IsPreviewPending arg0, ExecutionContext arg1) throws ActionException {
        String uri = arg0.getUri();
        IsPreviewPendingResult result = new IsPreviewPendingResult();
        // is it a collection?
        String badge = TupeloStore.getInstance().getBadge(uri);
        if (badge != null) {
            // check the pending state of the badge
            result.setReady(TupeloStore.getInstance().getPreviewUri(badge, GetPreviews.SMALL) != null);
            result.setPending(!RestServlet.shouldCache404(badge));
            log.debug("Got badge for collection: " + uri + "; pending=" + result.isPending()); // FIXME debug
        } else {
            // otherwise check this dataset's pending state
            result.setReady(TupeloStore.getInstance().getPreviewUri(uri, arg0.getSize()) != null);
            result.setPending(!RestServlet.shouldCache404(uri));
        }
        return result;
    }

    @Override
    public Class<IsPreviewPending> getActionType() {
        // TODO Auto-generated method stub
        return IsPreviewPending.class;
    }

    @Override
    public void rollback(IsPreviewPending arg0, IsPreviewPendingResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
