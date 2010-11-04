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

import static org.tupeloproject.rdf.Namespaces.dcTerms;
import static org.tupeloproject.rdf.Resource.uriRef;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.BatchResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.LicenseResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetLicense;
import edu.illinois.ncsa.mmdb.web.server.AccessControl;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;

/**
 * Get license attached to a specific resource.
 * 
 * @author Rob Kooper
 * 
 */
public class SetLicenseHandler implements ActionHandler<SetLicense, BatchResult> {
    // FIXME move to dcTerms
    public static Resource DCTERMS_RIGHTS_HOLDER = uriRef(dcTerms("rightsHolder"));
    public static Resource DCTERMS_RIGHTS        = uriRef(dcTerms("rights"));
    public static Resource DCTERMS_LICENSE       = uriRef(dcTerms("license"));

    /** Commons logging **/
    private static Log     log                   = LogFactory.getLog(SetLicenseHandler.class);

    @Override
    public BatchResult execute(SetLicense arg0, ExecutionContext arg1) throws ActionException {
        BatchResult result = new BatchResult();
        // check for authorization
        boolean isAdmin = AccessControl.isAdmin(arg0.getUser());
        // get license information
        TripleWriter tw = new TripleWriter();
        for (String uriString : arg0.getResources() ) {
            Resource uri = Resource.uriRef(uriString);
            Unifier uf = new Unifier();
            uf.addPattern(uri, DCTERMS_RIGHTS, "rights", true);
            uf.addPattern(uri, DCTERMS_RIGHTS_HOLDER, "rightsHolder", true);
            uf.addPattern(uri, DCTERMS_LICENSE, "license", true); // FIXME literal used as identifier
            uf.addPattern(uri, MMDB.ALLOW_DOWNLOAD, "allowDownload", true);
            uf.setColumnNames("rights", "rightsHolder", "license", "allowDownload");
            try {
                TupeloStore.getInstance().getContext().perform(uf);
            } catch (OperatorException e) {
                log.warn("Could not get license information.", e);
                throw (new ActionException("Could not get license information.", e));
            }

            // remove old data
            for (Tuple<Resource> row : uf.getResult() ) {
                if (row.get(0) != null) {
                    tw.remove(uri, DCTERMS_RIGHTS, row.get(0));
                }
                if (row.get(1) != null) {
                    tw.remove(uri, DCTERMS_RIGHTS_HOLDER, row.get(1));
                }
                if (row.get(2) != null) {
                    tw.remove(uri, DCTERMS_LICENSE, row.get(2));
                }
                if (row.get(3) != null) {
                    tw.remove(uri, MMDB.ALLOW_DOWNLOAD, row.get(3));
                }
            }

            boolean publicDomain = false;
            // add new data
            LicenseResult license = arg0.getLicense();
            if (license.getRights() != null) {
                if (license.getRights().equalsIgnoreCase("pddl")) {
                    publicDomain = true;
                }
                tw.add(uri, DCTERMS_RIGHTS, license.getRights());
            }
            if (license.getRightsHolderUri() != null && !publicDomain) {
                tw.add(uri, DCTERMS_RIGHTS_HOLDER, Resource.uriRef(license.getRightsHolderUri()));
            } else if (license.getRightsHolder() != null && !publicDomain) {
                tw.add(uri, DCTERMS_RIGHTS_HOLDER, license.getRightsHolder());
            }
            if (license.getLicense() != null) {
                tw.add(uri, DCTERMS_LICENSE, license.getLicense());
            }
            tw.add(uri, MMDB.ALLOW_DOWNLOAD, license.isAllowDownload());

            result.addSuccess(uriString);
        }

        try {
            TupeloStore.getInstance().getContext().perform(tw);
        } catch (OperatorException e) {
            log.warn("Could not write license information.", e);
            throw (new ActionException("Could not write license information.", e));
        }

        // done
        return result;
    }

    @Override
    public Class<SetLicense> getActionType() {
        return SetLicense.class;
    }

    @Override
    public void rollback(SetLicense arg0, BatchResult arg1, ExecutionContext arg2) throws ActionException {
    }

}
