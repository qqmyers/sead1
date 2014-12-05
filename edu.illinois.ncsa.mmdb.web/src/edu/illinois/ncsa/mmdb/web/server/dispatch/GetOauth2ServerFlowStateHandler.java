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
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetOauth2ServerFlowState;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetOauth2ServerFlowStateResult;
import edu.illinois.ncsa.mmdb.web.server.TokenStore;

/**
 * Retrieve secret state 'key' for use in Oauth2 (in server-side flow).
 *
 * @author myersjd@umich.edu
 *
 */
public class GetOauth2ServerFlowStateHandler implements ActionHandler<GetOauth2ServerFlowState, GetOauth2ServerFlowStateResult> {

    /** Commons logging **/
    private static Log         log          = LogFactory.getLog(GetOauth2ServerFlowStateHandler.class);

    public static final String OAUTH2_STATE = "Oauth2State";

    @Override
    public GetOauth2ServerFlowStateResult execute(GetOauth2ServerFlowState action, ExecutionContext arg1)
            throws ActionException {
        String tokenString = TokenStore.generateToken(OAUTH2_STATE);
        log.debug("Generated state token: " + tokenString);
        return new GetOauth2ServerFlowStateResult(tokenString);
    }

    @Override
    public Class<GetOauth2ServerFlowState> getActionType() {
        return GetOauth2ServerFlowState.class;
    }

    @Override
    public void rollback(GetOauth2ServerFlowState action, GetOauth2ServerFlowStateResult result, ExecutionContext context) throws ActionException {
        // TODO Auto-generated method stub

    }

}
