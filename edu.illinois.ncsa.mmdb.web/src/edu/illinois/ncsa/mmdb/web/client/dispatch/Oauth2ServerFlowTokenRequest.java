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
public class Oauth2ServerFlowTokenRequest implements Action<Oauth2ServerFlowTokenRequestResult> {

    private String code;
    private String state;
    private String provider;

    public Oauth2ServerFlowTokenRequest() {
    }

    public Oauth2ServerFlowTokenRequest(String codeString, String state, String provider) {
        this.setCode(codeString);
        this.setState(state);
        this.setProvider(provider);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
