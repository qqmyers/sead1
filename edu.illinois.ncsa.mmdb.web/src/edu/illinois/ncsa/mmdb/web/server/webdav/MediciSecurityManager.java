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
package edu.illinois.ncsa.mmdb.web.server.webdav;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.SecurityManager;
import com.bradmcevoy.http.http11.auth.DigestResponse;

import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.illinois.ncsa.mmdb.web.server.Authentication;
import edu.illinois.ncsa.mmdb.web.server.SEADRbac;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CETBeans;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;

/**
 * Simple implementation of SecurityManger for medici. This will use the medici
 * to check if the username and password and if the user has access.
 * 
 * @author Rob Kooper
 * 
 */
public class MediciSecurityManager implements SecurityManager {
    private static Log                log      = LogFactory.getLog(MediciSecurityManager.class);

    private final Map<String, String> accepted = new HashMap<String, String>();
    private final SEADRbac            rbac;
    private final boolean             allowDelete;
    private PersonBean                user;

    public MediciSecurityManager(Context context, boolean allowDelete) {
        this.rbac = new SEADRbac(context);
        this.allowDelete = allowDelete;
    }

    public PersonBean getUser() {
        return user;
    }

    @Override
    public String getRealm(String host) {
        return TupeloStore.getInstance().getConfiguration(ConfigurationKey.MediciName);
    }

    @Override
    public boolean isDigestAllowed() {
        return false;
    }

    @Override
    public String authenticate(DigestResponse digestRequest) {
        return null;
    }

    @Override
    public String authenticate(String user, String password) {
        if (new Authentication().authenticate(user, password)) {
            String userid = PersonBeanUtil.getPersonID(user);
            try {
                this.user = new PersonBeanUtil(CETBeans.createBeanSession(rbac.getContext())).get(userid);
            } catch (OperatorException e) {
                log.warn("Could not find user.", e);
                return null;
            } catch (ClassNotFoundException e) {
                log.warn("Could not find user.", e);
                return null;
            } catch (Exception e) {
                log.warn("Could not find user.", e);
                return null;
            }

            String token = UUID.randomUUID().toString();
            accepted.put(token, user);
            return token;
        }
        return null;
    }

    public boolean checkPermission(com.bradmcevoy.http.Resource resource, Permission permission) {
        try {
            return rbac.checkPermission(user.getUri(), resource.getUniqueId(), permission);
        } catch (RBACException e) {
            log.info("Could not check permission.", e);
            return false;
        }
    }

    @Override
    public boolean authorise(Request request, Method method, Auth auth, com.bradmcevoy.http.Resource resource) {
        if (auth == null) {
            return false;
        }
        if (!accepted.containsKey(auth.getTag())) {
            return false;
        }

        // can the user be here?
        // TODO add WEBDAV permission
        if (!checkPermission(resource, Permission.VIEW_DATA)) {
            return false;
        }

        switch (method) {
            case GET:
                if (resource instanceof DatasetBeanResource) {
                    return checkPermission(resource, Permission.DOWNLOAD);
                }
                return true;

            case DELETE:
                if (resource instanceof DatasetBeanResource) {
                    return allowDelete && checkPermission(resource, Permission.DELETE_DATA);
                }
                return false;

            case PUT:
                if (!checkPermission(resource, Permission.UPLOAD_DATA)) {
                    return false;
                }
                if (resource instanceof TagBeanResource) {
                    return checkPermission(resource, Permission.ADD_TAG);
                }
                if (resource instanceof CollectionBeanResource) {
                    // TODO add permission to check for permisson to add to collection
                    return true;
                }
                if (resource instanceof PersonBeanResource) {
                    return resource.getUniqueId().equals(PersonBeanResource.HOME.getString()) || resource.getUniqueId().equals(getUser().getUri());
                }
                return false;

            case PROPFIND:
                return true;

            case LOCK:
                return true;

            default:
                log.info(method + " " + resource);
                return false;
        }
    }
}
