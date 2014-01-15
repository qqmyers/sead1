/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.resteasy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
public class SysInfoRestService {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(SysInfoRestService.class);

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
            Map<String, String> map = sir.getMap();
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

        List<ConfigurationKey> keyList = GetConfigurationHandler.getWhitelist();

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
