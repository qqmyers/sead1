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

import java.util.List;

import javax.servlet.http.HttpSession;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Foaf;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserPID;
import edu.illinois.ncsa.mmdb.web.client.dispatch.Oauth2ServerFlowUserInfo;
import edu.illinois.ncsa.mmdb.web.client.dispatch.Oauth2ServerFlowUserInfoResult;
import edu.illinois.ncsa.mmdb.web.client.ui.LoginPage;
import edu.illinois.ncsa.mmdb.web.rest.AuthenticatedServlet;
import edu.illinois.ncsa.mmdb.web.server.Authentication;
import edu.illinois.ncsa.mmdb.web.server.Mail;
import edu.illinois.ncsa.mmdb.web.server.OrcidClient;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;

/**
 * Retrieve Google user info based on Oauth2 code (in server-side flow).
 *
 * @author myersjd@umich.edu
 *
 */
public class Oauth2ServerFlowUserInfoHandler implements ActionHandler<Oauth2ServerFlowUserInfo, Oauth2ServerFlowUserInfoResult> {

    /** Commons logging **/
    private static Log                            log     = LogFactory.getLog(Oauth2ServerFlowUserInfoHandler.class);

    private static final ThreadLocal<HttpSession> session = new ThreadLocal<HttpSession>();

    public static void setSession(HttpSession session) {
        Oauth2ServerFlowUserInfoHandler.session.set(session);
    }

    private static final ThreadLocal<String>  orcidId        = new ThreadLocal<String>();
    private static final ThreadLocal<Integer> tokenExpiresAt = new ThreadLocal<Integer>();
    private static final ThreadLocal<String>  theServer      = new ThreadLocal<String>();

    public static void setId(String id) {
        orcidId.set(id);
    }

    public static void setExpiresAt(int expires_at) {
        tokenExpiresAt.set(expires_at);
    }

    public static void setServer(String server) {
        theServer.set(server);

    }

    @Override
    public Oauth2ServerFlowUserInfoResult execute(Oauth2ServerFlowUserInfo action, ExecutionContext arg1)
            throws ActionException {

        if (LoginPage.OrcidProvider.equals(action.getProvider())) {
            String id = orcidId.get();
            Oauth2ServerFlowUserInfoResult result = OrcidClient.requestUserInfo(id, action.getToken());
            result.setId(id);
            int expires_at = tokenExpiresAt.get();
            HttpSession newSession = session.get();
            //Send sessionId to client in an easy to parse form
            result.setSessionId(newSession.getId());

            if (result.getEmail() != null) {
                result.setCreated(checkUsersExists(result.getUserName(), result.getEmail(), result.getId(), action.shouldCreate()));
                if (action.shouldCreate()) {
                    //Do nothing - currently signup does not log you in (since you wouldn't have permissions anyway)
                    log.debug("User creation requested: result: " + result.isCreated());
                } else {
                    //Create a valid session for authenticated user (as if /api/authentication was called)
                    AuthenticatedServlet.fillAuthenticatedSession(newSession, result.getEmail(), expires_at, theServer.get());
                    //Record login
                    Authentication.setLastLogin(Resource.uriRef(PersonBeanUtil.getPersonID(result.getEmail())));
                }
                return result;
            } else {
                //couldn't get email - fail...
                throw new ActionException("no email");
            }
        }
        return null;
    }

    @Override
    public Class<Oauth2ServerFlowUserInfo> getActionType() {
        return Oauth2ServerFlowUserInfo.class;
    }

    private boolean checkUsersExists(String name, String email, String orcidId, boolean accessRequest) {
        log.debug("Checking if user exists " + email);

        Context context = TupeloStore.getInstance().getContext();

        PersonBeanUtil pbu = new PersonBeanUtil(TupeloStore.getInstance()
                .getBeanSession());
        try {
            Unifier u = new Unifier();
            u.setColumnNames("uri");
            u.addPattern("uri", Foaf.MBOX, Resource.literal(email));
            context.perform(u);
            List<Resource> uris = u.getFirstColumn();
            if (uris.size() == 1) {
                log.debug("User in the system " + uris.get(0));
                PersonBean pb = pbu.get(uris.get(0));
                log.debug("User retrieved " + pb.getUri());
                return false;
            } else if (uris.size() == 0) {
                log.debug("User not in the system " + email);
                if (accessRequest) {
                    PersonBean pb = createUser(email, name);
                    TupeloStore.getInstance().getBeanSession().save(pb);
                    Mail.userAdded(pb);
                    //Add orcid ID
                    TripleWriter tw = new TripleWriter();
                    tw.add(Resource.uriRef(pb.getUri()), Resource.uriRef(GetUserPID.userPIDPredicate), orcidId);
                    try {
                        TupeloStore.getInstance().getContext().perform(tw);
                    } catch (OperatorException oe) {
                        log.warn("Could not write id for user");
                    }
                    log.debug("User created " + pb.getUri());
                    return true;
                } else {
                    return false;
                }
            } else {
                log.error("Query returned too many users with email " + email);
                return false;
            }
        } catch (Exception e) {
            log.error("Error retrieving information about user "
                    + email, e);
            return true;
        }
    }

    private PersonBean createUser(String email, String name) {
        //FixMe - name may be null/blank from Google
        PersonBean pb = new PersonBean();
        pb.setUri(PersonBeanUtil.getPersonID(email));
        pb.setEmail(email);
        pb.setName(name);
        return pb;
    }

    @Override
    public void rollback(Oauth2ServerFlowUserInfo action, Oauth2ServerFlowUserInfoResult result, ExecutionContext context) throws ActionException {
        // TODO Auto-generated method stub

    }

}
