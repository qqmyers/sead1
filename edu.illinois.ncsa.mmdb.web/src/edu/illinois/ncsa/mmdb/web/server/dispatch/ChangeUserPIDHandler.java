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
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.mmdb.web.client.dispatch.ChangeUserPID;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserPID;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

/**
 * Create new user external PID and remove old.
 * 
 * @author Jim Myers
 * 
 */
public class ChangeUserPIDHandler implements ActionHandler<ChangeUserPID, EmptyResult> {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(ChangeUserPIDHandler.class);

    @Override
    public EmptyResult execute(ChangeUserPID action, ExecutionContext arg1) throws ActionException {
        Resource user = Resource.uriRef(action.getUserUri());

        TripleWriter tw = new TripleWriter();
        String newPID = action.getNewPID();
        if ((newPID != null) && (newPID.length() > 0)) {
            tw.add(user, Resource.uriRef(GetUserPID.userPIDPredicate), newPID);
        }
        String oldPID = action.getOldPID();
        if ((oldPID != null) && (oldPID.length() > 0)) {

            tw.remove(user, Resource.uriRef(GetUserPID.userPIDPredicate), oldPID);
        }
        try {
            TupeloStore.getInstance().getContext().perform(tw);
        } catch (Exception e) {
            log.warn("Could not update user PID.", e);
            throw (new ActionException("Could not update user PID.", e));
        }
        return new EmptyResult();

    }

    @Override
    public Class<ChangeUserPID> getActionType() {
        return ChangeUserPID.class;
    }

    @Override
    public void rollback(ChangeUserPID arg0, EmptyResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub
    }

}
