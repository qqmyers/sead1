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
import java.util.HashSet;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;

import edu.illinois.ncsa.mmdb.web.client.dispatch.RemoveFromCollection;
import edu.illinois.ncsa.mmdb.web.client.dispatch.RemoveFromCollectionResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;

public class RemoveFromCollectionHandler implements ActionHandler<RemoveFromCollection, RemoveFromCollectionResult> {

    @Override
    public RemoveFromCollectionResult execute(RemoveFromCollection action,
            ExecutionContext exc) throws ActionException {

        BeanSession beanSession = TupeloStore.getInstance().getBeanSession();
        CollectionBeanUtil cbu = new CollectionBeanUtil(beanSession);
        Collection<String> resourcesString = action.getResources();
        Collection<Resource> resources = new HashSet<Resource>();

        for (String uri : resourcesString ) {
            resources.add(Resource.uriRef(uri));
        }

        try {
            CollectionBean collectionBean = cbu.get(action.getCollectionUri(), true);
            cbu.removeFromCollection(collectionBean, resources);

            TripleWriter tw = new TripleWriter();
            Context c = TupeloStore.getInstance().getContext();
            for (Resource r : resources ) {
                TupeloStore.getInstance().changed(r.getString());
                if (!AddToCollectionHandler.hasMoreParents((UriRef) r, Resource.uriRef(action.getCollectionUri()), c)) {
                    tw.add(AddToCollectionHandler.TOP_LEVEL, AddToCollectionHandler.INCLUDES, r);
                }
            }
            c.perform(tw);
            //Extract last so any error does not affect removal or top-level assignment
            TupeloStore.getInstance().extractPreviews(action.getCollectionUri(), true); //rerun the extraction(s).
        } catch (OperatorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return new RemoveFromCollectionResult();

    }

    @Override
    public Class<RemoveFromCollection> getActionType() {
        return RemoveFromCollection.class;
    }

    @Override
    public void rollback(RemoveFromCollection arg0,
            RemoveFromCollectionResult arg1, ExecutionContext arg2)
            throws ActionException {
        // TODO Auto-generated method stub

    }
}
