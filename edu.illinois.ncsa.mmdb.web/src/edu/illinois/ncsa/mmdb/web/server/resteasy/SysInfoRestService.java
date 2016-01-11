package edu.illinois.ncsa.mmdb.web.server.resteasy;

/*******************************************************************************
 * University of Michigan
 * Open Source License
 *
 * Copyright (c) 2013, University of Michigan.  All rights reserved.
 *
 * Developed by:
 * http://sead-data.net
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.mmdb.web.client.dispatch.SystemInfoResult;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.illinois.ncsa.mmdb.web.server.SEADRbac;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetConfigurationHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.SystemInfoHandler;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;

/**
 * @author Jim Myers <myersjd@umich.edu>
 *
 */

@Path("/sys")
@NoCache
public class SysInfoRestService {

    /** Commons logging **/
    private static Log    log                  = LogFactory.getLog(SysInfoRestService.class);

    //Software Version - should be kept in sync with /war/mmdb.html
    private static String _versionNumberString = "1.5.2";

    //Will be replaced with actual build # by bamboo build process
    private static String _buildNumber         = "@VERSION@";

    @GET
    @Path("/info")
    @Produces("application/json")
    public Response executeSysInfo() {
        log.debug("In Sys Info");
        SEADRbac rbac = new SEADRbac(TupeloStore.getInstance().getContext());
        Resource anon = PersonBeanUtil.getAnonymousURI();

        try {
            if (!rbac.checkPermission(anon, Resource.uriRef(Permission.VIEW_SYSTEM.getUri()))) {
                return Response.status(401).entity("Access to this endpoint is access controlled.").build();
            }
        } catch (RBACException e1) {
            log.error("Error running sys info: ", e1);
            return Response.status(500).entity("Error running sys info [" + e1.getMessage() + "]").build();
        }

        try {
            SystemInfoHandler sih = new SystemInfoHandler();
            SystemInfoResult sir = sih.execute(null, null);
            if (sir == null) {
                log.warn("Null Sys Info Results");
                return Response.status(503).entity("System info is being recalculated.").build();
            }
            Map<String, String> map = sir.getMap();

            //Add software version/build info
            map.put("Version", _versionNumberString);
            map.put("Build", _buildNumber);

            return Response.status(200).entity(map).build();
        } catch (Exception e) {
            log.error("Error running sys info: ", e);
            return Response.status(500).entity("Error running sys info [" + e.getMessage() + "]").build();
        }
    }

    @GET
    @Path("/config")
    @Produces("application/json")
    public Response executeSysConfig() {
        log.debug("In Sys Config");

        List<ConfigurationKey> keyList = GetConfigurationHandler.getWhitelist();
        //the ORCID Id is not secret (can be seen from a client) but not needed here either
        //Since keyList doesn't support remove()...
        List<ConfigurationKey> newList = new ArrayList<ConfigurationKey>();
        for (ConfigurationKey ck : keyList ) {
            if (ck != ConfigurationKey.OrcidClientId) {
                newList.add(ck);
            }
        }
        return configInfo(newList);
    }

    /**
     * Provides the basic info related to displaying a project space - i.e. the
     * name. url, description, logo, ...
     *
     * @return array of values for project's basic descriptive info
     */
    @GET
    @Path("/basic")
    @Produces("application/json")
    public Response executeSysBasic() {
        log.debug("In Sys Basic");

        List<ConfigurationKey> keyList = new ArrayList<ConfigurationKey>();
        keyList.add(ConfigurationKey.ProjectName);
        keyList.add(ConfigurationKey.ProjectDescription);
        keyList.add(ConfigurationKey.ProjectURL);
        keyList.add(ConfigurationKey.ProjectHeaderLogo);
        keyList.add(ConfigurationKey.ProjectHeaderBackground);
        keyList.add(ConfigurationKey.ProjectHeaderTitleColor);
        return configInfo(keyList);
    }

    private Response configInfo(List<ConfigurationKey> keyList) {
        SEADRbac rbac = new SEADRbac(TupeloStore.getInstance().getContext());
        Resource anon = PersonBeanUtil.getAnonymousURI();

        try {
            if (!rbac.checkPermission(anon, Resource.uriRef(Permission.VIEW_SYSTEM.getUri()))) {
                return Response.status(401).entity("Access to this endpoint is access controlled.").build();
            }
        } catch (RBACException e1) {
            log.error("Error running sys config: ", e1);
            return Response.status(500).entity("Error running sys config [" + e1.getMessage() + "]").build();
        }

        try {
            HashMap<String, String> map = new HashMap<String, String>();

            for (ConfigurationKey key : keyList ) {
                map.put(key.getPropertyKey(), TupeloStore.getInstance().getConfiguration(key));
            }
            return Response.status(200).entity(map).build();
        } catch (Exception e) {
            log.error("Error running sys info: ", e);
            return Response.status(500).entity("Error running sys info [" + e.getMessage() + "]").build();
        }
    }
}
