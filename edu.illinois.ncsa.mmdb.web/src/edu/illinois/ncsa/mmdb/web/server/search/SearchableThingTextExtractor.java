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
package edu.illinois.ncsa.mmdb.web.server.search;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Thing;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.Rdfs;

import edu.illinois.ncsa.cet.search.TextExtractor;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollections;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollectionsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetMetadata;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetMetadataResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserMetadataFields;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserMetadataFieldsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.Metadata;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetCollectionsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetMetadataHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetUserMetadataFieldsHandler;
import edu.uiuc.ncsa.cet.bean.AnnotationBean;
import edu.uiuc.ncsa.cet.bean.CETBean;
import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.AnnotationBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.CETBeans;
import edu.uiuc.ncsa.cet.bean.tupelo.TagEventBeanUtil;

public class SearchableThingTextExtractor implements TextExtractor<String> {
    Log log = LogFactory.getLog(SearchableThingTextExtractor.class);

    BeanSession getBeanSession() throws OperatorException, ClassNotFoundException {
        return CETBeans.createBeanSession(TupeloStore.getInstance().getContext());
    }

    Object fetchBean(String uri) throws OperatorException, ClassNotFoundException {
        BeanSession bs = getBeanSession();
        try {
            return bs.fetchBean(Resource.uriRef(uri));
        } finally {
            bs.close();
        }
    }

    @Override
    /**
     * Extract a text representation of an mmdb thing (e.g., a dataset or collection)
     * for full-text indexing purposes.
     */
    public String extractText(String uri) {
        assert uri != null;
        String text = "";
        try {
            Object bean = fetchBean(uri);
            if (bean instanceof CETBean) {
                text = text((CETBean) bean);
                bean = null;
            }
        } catch (Exception x) {
            log.warn("unexpected bean session behavior: " + x.getMessage());
        }
        // it's either not a bean or not a CETBean
        try {
            text = text(uri);
        } catch (Exception x) { // something's wrong
            x.printStackTrace();
            return "";
        }
        //log.debug(uri+"="+text); // FIXME debug
        return text;
    }

    String text(String uri) throws OperatorException, ClassNotFoundException {
        return unsplit(title(uri), tags(uri), authors(uri), annotations(uri), collections(uri), metadata(uri), userMetadata(uri));
    }

    String text(CETBean bean) throws OperatorException, ClassNotFoundException {
        return unsplit(title(bean), tags(bean), authors(bean), annotations(bean), collections(bean), metadata(bean), userMetadata(bean));
    }

    String authors(DatasetBean bean) {
        List<PersonBean> contributors = new LinkedList<PersonBean>();
        contributors.add(bean.getCreator());
        contributors.addAll(bean.getContributors());
        List<String> names = new LinkedList<String>();
        for (PersonBean person : contributors ) {
            if (person != null) {
                String name = person.getName();
                if (name != null) {
                    names.add(name);
                }
                String email = person.getEmail();
                if (email != null) {
                    names.add(email);
                    names.add(atomize(email));
                }
            }
        }
        return unsplit(names);
    }

    String authors(CETBean bean) {
        if (bean instanceof DatasetBean) {
            return authors((DatasetBean) bean);
        } else {
            log.warn("unexpected bean class " + bean.getClass());
            return authors(bean.getUri());
        }
    }

    // aaagh, unsafe casts
    String authors(String uri) {
        try {
            Object bean = fetchBean(uri);
            if (bean instanceof CETBean) {
                String authors = authors((CETBean) bean);
                bean = null;
                return authors;
            } else {
                log.error(uri + " is not a bean, no authors extracted");
            }
        } catch (Exception x) {
        }
        return "";
    }

    String tags(CETBean bean) throws OperatorException, ClassNotFoundException {
        return tags(bean.getUri());
    }

    String tags(String uri) throws OperatorException, ClassNotFoundException {
        BeanSession bs = getBeanSession();
        TagEventBeanUtil tebu = new TagEventBeanUtil(bs);
        TreeSet<String> tags = new TreeSet<String>();
        try {
            tags.addAll(tebu.getTags(uri));
            return unsplit(tags);
        } catch (OperatorException e) {
            e.printStackTrace();
            return "";
        } finally {
            bs.close();
        }
    }

