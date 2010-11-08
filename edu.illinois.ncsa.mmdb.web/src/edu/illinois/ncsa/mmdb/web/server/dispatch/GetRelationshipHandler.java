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

import java.util.LinkedList;
import java.util.List;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetRelationship;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetRelationshipResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * Get datasets a particular dataset is related to (all types)
 * 
 * @author Luis Mendez
 * 
 */
public class GetRelationshipHandler implements
        ActionHandler<GetRelationship, GetRelationshipResult> {

    public static Resource MMDB_RELATIONSHIP         = Cet.cet("mmdb/relationship");
    public static Resource MMDB_RELATIONSHIP_TYPE    = Cet.cet("mmdb/relationshipType");
    public static Resource MMDB_RELATIONSHIP_DATASET = Cet.cet("mmdb/relationshipDataset");

    /** Commons logging **/
    private static Log     log                       = LogFactory.getLog(GetRelationshipHandler.class);

    @Override
    public GetRelationshipResult execute(GetRelationship arg0,
            ExecutionContext arg1) throws ActionException {

        // query
        try {
            Resource subject = Resource.uriRef(arg0.getDatasetURI());
            Unifier u = new Unifier();

            //one direction
            u.addPattern(subject, MMDB_RELATIONSHIP, "relationship");
            u.addPattern("relationship", MMDB_RELATIONSHIP_TYPE, "type");
            u.addPattern("relationship", MMDB_RELATIONSHIP_DATASET, "dataset");
            u.addPattern("relationship", Dc.CREATOR, "creator");
            u.addPattern("relationship", Dc.DATE, "date");

            u.setColumnNames("relationship", "type", "dataset", "creator", "date");

            TupeloStore.getInstance().getContext().perform(u);

            List<DatasetBean> rt = new LinkedList<DatasetBean>();
            List<String> types = new LinkedList<String>();
            //HashMap<DatasetBean, String> rt = new HashMap<DatasetBean, String>();

            for (Tuple<Resource> row : u.getResult() ) {

                DatasetBean db = TupeloStore.fetchDataset(row.get(2)); // dbu's only take strings
                String type = row.get(1).getString();

                rt.add(db);
                types.add(type);
            }

            return new GetRelationshipResult(rt, types);

        } catch (Exception x) {
            log.error("Error getting related datasets for " + arg0.getDatasetURI(), x);
            throw new ActionException("unable to find datasets "
                    + arg0.getDatasetURI() + " has a relationship with");
        }
    }

    @Override
    public Class<GetRelationship> getActionType() {
        return GetRelationship.class;
    }

    @Override
    public void rollback(GetRelationship arg0, GetRelationshipResult arg1,
            ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
