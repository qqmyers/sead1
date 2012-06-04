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

import edu.illinois.ncsa.mmdb.web.client.dispatch.ExtractionService;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ExtractionServiceResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;

/**
 * Retrieve a specific dataset.
 * 
 * @author Luigi Marini
 * 
 */
public class ExtractionServiceHandler implements ActionHandler<ExtractionService, ExtractionServiceResult> {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(EditRoleHandler.class);

    @Override
    public ExtractionServiceResult execute(ExtractionService action, ExecutionContext arg1) throws ActionException {
        if (action.getUri() != null) {
            TupeloStore.getInstance().removeCachedPreview(action.getUri(), GetPreviews.SMALL);
            return new ExtractionServiceResult(TupeloStore.getInstance().extractPreviews(action.getUri(), action.getDelete()));
        } else {
            try {
                // get the collection of all IDs (URL) and convert them into String[]
                String[] ids = new DatasetBeanUtil(TupeloStore.getInstance().getBeanSession()).getIDs().toArray(new String[0]);
                for (int i = 0; i < ids.length; i++ ) {
                    // rerun extraction on all data
                    TupeloStore.getInstance().removeCachedPreview(ids[i], GetPreviews.SMALL);
                    TupeloStore.getInstance().extractPreviews(ids[i], action.getDelete());
                }
            } catch (Throwable thr) {
                log.warn("Unable to retrieve IDs of all databeans");
            }
            return new ExtractionServiceResult("OK");
        }
    }

    @Override
    public Class<ExtractionService> getActionType() {
        return ExtractionService.class;
    }

    @Override
    public void rollback(ExtractionService arg0, ExtractionServiceResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
