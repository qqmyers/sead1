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
import org.sead.acr.common.utilities.Memoized;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Namespaces;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.ListNamedThingsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListUserMetadataFields;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListUserMetadataFieldsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.UserMetadataField;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;

public class ListUserMetadataFieldsHandler implements ActionHandler<ListUserMetadataFields, ListUserMetadataFieldsResult> {
    static Log                                            log             = LogFactory.getLog(ListUserMetadataFieldsHandler.class);

    private static Memoized<ListUserMetadataFieldsResult> editResultCache = null;
    private static Memoized<ListUserMetadataFieldsResult> viewResultCache = null;

    public static ListUserMetadataFieldsResult listUserMetadataFields(boolean isForEdit) {
        ListUserMetadataFieldsResult lumfr = null;
        log.trace("Listing fields, editable = : " + isForEdit);
        if (isForEdit) {
            if (editResultCache == null) {
                log.debug("Initializing field list, editable = : " + isForEdit);

                editResultCache = new Memoized<ListUserMetadataFieldsResult>() {
                    public ListUserMetadataFieldsResult computeValue() {
                        ListUserMetadataFieldsResult result = new ListUserMetadataFieldsResult();
                        ListNamedThingsResult r = new ListNamedThingsResult();
                        ListNamedThingsResult r_d = new ListNamedThingsResult();

                        try {
                            r.setThingNames(TupeloStore.getInstance().listThingsOfType(MMDB.USER_METADATA_FIELD, Rdfs.LABEL));
                            r_d.setThingNames(TupeloStore.getInstance().listThingsOfType(GetUserMetadataFieldsHandler.VIEW_METADATA, Rdfs.COMMENT));
                        } catch (OperatorException e) {
                            r = null;
                            r_d = null;
                        }
                        if (r == null) {
                            log.error("can't list plain metadata fields");
                        }
                        for (Map.Entry<String, String> entry : r.getThingNames().entrySet() ) {
                            log.trace("Found plain viewable umf :" + entry.getKey() + " : " + entry.getValue() + "desciption: " + r_d.getThingNames().get(entry.getKey()));
                            result.addField(new UserMetadataField(entry.getKey(), entry.getValue(), r_d.getThingNames().get(entry.getKey())));
                        }
                        // now run more queries to get user metadata fields of various other types
                        try {
                            addDatatypeProperties(result);
                        } catch (OperatorException e) {
                            log.error(e);
                        }
                        try {
                            addEnumeratedProperties(result);
                        } catch (OperatorException e) {
                            log.error(e);
                        }
                        try {
                            addNonEnumeratedProperties(result);
                        } catch (OperatorException e) {
                            log.error(e);
                        }
                        return result;
                    }
                };
                editResultCache.setTtl(60 * 60 * 1000); // 1hr
            }
            lumfr = editResultCache.getValue();
        }
        else {
            if (viewResultCache == null) {
                viewResultCache = new Memoized<ListUserMetadataFieldsResult>() {
                    public ListUserMetadataFieldsResult computeValue() {
                        ListUserMetadataFieldsResult result = new ListUserMetadataFieldsResult();
                        ListNamedThingsResult r = new ListNamedThingsResult();
                        ListNamedThingsResult r_d = new ListNamedThingsResult();
                        try {
                            r.setThingNames(TupeloStore.getInstance().listThingsOfType(GetUserMetadataFieldsHandler.VIEW_METADATA, Rdfs.LABEL));
                            r_d.setThingNames(TupeloStore.getInstance().listThingsOfType(GetUserMetadataFieldsHandler.VIEW_METADATA, Rdfs.COMMENT));
                        } catch (OperatorException e) {
                            r = null;
                            r_d = null;
                        }
                        if (r == null) {
                            log.error("can't list plain metadata fields");
                        }
                        for (Map.Entry<String, String> entry : r.getThingNames().entrySet() ) {

                            log.trace("Found plain viewable umf :" + entry.getKey() + " : " + entry.getValue() + "desciption: " + r_d.getThingNames().get(entry.getKey()));
                            result.addField(new UserMetadataField(entry.getKey(), entry.getValue(), r_d.getThingNames().get(entry.getKey())));
                        }
                        // now run more queries to get user metadata fields of various other types
                        try {
                            addDatatypeProperties(result);
                        } catch (OperatorException e) {
                            log.error(e);
                        }
                        try {
                            addEnumeratedProperties(result);
                        } catch (OperatorException e) {
                            log.error(e);
                        }
                        try {
                            addNonEnumeratedProperties(result);
                        } catch (OperatorException e) {
                            log.error(e);
                        }
                        return result;
                    }
                };
                viewResultCache.setTtl(60 * 60 * 1000); // 1hr
            }
            lumfr = viewResultCache.getValue();
        }
        return lumfr;
    }

