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

import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilderFactory;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.util.Tuple;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetMetadata;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetMetadataResult;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;

/**
 * Retrieve generic metadata about a resource.
 * 
 * @author Luigi Marini
 * @author Rob Kooper
 * 
 */
public class GetMetadataHandler implements
        ActionHandler<GetMetadata, GetMetadataResult> {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(GetMetadataHandler.class);

    @Override
    public GetMetadataResult execute(GetMetadata action, ExecutionContext arg1)
            throws ActionException {

        Resource uri = Resource.resource(action.getUri());

        GetMetadataResult result = new GetMetadataResult();

        Unifier uf = new Unifier();
        uf.addPattern(uri, "predicate", "value"); //$NON-NLS-1$ //$NON-NLS-2$
        uf.addPattern("predicate", Rdf.TYPE, MMDB.METADATA_TYPE); //$NON-NLS-1$
        uf.addPattern("predicate", MMDB.METADATA_CATEGORY, "category"); //$NON-NLS-1$ //$NON-NLS-2$
        uf.addPattern("predicate", Rdfs.LABEL, "label"); //$NON-NLS-1$ //$NON-NLS-2$
        uf.setColumnNames("label", "value", "category"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        try {
            TupeloStore.getInstance().getContext().perform(uf);

            for (Tuple<Resource> row : uf.getResult() ) {
                if (row.get(0) != null) {
                    result.add(row.get(2).getString(), row.get(0).getString(),
                            row.get(1).getString());
                }
            }
        } catch (OperatorException e1) {
            log.error("Error getting metadata for " + action.getUri(), e1);
            e1.printStackTrace();
        }

        // SEAD SPECIFIC CODE
        try {
            String vaurl = TupeloStore.getInstance().getConfiguration(ConfigurationKey.VAURL);
            if (!vaurl.equals("")) {
                URL url = new URL(String.format(vaurl, URLEncoder.encode(uri.getString(), "UTF-8")));
                log.debug("DOI Querying " + url.toString());
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openStream());
                Element root = doc.getDocumentElement();
                NodeList nl = root.getElementsByTagName("idValue");
                for (int i = 0; i < nl.getLength(); i++ ) {
                    Node node = nl.item(i);
                    log.debug("Adding DOI metadata: " + node.getNodeValue());
                    result.add("VA", "DOI", node.getNodeValue());
                }
            }
        } catch (java.io.FileNotFoundException fnfe) {
            log.debug("No DOI for " + uri.getString());
        } catch (Throwable thr) {
            log.error("Error getting DOI", thr);
        }
        // END SEAD SPECIFIC CODE

        return result;
    }

    @Override
    public Class<GetMetadata> getActionType() {
        return GetMetadata.class;
    }

    @Override
    public void rollback(GetMetadata arg0, GetMetadataResult arg1, ExecutionContext arg2) throws ActionException {
    }
}
