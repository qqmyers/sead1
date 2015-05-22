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

import java.util.Collection;
import java.util.HashSet;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleMatcher;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.DcTerms;

import edu.illinois.ncsa.mmdb.web.client.dispatch.AddToCollection;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AddToCollectionResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;

/**
 * Add a set of resources to a collection.
 *
 * @author Luigi Marini
 * @myersjd@umich.edu
 *
 */
public class AddToCollectionHandler implements ActionHandler<AddToCollection, AddToCollectionResult> {

    /** Commons logging **/
    private static Log           log       = LogFactory.getLog(AddToCollectionHandler.class);

    //A non-collection resource that tracks all things not in other collections
    public static final Resource TOP_LEVEL = Resource.uriRef("http://sead-data.net/terms/acr/Top_Level");
    //THe relationship between TOP_LEVEL and the things at the top
    public static final Resource INCLUDES  = Resource.uriRef("http://sead-data.net/terms/acr/includes");

    @Override
    public AddToCollectionResult execute(AddToCollection arg0,
            ExecutionContext arg1) throws ActionException {

        BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

        CollectionBeanUtil cbu = new CollectionBeanUtil(beanSession);

        Collection<String> resourcesString = arg0.getResources();

        Collection<Resource> resources = new HashSet<Resource>();
        TripleWriter tw = new TripleWriter();
        for (String uri : resourcesString ) {
            UriRef current = Resource.uriRef(uri);
            resources.add(current);
            TupeloStore.getInstance().changed(uri);
            tw.remove(AddToCollectionHandler.TOP_LEVEL, AddToCollectionHandler.INCLUDES, current);
        }

        try {
            CollectionBean collectionBean = cbu.get(arg0.getCollectionUri(), true);
            cbu.addToCollection(collectionBean, resources);
            TupeloStore.getInstance().extractPreviews(arg0.getCollectionUri(), true); // rerun the extraction(s).
            TupeloStore.getInstance().getContext().perform(tw);
        } catch (OperatorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return new AddToCollectionResult();
    }

    @Override
    public Class<AddToCollection> getActionType() {
        return AddToCollection.class;
    }

    @Override
    public void rollback(AddToCollection arg0, AddToCollectionResult arg1,
            ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

    public static boolean hasMoreParents(UriRef r, UriRef parent, Context c) throws OperatorException {
        //Index as top level if no other parents
        TripleMatcher tm2 = new TripleMatcher();
        tm2.match(null, DcTerms.HAS_PART, r);
        c.perform(tm2);

        boolean moreParents = false;
        for (Triple s : tm2.getResult() ) {
            if (!s.getSubject().equals(parent)) {
                moreParents = true;
                break;
            }
        }

        return moreParents;
    }

}
