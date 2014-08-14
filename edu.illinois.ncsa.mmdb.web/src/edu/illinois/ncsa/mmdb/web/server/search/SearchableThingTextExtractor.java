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
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.SubjectFacade;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Namespaces;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.cet.search.TextExtractor;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.illinois.ncsa.mmdb.web.server.dispatch.ListUserMetadataFieldsHandler;
import edu.uiuc.ncsa.cet.bean.AnnotationBean;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CETBeans;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;

public class SearchableThingTextExtractor implements TextExtractor<String> {
    Log                        log        = LogFactory.getLog(SearchableThingTextExtractor.class);

    private static BeanSession managedBS  = null;
    private static int         reuseLimit = -1;
    private static int         reuseCount = 0;

    private BeanSession getBeanSession() throws ClassNotFoundException, OperatorException
    {
        BeanSession bs = managedBS;
        if (bs != null) {
            //Check to see if we've reached the reuse limit
            reuseCount++;
            if ((reuseLimit != -1) && (reuseCount >= reuseLimit)) {
                //Hit the limit, so close the old beansession and create a new one
                deleteSession();
                createSession(reuseLimit);
                bs = managedBS;
            }
        }
        if (bs == null) {//Have no session or error creating it
            bs = CETBeans.createBeanSession(TupeloStore.getInstance().getContext());
        }
        return bs;
    }

    private void closeBeanSession(BeanSession bs) {
        if ((managedBS == null) && (bs != null)) {
            bs.close();
            reuseCount = 0;
        }
    }

    Object fetchBean(Resource res) {
        BeanSession bs = null;
        try {
            bs = getBeanSession();
            return bs.fetchBean(res);
        } catch (OperatorException e) {
            log.error("Could not retrieve bean for uri: " + res.getString(), e);

        } catch (ClassNotFoundException e) {
            log.error("Could not retrieve bean for uri: " + res.getString(), e);

        } finally {
            closeBeanSession(bs);
        }
        return null;
    }

    /*When many objects will be indexed, the caller can create/delete a managed beansession to take advantage of bean caching 
     * and reduce beansession initialization costs. The caller MUST call delete when done or the session will be held open...
     *  
     */
    public void createSession(int limit) {
        try {
            managedBS = CETBeans.createBeanSession(TupeloStore.getInstance().getContext());
            reuseLimit = limit;
        } catch (ClassNotFoundException e) {
            log.warn("Could not create bean session for indexing: ", e);
        } catch (OperatorException e) {
            log.warn("Could not create bean session for indexing: ", e);
        }
    }

    public void deleteSession() {
        if (managedBS != null) {

            managedBS.close();
            managedBS = null;
        }
    }

    /**
     * Extract a text representation of an mmdb thing (e.g., a dataset or
     * collection)
     * for full-text indexing purposes.
     */

    @SuppressWarnings("deprecation")
    @Override
    /**
     * This method extracts text from the item with id = uri. The method scans all triples
     *  and makes no assumptions about what the uri represents, but the expectation is that it is
     * a Dataset or Collection and a warning will be logged if that's not true.
     * By default, literal triples are indexed. In some special cases, the value will be further processed.
     * For non-literal values, the special cases where the type of object is known will also be processed, 
     * in a type-specific way, to index additional text. 
     */
    public Map<String, String> extractText(String uri) {
        Map<String, String> result = new HashMap<String, String>();
        assert uri != null;
        //List of user metadata fields - Memoized - cached for an hour
        HashSet<String> userMetadataPredicates = ListUserMetadataFieldsHandler.listUserMetadataFields(false).getPredicates();
        HashSet<String> extractorMetadataPredicates = getExtractorMetadataPredicates();
        SubjectFacade s = TupeloStore.getInstance().getContext().getSubject(Resource.uriRef(uri));
        List<String> resultStrings = new LinkedList<String>();
        try {
            for (Triple t : s.getTriples() ) {
                Resource pred = t.getPredicate();
                Resource obj = t.getObject();
                String val = obj.getString();
                if (obj.isLiteral()) {
                    //DC.TITLE, Rdfs.LABEL are special cases where we split pieces... 
                    if (pred.equals(Dc.TITLE) && val != null) {
                        val = atomize(val);
                    }
                    if (pred.equals(Rdfs.LABEL) && val != null) {
                        val = atomize(val);
                    }
                    if (val != null) {
                        resultStrings.add(val);
                    }
                } else {
                    if (pred.equals(MMDB.METADATA_HASSECTION)) {
                        String sm = getSectionMetadata(obj);
                        if (sm != null) {
                            //Add text and associate with section ID/uri
                            result.put(obj.getString(), sm);
                        }
                    } else if (userMetadataPredicates.contains(pred.getString())) {
                        //Should this be  just literals instead?
                        resultStrings.add(val);
                    } else if (extractorMetadataPredicates.contains(pred.getString())) {
                        //Should this be  just literals instead?
                        resultStrings.add(val);
                    } else if (pred.getString().equals("http://www.holygoat.co.uk/owl/redwood/0.1/tags/taggedWithTag")) {
                        String tag = val.substring("tag:cet.ncsa.uiuc.edu,2008:/tag#".length());
                        resultStrings.add(URLDecoder.decode(tag));
                    } else if (pred.equals(Dc.CREATOR)) { //The uploader (PersonBean.getCreator())
                        resultStrings.add(getUploaderText(obj));
                    } else if (pred.equals(Namespaces.dcTerms("creator"))) {// Creators (PersonBean.getContributors())
                        resultStrings.add(getCreatorsText(val));
                    } else if (pred.getString().equals("http://cet.ncsa.uiuc.edu/2007/annotation/hasAnnotation")) {//annotations/comments
                        resultStrings.add(getAnnotationText(obj));
                    }
                }

            }

            result.put(uri, unsplit(resultStrings));
        } catch (OperatorException e) {
            log.warn("Error retrieving triples during indexing:" + e);

        }
        return result;
    }

