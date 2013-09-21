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
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetAccessLevel;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetAccessLevelResult;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

/**
 * Get annotations attached to a specific resource sorted by date.
 * 
 * @author Luigi Marini
 * 
 */
public class GetAccessLevelHandler implements ActionHandler<GetAccessLevel, GetAccessLevelResult> {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(GetAccessLevelHandler.class);

    @Override
    public GetAccessLevelResult execute(GetAccessLevel arg0, ExecutionContext arg1) throws ActionException {
        return getResult(arg0.getUri());
    }

    @Override
    public Class<GetAccessLevel> getActionType() {
        return GetAccessLevel.class;
    }

    @Override
    public void rollback(GetAccessLevel arg0, GetAccessLevelResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub
    }

    public static GetAccessLevelResult getResult(String uri) throws ActionException {
        GetAccessLevelResult result = new GetAccessLevelResult();

        result.setDefaultLevel(Integer.parseInt(TupeloStore.getInstance().getConfiguration(ConfigurationKey.AccessLevelDefault)));
        result.setMinLevel(Integer.parseInt(TupeloStore.getInstance().getConfiguration(ConfigurationKey.AccessLevelMin)));
        result.setMaxLevel(Integer.parseInt(TupeloStore.getInstance().getConfiguration(ConfigurationKey.AccessLevelMax)));
        result.setPredicate(TupeloStore.getInstance().getConfiguration(ConfigurationKey.AccessLevelPredicate));
        result.setLabel(TupeloStore.getInstance().getConfiguration(ConfigurationKey.AccessLevelLabel));

        if (uri != null) {
            result.setDatasetLevel(result.getDefaultLevel());

            try {
                if ((result.getPredicate() != null) && (result.getPredicate().trim().length() > 0)) {
                    for (Triple t : TupeloStore.getInstance().getContext().match(Resource.uriRef(uri), Resource.uriRef(result.getPredicate()), null) ) {
                        result.setDatasetLevel(Integer.parseInt(t.getObject().toString()));
                    }
                }
            } catch (Exception exc) {
                log.warn("Could not get access level of dataset.", exc);
            }
        }
        return result;
    }
}
