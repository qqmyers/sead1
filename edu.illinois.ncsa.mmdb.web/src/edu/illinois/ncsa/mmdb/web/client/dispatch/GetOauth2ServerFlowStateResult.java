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
 * Get a limited time, secret state token for oath2 server side flow.
 *
 * @author myersjd@umich.edu
 *
 */

public class GetOauth2ServerFlowStateResult implements Result {

    /**
     *
     */
    private static final long serialVersionUID = -2645395902591679254L;

    private String            state;

    public GetOauth2ServerFlowStateResult() {
    }

    public GetOauth2ServerFlowStateResult(String state) {
        this.setState(state);
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

}
