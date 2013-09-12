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

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map.Entry;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;

import edu.illinois.ncsa.mmdb.web.client.dispatch.ConfigurationResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetConfiguration;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.illinois.ncsa.mmdb.web.server.SEADRbac;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;

/**
 * Get license attached to a specific resource.
 * 
 * @author Rob Kooper
 * 
 */
public class SetConfigurationHandler implements ActionHandler<SetConfiguration, ConfigurationResult> {
    /** Commons logging **/
    private static Log log = LogFactory.getLog(SetConfigurationHandler.class);

    @Override
    public ConfigurationResult execute(SetConfiguration arg0, ExecutionContext arg1) throws ActionException {
        SEADRbac rbac = new SEADRbac(TupeloStore.getInstance().getContext());
        try {
            if (!rbac.checkPermission(arg0.getUser(), Permission.VIEW_ADMIN_PAGES)) {
                throw (new ActionException("No admin permission."));
            }
        } catch (RBACException exc) {
            throw (new ActionException("No admin permission.", exc));
        }

        ConfigurationResult result = new ConfigurationResult();
        for (Entry<ConfigurationKey, String> entry : arg0.getConfiguration().entrySet() ) {
            ConfigurationKey key = entry.getKey();
            try {
                if (key != ConfigurationKey.ExtractorUrl) {
                    TupeloStore.getInstance().setConfiguration(key, entry.getValue());
                }
                else {
                    // if the key is extrator URL, then need to check its validity
                    final ConfigurationKey extractorKey = key;
                    String server = entry.getValue();
                    if (!server.endsWith("/")) {
                        server += "/";
                    }
                    final String serverUrl = server;

                    server += "extractor/status";

                    // check validity by getting the response code from ($EXTRACTOR_URL)/extractor/status
                    try {
                        URL testUrl = new URL(server);
                        URLConnection connection = testUrl.openConnection();
                        connection.connect();

                        if (connection instanceof HttpURLConnection)
                        {
                            HttpURLConnection httpConnection = (HttpURLConnection) connection;

                            // 200 means valid URL
                            if (200 == httpConnection.getResponseCode()) {
                                TupeloStore.getInstance().setConfiguration(extractorKey, serverUrl);
                            }
                        }

                    } catch (Throwable thr) {
                        log.warn("Could not connect to the Extraction Server URL : " + extractorKey, thr);
                    }

                }
            } catch (OperatorException e) {
                log.warn("Could not store entry for key : " + key, e);
            }
            result.setConfiguration(key, TupeloStore.getInstance().getConfiguration(key));
        }
        return result;
    }

    @Override
    public Class<SetConfiguration> getActionType() {
        return SetConfiguration.class;
    }

    @Override
    public void rollback(SetConfiguration arg0, ConfigurationResult arg1, ExecutionContext arg2) throws ActionException {
        throw (new ActionException("Rollback not implemented"));
    }
}
