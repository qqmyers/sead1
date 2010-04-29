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

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.mmdb.web.client.dispatch.AddCollection;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AddCollectionResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;

/**
 * Create a new collection.
 * 
 * @author Luigi Marini
 * 
 */
public class AddCollectionHandler implements ActionHandler<AddCollection, AddCollectionResult> {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(AddCollectionHandler.class);

    @Override
    public AddCollectionResult execute(AddCollection action, ExecutionContext arg1)
            throws ActionException {

        BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

        PersonBeanUtil pbu = new PersonBeanUtil(beanSession);

        CollectionBean collection = action.getCollection();

        try {
            log.debug("Adding collection " + action.getCollection().getTitle());

            // create person bean from session id
            // FIXME only required until sessionid stores the full uri and not the email address
            String sessionId = action.getSessionId();
            String personID = sessionId;
            if (!sessionId.startsWith(PersonBeanUtil.getPersonID(""))) {
                personID = PersonBeanUtil.getPersonID(sessionId);
            }

            try {
                collection.setCreator(pbu.get(personID));
            } catch (Exception e1) {
                log.error("Error getting creator of collection", e1);
            }

            // set creation date
            collection.setCreationDate(new Date());

            // save to repository
            beanSession.registerAndSave(action.getCollection());

            // add any members
            if (action.getMembers() != null && action.getMembers().size() > 0) {
                CollectionBeanUtil cbu = new CollectionBeanUtil(beanSession);
                List<Resource> r = new LinkedList<Resource>();
                for (String uri : action.getMembers() ) {
                    r.add(Resource.uriRef(uri));
                }
                cbu.addToCollection(collection, r);
                for (String uri : action.getMembers() ) {
                    TupeloStore.getInstance().changed(uri);
                }
            }

            // FIXME why doesn't update work and we have to use registerAndSave?
            //			CollectionBeanUtil cbu = new CollectionBeanUtil(beanSession);
            //			CollectionBean collection = cbu.update(arg0.getCollection());

        } catch (Exception e) {
            log.error("Error creating new collection", e);
        }
        return new AddCollectionResult();
    }

    @Override
    public Class<AddCollection> getActionType() {
        return AddCollection.class;
    }

    @Override
    public void rollback(AddCollection arg0, AddCollectionResult arg1,
            ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