    String annotations(CETBean bean) throws OperatorException, ClassNotFoundException {
        return annotations(bean.getUri());
    }

    String annotations(String uri) throws OperatorException, ClassNotFoundException {
        BeanSession bs = getBeanSession();
        AnnotationBeanUtil abu = new AnnotationBeanUtil(bs);
        List<String> annotations = new LinkedList<String>();
        try {
            for (AnnotationBean annotation : abu.getAssociationsFor(uri) ) {
                annotations.add(annotation.getDescription());
            }
        } catch (OperatorException e) {
            e.printStackTrace();
            return "";
        } finally {
            bs.close();
        }
        return unsplit(annotations);
    }

    String collections(CETBean bean) {
        return collections(bean.getUri());
    }

    String collections(String uri) {
        GetCollectionsResult r = GetCollectionsHandler.getCollections(new GetCollections(uri));
        List<String> names = new LinkedList<String>();
        for (CollectionBean c : r.getCollections() ) {
            names.add(c.getTitle());
        }
        return unsplit(names);
    }

    String metadata(CETBean bean) {
        return metadata(bean.getUri());
    }

    String metadata(String uri) {
        GetMetadata gm = new GetMetadata(uri);
        GetMetadataResult gmr;
        try {
            gmr = (new GetMetadataHandler()).execute(gm, null);
        } catch (ActionException e) {
            e.printStackTrace();
            return "";
        }
        List<String> allValues = new LinkedList<String>();
        for (Metadata m : gmr.getMetadata() ) {
            allValues.add(m.getValue());
        }
        return unsplit(allValues);
    }

    String userMetadata(CETBean bean) {
        return userMetadata(bean.getUri());
    }

    String userMetadata(String uri) {
        GetUserMetadataFields gumf = new GetUserMetadataFields(uri);
        GetUserMetadataFieldsResult gumfr;
        try {
            gumfr = (new GetUserMetadataFieldsHandler()).execute(gumf, null);
        } catch (ActionException e) {
            e.printStackTrace();
            return "";
        }
        List<String> allValues = new LinkedList<String>();
        for (Map.Entry<String, Collection<String>> entry : gumfr.getValues().entrySet() ) {
            //log.debug(entry.getKey()+"="+entry.getValue());
            allValues.addAll(entry.getValue());
        }
        return unsplit(allValues);
    }

    // split a string into words on non-whitespace boundaries
    static String atomize(String title) {
        String e = title.replaceAll("([a-z])([A-Z])", "$1 $2");
        e = e.replaceAll("([-_\\[\\]\\.])", " $1 ");
        e = e.replaceAll("([0-9]+)", " $1 ");
        e = e.replaceAll("  +", " ");
        return title + " " + e;
    }

    String title(CETBean bean) {
        return atomize(bean.getLabel());
    }

    String title(String uri) throws OperatorException, ClassNotFoundException {
        BeanSession bs = getBeanSession();
        try {
            Thing thing = bs.getThingSession().fetchThing(Resource.uriRef(uri));
            String dcTitle = thing.getString(Dc.TITLE);
            String rdfsLabel = thing.getString(Rdfs.LABEL);
            return atomize((dcTitle != null ? dcTitle : "") + (rdfsLabel != null ? " " + rdfsLabel : ""));
        } finally {
            bs.close();
        }
    }

    // why am I writing this.
    String unsplit(Iterable<String> strings) {
        boolean first = true;
        StringWriter sw = new StringWriter();
        for (String s : strings ) {
            if (!first) {
                sw.append(' ');
            }
            sw.append(s);
            first = false;
        }
        return sw.toString();
    }

    String unsplit(String... strings) {
        List<String> s = new ArrayList<String>(strings.length);
        for (int i = 0; i < strings.length; i++ ) {
            s.add(strings[i]);
        }
        return unsplit(s);
    }
}
