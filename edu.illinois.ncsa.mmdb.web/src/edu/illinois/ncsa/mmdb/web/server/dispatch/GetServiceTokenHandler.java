/*******************************************************************************
 * Copyright 2014 University of Michigan

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 *******************************************************************************/
/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetServiceToken;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetServiceTokenResult;
import edu.illinois.ncsa.mmdb.web.server.TokenStore;

/**
 * Get limited-time access token
 * 
 * @author Jim Myers
 */
public class GetServiceTokenHandler implements ActionHandler<GetServiceToken, GetServiceTokenResult> {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(GetServiceTokenHandler.class);

    @Override
    public Class<GetServiceToken> getActionType() {
        return GetServiceToken.class;
    }

    @Override
    public GetServiceTokenResult execute(GetServiceToken action, ExecutionContext context) throws ActionException {
        GetServiceTokenResult gstr = new GetServiceTokenResult();
        String method = action.getMethod();
        String token = TokenStore.generateToken(method);
        gstr.setToken(token);
        return gstr;
    }

    @Override
    public void rollback(GetServiceToken action, GetServiceTokenResult result, ExecutionContext context) throws ActionException {
        // TODO Auto-generated method stub

    }

}
