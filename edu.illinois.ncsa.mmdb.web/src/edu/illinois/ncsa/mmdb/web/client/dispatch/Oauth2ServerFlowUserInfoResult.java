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

public class Oauth2ServerFlowUserInfoResult implements Result {

    /**
     *
     */
    private static final long serialVersionUID = -2645395902591679254L;

    private boolean           created;
    private String            userName;
    private String            email;
    private String            id;
    private String            sessionId;

    public Oauth2ServerFlowUserInfoResult() {
    }

    public Oauth2ServerFlowUserInfoResult(Boolean created, String userName, String email, String id) {
        this.setCreated(created);
        this.setUserName(userName);
        this.setEmail(email);
        this.setId(id);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isCreated() {
        return created;
    }

    public void setCreated(boolean created) {
        this.created = created;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

}
