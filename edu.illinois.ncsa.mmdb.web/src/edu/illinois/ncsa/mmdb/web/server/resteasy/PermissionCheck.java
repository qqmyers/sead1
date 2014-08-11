/*******************************************************************************
 * Copyright 2014 University of Michigan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.illinois.ncsa.mmdb.web.server.resteasy;

import java.net.HttpURLConnection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.rdf.UriRef;

import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;

public class PermissionCheck {

    private boolean    hasPermission = false;
    private Response   r;

    /** Commons logging **/
    private static Log log           = LogFactory.getLog(PermissionCheck.class);

    public PermissionCheck(UriRef userId, Permission p) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        try {
            if (!ItemServicesImpl.rbac.checkPermission(userId.toString(), p)) {
                log.debug(userId.toString() + "  forbidden");
                result.put("Failure", "User " + userId.toString() + " does not the required \"" + p.getLabel() + "\" permission");
                r = Response.status(HttpURLConnection.HTTP_FORBIDDEN).entity(result).build();
            } else {
                hasPermission = true;
            }
        } catch (RBACException re) {
            log.error(re.getMessage(), re);
            result.put("Error", "Server error: " + re.getMessage() + " checking permsission: " + p.getLabel());
            r = Response.status(500).entity(result).build();
        }
    }

    public boolean userHasPermission() {
        return hasPermission;
    }

    public Response getErrorResponse() {
        return r;
    }
}
