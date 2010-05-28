/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010 , NCSA.  All rights reserved.
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

import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.ListUserMetadataFields;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListUserMetadataFieldsResult;
import edu.illinois.ncsa.mmdb.web.server.Memoized;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

public class ListUserMetadataFieldsHandler implements ActionHandler<ListUserMetadataFields, ListUserMetadataFieldsResult> {
    private static Log                           log = LogFactory.getLog(ListUserMetadataFieldsHandler.class);

    private static Memoized<Map<String, String>> cache;                                                       // key = uri, value = label

    public static Map<String, String> listUserMetadataFields() {
        if (cache == null) {
            cache = new Memoized<Map<String, String>>() {
                public Map<String, String> computeValue() {
                    // query for all the user metadata fields and their labels
                    Unifier u = new Unifier();
                    u.setColumnNames("field", "label");
                    u.addPattern("field", Rdf.TYPE, Cet.cet("userMetadataField"));
                    u.addPattern("field", Rdfs.LABEL, "label");
                    Map<String, String> result = new HashMap<String, String>();
                    try {
                        for (Tuple<Resource> row : TupeloStore.getInstance()
                                .unifyExcludeDeleted(u, "field") ) {
                            result.put(row.get(0).getString(), row.get(1).getString());
                        }
                    } catch (OperatorException e) {
                        log.error("query to fetch user metadata fields failed");
                        return null;
                    }
                    return result;
                }
            };
            cache.setTtl(60 * 1000); // 1min
        }
        return cache.getValue();
    }

    @Override
    public ListUserMetadataFieldsResult execute(ListUserMetadataFields arg0, ExecutionContext arg1) throws ActionException {
        Map<String, String> umf = listUserMetadataFields();
        if (umf == null) {
            throw new ActionException("query to fetch user metadata fields failed");
        } else {
            ListUserMetadataFieldsResult value = new ListUserMetadataFieldsResult();
            value.setFieldLabels(umf);
            return value;
        }
    }

    @Override
    public Class<ListUserMetadataFields> getActionType() {
        return ListUserMetadataFields.class;
    }

    @Override
    public void rollback(ListUserMetadataFields arg0, ListUserMetadataFieldsResult arg1, ExecutionContext arg2) throws ActionException {
    }

}
