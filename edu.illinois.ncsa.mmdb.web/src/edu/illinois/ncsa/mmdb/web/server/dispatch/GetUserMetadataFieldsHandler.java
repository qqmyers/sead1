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
import java.util.Map;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Thing;
import org.tupeloproject.kernel.ThingSession;
import org.tupeloproject.kernel.TripleMatcher;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserMetadataFields;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserMetadataFieldsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.UserMetadataField;
import edu.illinois.ncsa.mmdb.web.client.dispatch.UserMetadataValue;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
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
    private static Log            log           = LogFactory
                                                        .getLog(GetUserMetadataFieldsHandler.class);

    public static final Resource  VIEW_METADATA = Resource.uriRef("http://sead-data.net/terms/acr/Viewable_Metadata");

    ListUserMetadataFieldsHandler umfHelper     = new ListUserMetadataFieldsHandler();

    private String nameOf(Resource uri) throws OperatorException {
        log.debug("Name of: " + uri.toString());
        Unifier u = new Unifier();
        u.setColumnNames("label", "title" /*, "type" */);
        u.addPattern(uri, Rdfs.LABEL, "label", true);
        u.addPattern(uri, Dc.TITLE, "title", true);
        TupeloStore.getInstance().getContext().perform(u); // FIXME memorize
        for (Tuple<Resource> row : u.getResult() ) {
            if (row.get(0) != null) {
                return row.get(0).getString();
            }
            if (row.get(1) != null) {
                return row.get(1).getString();
            }
        }
        return uri.getString();
    }

    /* For URIs, if the URI is really a tagId of some ACR resource, modify it to be the full 
     * relative URL required to access the resource.
     */
    private String rewrite(Resource uri) throws OperatorException {
        log.debug("Rewriting: " + uri.toString());
        TripleMatcher tm = new TripleMatcher();
        tm.setSubject(uri);
        tm.setPredicate(Rdf.TYPE);
        TupeloStore.getInstance().getContext().perform(tm); // FIXME memorize
        log.debug("Found " + tm.getResult().size() + " triples");
        for (Triple triple : tm.getResult() ) {
            if (triple.getObject() != null) {
                String obj = triple.getObject().getString();
                log.debug(obj);
                if (obj.equals("http://cet.ncsa.uiuc.edu/2007/Dataset")) {
                    log.debug(uri.getString() + "is a DataSet");
                    return ("dataset?id=" + uri.getString());
                } else if (obj.equals("http://cet.ncsa.uiuc.edu/2007/Collection")) {
                    log.debug(uri.getString() + "is a Collection");
                    return ("collection?uri=" + uri.getString());
                }
            }
        }
        if (uri.getString().startsWith("tag:")) {
            //if it starts with tag and is an ACR identifier but isn't a dataset or collection
            // return null so that it is not treated as a URL (just a text value)
            return null;
        }
        //external http/https/ftp URLs should be returned intact
        return uri.getString();
    }

    private Collection<UserMetadataValue> getUserMetadataValues(Thing t, Resource predicate) throws OperatorException {
        return getUserMetadataValues(t, predicate, null, null);
    }

    private Collection<UserMetadataValue> getUserMetadataValues(Thing t, Resource predicate, String marker, String sectionValue) throws OperatorException {
        Collection<UserMetadataValue> values = new LinkedList<UserMetadataValue>();
        //        log.debug("Getting Metadata: " + t.toString() + " : " + predicate.toString() + " : " + marker + " : " + sectionValue);
        for (Object value : t.getValues(predicate) ) {
            UserMetadataValue umv = null;
            if (value instanceof Resource) {
                Resource v = (Resource) value;
                if (v instanceof UriRef) {
                    umv = new UserMetadataValue(rewrite(v), nameOf(v));
                } else {
                    umv = new UserMetadataValue(null, v.getString());
                }
            } else {
                //FixMe : parsing the '<name> : <url>' format that has been used to record people looked up in Vivo
                //We should move to caching the list of vivo people on the server, storing just the URI and looking up the
                //names as needed.
                String vivoUrl = TupeloStore.getInstance().getConfiguration(ConfigurationKey.VIVOIDENTIFIERURL);
                String val = value.toString();
                //log.debug("Val: " + val + " : Vivo: " + vivoUrl);
                int separator = val.indexOf(" : " + vivoUrl);
                String name = val;
                if (separator != -1) {
                    name = val.substring(0, separator);
                    val = val.substring(separator + 3, val.length());
                    umv = new UserMetadataValue(val, name);
                } else {

                    umv = new UserMetadataValue(null, value.toString());
                }

            }
            if (marker != null) {
                umv.setSectionMarker(marker);
                umv.setSectionValue(sectionValue);
            }
            values.add(umv);
        }
        return values;
    }

    @Override
    public GetUserMetadataFieldsResult execute(GetUserMetadataFields action,
            ExecutionContext arg1) throws ActionException {

        try {
            Map<String, String> labels = new HashMap<String, String>();
            Map<String, Collection<UserMetadataValue>> allValues = new HashMap<String, Collection<UserMetadataValue>>();
            ThingSession ts = new ThingSession(TupeloStore.getInstance()
                    .getContext());
            Resource subject = Resource.uriRef(action.getUri());
            Thing t = ts.fetchThing(subject);
            for (UserMetadataField field : ListUserMetadataFieldsHandler.listUserMetadataFields(false).getFieldsSortedByName() ) {
                Resource predicate = Resource.uriRef(field.getUri());
                Collection<UserMetadataValue> values = getUserMetadataValues(t, predicate);
                // now look for sections with this field set; this is gonna produce lots of traffic
                //FixMe - better to do one query for all predicates of rdftype VIEW_METADATA? Or fetch sections as Things?
                Unifier u = new Unifier();
                u.addPattern(subject, MMDB.METADATA_HASSECTION, "section");
                u.addPattern("section", predicate, "value");
                u.addPattern("section", MMDB.SECTION_LABEL, "label");
                u.addPattern("section", MMDB.SECTION_MARKER, "marker");
                u.setColumnNames("section", "label", "marker");
                for (Tuple<Resource> row : TupeloStore.getInstance().unifyExcludeDeleted(u, "section") ) {
                    Thing st = ts.fetchThing(row.get(0));
                    String section = row.get(1).getString() + " " + row.get(2).getString();
                    String sectionValue = row.get(2).getString();
                    values.addAll(getUserMetadataValues(st, predicate, section, sectionValue));
                }
                if (values.size() > 0) {
                    labels.put(field.getUri(), field.getLabel()); // remember the label for this one
                    allValues.put(field.getUri(), values);
                }
            }
            GetUserMetadataFieldsResult r = new GetUserMetadataFieldsResult();
            r.setThingNames(labels);
            r.setValues(allValues);
            ts.close();
            return r;
        } catch (Exception x) {
            log.error("Error getting metadata specified by user for "
                    + action.getUri());
            throw new ActionException("failed", x);
        }
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
