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

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Cet;

import edu.illinois.ncsa.mmdb.web.client.dispatch.SetRelationship;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetRelationshipResult;

/**
 * Create new relationship between datasets.
 * 
 * @author Luis Mendez
 * 
 */
public class SetRelationshipHandler implements ActionHandler<SetRelationship, SetRelationshipResult> {

    public static Resource MMDB_RELATIONSHIP         = Cet.cet("mmdb/relationship");
    public static Resource MMDB_RELATIONSHIP_TYPE    = Cet.cet("mmdb/relationshipType");
    public static Resource MMDB_RELATIONSHIP_DATASET = Cet.cet("mmdb/relationshipDataset");

    /** Commons logging **/
    private static Log     log                       = LogFactory.getLog(SetRelationshipHandler.class);

    @Override
    public SetRelationshipResult execute(SetRelationship arg0, ExecutionContext arg1) throws ActionException {

        Resource dataset = Resource.uriRef(arg0.getUri1());
        Resource person = Resource.uriRef(arg0.getCreator());
        Resource dataset2 = Resource.uriRef(arg0.getUri2());
        String type = arg0.getType();

        /*
        //create relationship - add tuples
        Unifier uf = new Unifier();

        //one direction
        uf.addPattern(dataset, MMDB_RELATIONSHIP, "relationship");
        uf.addPattern("relationship", MMDB_RELATIONSHIP_TYPE, "type");
        uf.addPattern("relationship", MMDB_RELATIONSHIP_DATASET, "dataset");
        uf.addPattern("relationship", Dc.CREATOR, "creator");
        uf.addPattern("relationship", Dc.DATE, "date");

        uf.setColumnNames("type", "rightsHolder");
        try {
            TupeloStore.getInstance().getContext().perform(uf);
        } catch (OperatorException e) {
            log.warn("Could not get license information.", e);
            throw (new ActionException("Could not ge license information.", e));
        }
        */

        //done
        return new SetRelationshipResult();
    }

    @Override
    public Class<SetRelationship> getActionType() {
        return SetRelationship.class;
    }

    @Override
    public void rollback(SetRelationship arg0, SetRelationshipResult arg1, ExecutionContext arg2)
            throws ActionException {
        // TODO Auto-generated method stub

    }

}
