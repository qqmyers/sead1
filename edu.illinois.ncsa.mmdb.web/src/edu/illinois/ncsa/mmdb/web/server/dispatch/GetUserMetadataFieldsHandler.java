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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Thing;
import org.tupeloproject.kernel.ThingSession;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserMetadataFields;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserMetadataFieldsResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;

/**
 * TODO Add comments
 * 
 * @author Joe Futrelle
 * 
 */
public class GetUserMetadataFieldsHandler implements
        ActionHandler<GetUserMetadataFields, GetUserMetadataFieldsResult> {

    /** Commons logging **/
    private static Log log = LogFactory
                                   .getLog(GetUserMetadataFieldsHandler.class);

    @Override
    public GetUserMetadataFieldsResult execute(GetUserMetadataFields arg0,
            ExecutionContext arg1) throws ActionException {

        try {
            Map<String, String> labels = new HashMap<String, String>();
            Map<String, Collection<String>> allValues = new HashMap<String, Collection<String>>();
            ThingSession ts = new ThingSession(TupeloStore.getInstance()
                    .getContext());
            Thing t = ts.fetchThing(Resource.uriRef(arg0.getUri()));
            for (Map.Entry<String, String> fl : getUserMetadataFields()
                    .entrySet() ) {
                List<String> values = new LinkedList<String>();
                for (Object value : t.getValues(Resource.uriRef(fl.getKey())) ) {
                    if (value instanceof Resource) {
                        values.add(((Resource) value).getString());
                    } else {
                        values.add(value.toString());
                    }
                }
                if (values.size() > 0) {
                    labels.put(fl.getKey(), fl.getValue()); // remember the label for this one
                    allValues.put(fl.getKey(), values);
                }
            }
            GetUserMetadataFieldsResult r = new GetUserMetadataFieldsResult();
            r.setFieldLabels(labels);
            r.setValues(allValues);
            ts.close();
            return r;
        } catch (Exception x) {
            log.error("Error getting metadata specified by user for "
                    + arg0.getUri());
            throw new ActionException("failed", x);
        }
    }

    /**
     * 
     * @return
     * @throws OperatorException
     */
    private Map<String, String> getUserMetadataFields()
            throws OperatorException {
        Unifier u = new Unifier();
        u.setColumnNames("field", "label");
        u.addPattern("field", Rdf.TYPE, MMDB.USER_METADATA_FIELD);
        u.addPattern("field", Rdfs.LABEL, "label");
        Map<String, String> result = new HashMap<String, String>();
        for (Tuple<Resource> row : TupeloStore.getInstance()
                .unifyExcludeDeleted(u, "field") ) {
            result.put(row.get(0).getString(), row.get(1).getString());
        }
        return result;
    }

    @Override
    public Class<GetUserMetadataFields> getActionType() {
        return GetUserMetadataFields.class;
    }

    @Override
    public void rollback(GetUserMetadataFields arg0,
            GetUserMetadataFieldsResult arg1, ExecutionContext arg2)
            throws ActionException {
        // TODO Auto-generated method stub

    }
}
