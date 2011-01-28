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

import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Namespaces;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.ListNamedThingsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListUserMetadataFields;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListUserMetadataFieldsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.UserMetadataField;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;

public class ListUserMetadataFieldsHandler extends ListNamedThingsHandler implements ActionHandler<ListUserMetadataFields, ListUserMetadataFieldsResult> {
    @Override
    public ListUserMetadataFieldsResult execute(ListUserMetadataFields arg0, ExecutionContext arg1) throws ActionException {
        ListUserMetadataFieldsResult result = new ListUserMetadataFieldsResult();
        ListNamedThingsResult r = listNamedThings(MMDB.USER_METADATA_FIELD, Rdfs.LABEL);
        if (r == null) {
            throw new ActionException("query to fetch user metadata fields failed");
        }
        for (Map.Entry<String, String> entry : r.getThingNames().entrySet() ) {
            result.addField(new UserMetadataField(entry.getKey(), entry.getValue()));
        }
        // now run more queries to get user metadata fields of various other types
        try {
            addDatatypeProperties(result);
        } catch (OperatorException e) {
            throw new ActionException(e);
        }
        try {
            addEnumeratedProperties(result);
        } catch (OperatorException e) {
            throw new ActionException(e);
        }
        try {
            addNonEnumeratedProperties(result);
        } catch (OperatorException e) {
            throw new ActionException(e);
        }
        return result;
    }

    // FIXME this is not the correct way to represent "non-enumerated" properties in OWL
    private void addNonEnumeratedProperties(ListUserMetadataFieldsResult result) throws OperatorException {
        Unifier u = new Unifier();
        u.setColumnNames("prop", "label", "value");
        u.addPattern("prop", Rdf.TYPE, owl("ObjectProperty"));
        u.addPattern("prop", Rdfs.LABEL, "label");
        u.addPattern("prop", Rdfs.RANGE, "clazz");
        u.addPattern("value", Rdf.TYPE, "clazz", true);
        TupeloStore.getInstance().getOntologyContext().perform(u);
        for (Tuple<Resource> row : u.getResult() ) {
            if (row.get(2) == null) {
                UserMetadataField umf = new UserMetadataField(row.get(0).getString(), row.get(1).getString());
                umf.setType(UserMetadataField.CLASS);
                result.addField(umf);
            }
        }
    }

    private void addEnumeratedProperties(ListUserMetadataFieldsResult result) throws OperatorException {
        Unifier u = new Unifier();
        u.setColumnNames("prop", "label", "value", "valueLabel");
        u.addPattern("prop", Rdf.TYPE, owl("ObjectProperty"));
        u.addPattern("prop", Rdfs.LABEL, "label");
        u.addPattern("prop", Rdfs.RANGE, "clazz");
        u.addPattern("value", Rdf.TYPE, "clazz");
        u.addPattern("value", Rdfs.LABEL, "valueLabel");
        TupeloStore.getInstance().getOntologyContext().perform(u);
        Map<Resource, UserMetadataField> fields = new HashMap<Resource, UserMetadataField>();
        for (Tuple<Resource> row : u.getResult() ) {
            UserMetadataField field = fields.get(row.get(0));
            if (field == null) {
                field = new UserMetadataField(row.get(0).getString(), row.get(1).getString());
                field.setType(UserMetadataField.ENUMERATED);
                fields.put(row.get(0), field);
            }
            field.addToRange(row.get(2).getString(), row.get(3).getString());
        }
        for (UserMetadataField field : fields.values() ) {
            result.addField(field);
        }
    }

    public static Resource owl(String s) {
        return Resource.uriRef(Namespaces.owl(s));
    }

    private void addDatatypeProperties(ListUserMetadataFieldsResult result) throws OperatorException {
        // find all datatype properties
        Unifier u = new Unifier();
        u.setColumnNames("prop", "label");
        u.addPattern("prop", Rdf.TYPE, owl("DatatypeProperty"));
        u.addPattern("prop", Rdfs.LABEL, "label");
        TupeloStore.getInstance().getOntologyContext().perform(u);
        for (Tuple<Resource> row : u.getResult() ) {
            UserMetadataField field = new UserMetadataField();
            field.setUri(row.get(0).getString());
            field.setLabel(row.get(1).getString());
            result.addField(field);
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