    private String getCreatorsText(String creator) {
        //Creators are either plain strings, or string with " : " + vivoURL + <usersVivoID> appended.
        //For text indexing, strip the vivo part
        String vivoURL = TupeloStore.getInstance().getConfiguration(ConfigurationKey.VIVOIDENTIFIERURL);
        int index = creator.indexOf(vivoURL);
        if (index != -1) {
            creator = creator.substring(0, index - 3);
        }
        return creator;
    }

    private String getUploaderText(Resource uri) {
        PersonBean person = null;
        List<String> names = new LinkedList<String>();

        person = (PersonBean) fetchBean(uri);
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
        return unsplit(names);
    }

    String getAnnotationText(Resource aResource) {
        AnnotationBean ann = null;
        List<String> text = new LinkedList<String>();
        BeanSession bs = null;
        try {
            bs = getBeanSession();
            ann = (AnnotationBean) bs.fetchBean(aResource);
        } catch (OperatorException e) {
            log.error("Error retrieving annotation bean for indexing", e);
        } catch (ClassNotFoundException e) {
            log.error("Error retrieving annotation bean for indexing", e);
        }
        if (ann != null) {
            String desc = ann.getDescription();
            if (desc != null) {
                text.add(desc);
            }
            PersonBean creator = ann.getCreator();
            if (creator != null) {
                String name = creator.getName();
                if (name != null) {
                    text.add(name);
                }
                String email = creator.getEmail();
                if (email != null) {
                    text.add(email);
                    text.add(atomize(email));
                }
            }
        }
        closeBeanSession(bs);
        return unsplit(text);
    }

    String getSectionMetadata(Resource uri) {
        Unifier u = new Unifier();
        u.setColumnNames("t");
        u.addPattern(uri, MMDB.SECTION_TEXT, "t");
        try {
            TupeloStore.getInstance().getContext().perform(u);
            for (Tuple<Resource> row : u.getResult() ) {
                return row.get(0).getString();
            }
        } catch (OperatorException e) {
            log.warn("Exception indexing section text: " + e);
        }
        return null;
    }

    private HashSet<String> getExtractorMetadataPredicates() {

        Unifier uf = new Unifier();
        uf.addPattern("predicate", Rdf.TYPE, MMDB.METADATA_TYPE); //$NON-NLS-1$
        uf.setColumnNames("predicate"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        HashSet<String> result = new HashSet<String>();

        try {
            TupeloStore.getInstance().getContext().perform(uf);

            for (Tuple<Resource> row : uf.getResult() ) {
                result.add(row.get(0).getString());
            }
        } catch (OperatorException e1) {
            log.error("Error getting extractor metadata predicates for indexing: ", e1);
        }

        return result;
    }

    // split a string into words on non-whitespace boundaries
    static String atomize(String title) {
        String e = title.replaceAll("([a-z])([A-Z])", "$1 $2");
        e = e.replaceAll("([-_\\[\\]\\.])", " $1 ");
        e = e.replaceAll("([0-9]+)", " $1 ");
        e = e.replaceAll("  +", " ");
        return title + " " + e;
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
