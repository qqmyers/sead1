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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.mmdb.web.client.dispatch.ConfigurationResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetConfiguration;
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
public class GetConfigurationHandler implements ActionHandler<GetConfiguration, ConfigurationResult> {
    private final static List<ConfigurationKey> WHITELIST = Arrays.asList(new ConfigurationKey[] {
                                                          ConfigurationKey.GoogleMapKey,
                                                          ConfigurationKey.ProjectName,
                                                          ConfigurationKey.ProjectDescription,
                                                          ConfigurationKey.ProjectURL,
                                                          ConfigurationKey.VAURL,
                                                          ConfigurationKey.VIVOQUERYURL,
                                                          ConfigurationKey.VIVOIDENTIFIERURL,
                                                          ConfigurationKey.DiscoveryURL,
                                                          ConfigurationKey.BigData,
                                                          ConfigurationKey.AccessLevelLabel,
                                                          ConfigurationKey.UseGoogleDocViewer,
                                                          ConfigurationKey.PresentationSortOrder,
                                                          ConfigurationKey.PresentationPageViewType,
                                                          ConfigurationKey.PresentationDataViewLevel,
                                                          ConfigurationKey.OrcidClientId,
                                                          ConfigurationKey.ProjectHeaderBackground,
                                                          ConfigurationKey.ProjectHeaderLogo,
                                                          ConfigurationKey.ProjectHeaderTitleColor
                                                          });

    /** Commons logging **/
    private static Log                          log       = LogFactory.getLog(GetConfigurationHandler.class);

    @Override
    public ConfigurationResult execute(GetConfiguration arg0, ExecutionContext arg1) throws ActionException {
        Set<ConfigurationKey> keys = arg0.getKeys();
        if (keys.size() == 0) {
            keys.addAll(Arrays.asList(ConfigurationKey.values()));
        }

        // make sure keys are whitelisted
        boolean checkadmin = false;
        for (ConfigurationKey key : keys ) {
            if (!WHITELIST.contains(key)) {
                checkadmin = true;
            }
        }
        log.debug("Config u: " + arg0.getUser());
        // check admin status
        if (checkadmin) {
            SEADRbac rbac = new SEADRbac(TupeloStore.getInstance().getContext());
            try {
                if (!rbac.checkPermission(arg0.getUser(), Permission.VIEW_ADMIN_PAGES)) {
                    log.warn("Non-admin request for non-whitelisted configuration parameters - sending exception to client");
                    throw (new ActionException("No admin permission."));
                }
            } catch (RBACException exc) {
                log.error("Permission exception when retrieving config values", exc);
                throw (new ActionException("No admin permission.", exc));
            }
        }

        // return keys
        ConfigurationResult result = new ConfigurationResult();
        for (ConfigurationKey key : keys ) {
            result.setConfiguration(key, TupeloStore.getInstance().getConfiguration(key));
        }
        return result;
    }

    public static List<ConfigurationKey> getWhitelist() {
        return WHITELIST;
    }

    @Override
    public Class<GetConfiguration> getActionType() {
        return GetConfiguration.class;
    }

    @Override
    public void rollback(GetConfiguration arg0, ConfigurationResult arg1, ExecutionContext arg2) throws ActionException {
    }
}