    @Override
    public ListUserMetadataFieldsResult execute(ListUserMetadataFields arg0, ExecutionContext arg1) throws ActionException {
        return listUserMetadataFields(arg0.isForEdit());
    }

    private static void addNonEnumeratedProperties(ListUserMetadataFieldsResult result) throws OperatorException {
        Unifier u = new Unifier();
        u.setColumnNames("prop", "label", "clazz", "clazzLabel");
        u.addPattern("prop", Rdf.TYPE, owl("ObjectProperty"));
        u.addPattern("prop", Rdfs.LABEL, "label");
        u.addPattern("prop", Rdfs.RANGE, "clazz");
        u.addPattern("clazz", Rdf.TYPE, "clazz"); // this is the punning pattern for Individual <-> Class
        u.addPattern("clazz", Rdfs.LABEL, "clazzLabel"); // this is the punning pattern for Individual <-> Class
        TupeloStore.getInstance().getOntologyContext().perform(u);
        for (Tuple<Resource> row : u.getResult() ) {
            log.trace("Found class umf " + row.get(1));
            UserMetadataField umf = new UserMetadataField(row.get(0).getString(), row.get(1).getString());
            umf.setType(UserMetadataField.CLASS);
            umf.addToRange(row.get(2).getString(), row.get(3).getString());
            result.addField(umf);
        }
    }

    private static void addEnumeratedProperties(ListUserMetadataFieldsResult result) throws OperatorException {
        Unifier u = new Unifier();
        u.setColumnNames("prop", "label", "value", "valueLabel", "clazz");
        u.addPattern("prop", Rdf.TYPE, owl("ObjectProperty"));
        u.addPattern("prop", Rdfs.LABEL, "label");
        u.addPattern("prop", Rdfs.RANGE, "clazz");
        u.addPattern("value", Rdf.TYPE, "clazz");
        u.addPattern("value", Rdfs.LABEL, "valueLabel");
        TupeloStore.getInstance().getOntologyContext().perform(u);
        Map<Resource, UserMetadataField> fields = new HashMap<Resource, UserMetadataField>();
        for (Tuple<Resource> row : u.getResult() ) {
            if (!row.get(2).equals(row.get(4))) { // punned: skip
                UserMetadataField field = fields.get(row.get(0));
                if (field == null) {
                    log.trace("Found enumerated umf " + row.get(1));
                    field = new UserMetadataField(row.get(0).getString(), row.get(1).getString());
                    field.setType(UserMetadataField.ENUMERATED);
                    fields.put(row.get(0), field);
                }
                log.trace("Enumerated umf " + row.get(1) + " allows value " + row.get(3));
                field.addToRange(row.get(2).getString(), row.get(3).getString());
            }
        }
        for (UserMetadataField field : fields.values() ) {
            result.addField(field);
        }
    }

    public static Resource owl(String s) {
        return Resource.uriRef(Namespaces.owl(s));
    }

    private static void addDatatypeProperties(ListUserMetadataFieldsResult result) throws OperatorException {
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

    public static void resetCache() {
        editResultCache = null;
        viewResultCache = null;
    }

    public static void addViewablePredicate(String pred) {
        ListUserMetadataFieldsResult lmufr = listUserMetadataFields(false);
        if (!lmufr.getPredicates().contains(pred)) {
            viewResultCache.getValue().addField(new UserMetadataField(pred, pred));
            TripleWriter tw = new TripleWriter();
            UriRef u = Resource.uriRef(pred);
            tw.add(u, Rdf.TYPE, GetUserMetadataFieldsHandler.VIEW_METADATA); //$NON-NLS-1$
            String label = pred.replace('.', '_'); //'.' in labels is not valid in json (or just MongoDB/BSON?), so avoid generating problem labels

            tw.add(u, Rdfs.LABEL, label);
            try {
                TupeloStore.getInstance().getContext().perform(tw);
            } catch (OperatorException e) {
                log.error("Could not add new predicate as viewable user metadata: " + pred + " " + e.getMessage());
            }

        }

    }
}
