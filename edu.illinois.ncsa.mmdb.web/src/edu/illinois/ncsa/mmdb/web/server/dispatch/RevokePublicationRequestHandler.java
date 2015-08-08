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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Tuple;

import com.hp.hpl.jena.vocabulary.DCTerms;

import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.RevokePublicationRequest;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.illinois.ncsa.mmdb.web.server.SEADRbac;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;

/**
 * TODO Add comments
 *
 * @author Luis Mendez
 *
 */
public class RevokePublicationRequestHandler implements ActionHandler<RevokePublicationRequest, EmptyResult> {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(RevokePublicationRequestHandler.class);

    @Override
    public EmptyResult execute(RevokePublicationRequest action, ExecutionContext arg1)
            throws ActionException {
        // only allow user to edit user metadata fields
        SEADRbac rbac = new SEADRbac(TupeloStore.getInstance().getContext());
        try {
            if (!rbac.checkPermission(action.getUser(), action.getUri(), Permission.EDIT_USER_METADATA)) {
                throw new ActionException("Unauthorized");
            }
        } catch (RBACException e) {
            throw new ActionException("access control failure", e);
        }
        try {
            TripleWriter tw = new TripleWriter();

            //1.5
            Resource subject = Resource.uriRef(action.getUri());
            Resource predicate = IsReadyForPublicationHandler.proposedForPublicationRef;
            Resource value = Resource.literal("true"); //Value is not used - just checking the existence of the triple for 1.5 publication
            log.debug("Removing 1.5 triple");
            tw.remove(subject, predicate, value);

            log.debug("Removing 2.0 triples");

            Unifier uf = new Unifier();
            uf.addPattern(subject, DcTerms.HAS_VERSION, "agg");
            uf.addPattern("agg", Resource.uriRef("http://sead-data.net/vocab/hasVersionNumber"), "ver");

            uf.addPattern("agg", Resource.uriRef(DCTerms.identifier.getURI()), "ext", true);
            uf.setColumnNames("agg", "ver", "ext");
            TupeloStore.getInstance().getContext().perform(uf);
            for (Tuple<Resource> row : uf.getResult() ) {
                if (row.get(2) == null) {
                    //Removing any request that does not yet have an ext id (isn't complete) since we only want one open at a time
                    //Write required triples
                    UriRef aggId = (UriRef) row.get(0);
                    tw.remove(aggId, Rdf.TYPE, RequestPublicationaHandler.Aggregation);
                    tw.remove(subject, DcTerms.HAS_VERSION, aggId);
                    tw.remove(aggId, Resource.uriRef("http://sead-data.net/vocab/hasVersionNumber"), Resource.literal(row.get(1).toString()));

                    try {
                        //Send notice to service
                        String server = TupeloStore.getInstance().getConfiguration(ConfigurationKey.CPURL);
                        URL url = new URL(server + "/cp/researchobjects/" + aggId);
                        log.debug("URL = " + url.toString());
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                        // send post

                        conn.setRequestProperty("Accept", "application/json");
                        conn.setRequestMethod("DELETE");

                        // Get the response
                        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String line;
                        StringBuilder sb = new StringBuilder();
                        while ((line = rd.readLine()) != null) {
                            log.debug(line);
                            sb.append(line);
                            sb.append("\n"); //$NON-NLS-1$
                        }
                        rd.close();
                    } catch (IOException io) {
                        log.warn("2.0 Pub Request delete call failed for :" + aggId.toString(), io);
                    }
                }
            }

            TupeloStore.getInstance().getContext().perform(tw);

            return new EmptyResult();

        } catch (Exception x) {
            log.error("Error setting metadata on " + action.getUri(), x);
            throw new ActionException("failed", x);
        }
    }

    @Override
    public Class<RevokePublicationRequest> getActionType() {
        return RevokePublicationRequest.class;
    }

    @Override
    public void rollback(RevokePublicationRequest arg0, EmptyResult arg1,
            ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
