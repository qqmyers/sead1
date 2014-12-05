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

package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Action;

/**
 * Get Orcid / Oauth2 Server side flow user information. Initial code retrieval
 * is done in the
 * client side.
 *
 * @author myersjd@umich.edu
 *
 */
@SuppressWarnings("serial")
public class Oauth2ServerFlowUserInfo implements Action<Oauth2ServerFlowUserInfoResult> {

    private boolean create;
    private String  token;
    private String  provider;

    public Oauth2ServerFlowUserInfo() {
    }

    public Oauth2ServerFlowUserInfo(String token, String provider) {
        this(token, provider, false);
    }

    public Oauth2ServerFlowUserInfo(String token, String provider, boolean create) {
        this.setCreate(create);
        this.setToken(token);
        this.setProvider(provider);
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean shouldCreate() {
        return create;
    }

    public void setCreate(boolean create) {
        this.create = create;
    }
}
