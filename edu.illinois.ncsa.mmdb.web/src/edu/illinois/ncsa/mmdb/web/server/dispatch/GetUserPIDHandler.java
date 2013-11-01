/*******************************************************************************
 * Copyright 2013 University of Michigan

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
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleMatcher;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserPID;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserPIDResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

/**
 * Get user external PID.
 * 
 * @author Jim Myers
 */
public class GetUserPIDHandler implements ActionHandler<GetUserPID, GetUserPIDResult> {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(GetUserPIDHandler.class);

    @Override
    public Class<GetUserPID> getActionType() {
        return GetUserPID.class;
    }

    @Override
    public GetUserPIDResult execute(GetUserPID action, ExecutionContext context) throws ActionException {
        GetUserPIDResult gup = new GetUserPIDResult();
        Resource userUri = Resource.uriRef(action.getUserUri());
        TripleMatcher tm = new TripleMatcher();
        tm.setSubject(userUri);
        tm.setPredicate(Resource.uriRef(GetUserPID.userPIDPredicate));
        try {
            TupeloStore.getInstance().getContext().perform(tm);
        } catch (OperatorException e) {
            log.info(e.getStackTrace().toString());
            throw new ActionException(e.getMessage());
        }
        log.debug("Found " + tm.getResult().size() + " triples");
        for (Triple triple : tm.getResult() ) {
            if (triple.getObject() != null) {
                //Only expecting 1 - will take first one
                String obj = triple.getObject().getString();
                log.debug(obj);
                gup.setUserPID(obj);
            }
        }
        return gup;

    }

    @Override
    public void rollback(GetUserPID action, GetUserPIDResult result, ExecutionContext context) throws ActionException {
        // TODO Auto-generated method stub

    }

}
