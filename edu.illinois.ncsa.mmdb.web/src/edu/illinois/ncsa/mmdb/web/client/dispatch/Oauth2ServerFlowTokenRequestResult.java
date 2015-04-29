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
/**
 *
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Result;

/**
 * Get user information via oath2 server side flow. Initial code is acquired on
 * the
 * client side.
 *
 * @author myersjd@umich.edu
 *
 */

public class Oauth2ServerFlowTokenRequestResult implements Result {

    /**
     *
     */
    private static final long serialVersionUID = -2645395902591679254L;

    private int               expirationTime;
    private String            authToken;
    private String            id;

    public Oauth2ServerFlowTokenRequestResult() {
    }

    public Oauth2ServerFlowTokenRequestResult(String userName, String email, String token, int exp) {
        this.setAuthToken(token);
        this.setExpirationTime(exp);
    }

    public int getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(int l) {
        this.expirationTime = l;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
