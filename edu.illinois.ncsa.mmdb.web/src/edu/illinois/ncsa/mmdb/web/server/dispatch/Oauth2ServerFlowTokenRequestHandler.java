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
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Foaf;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

import edu.illinois.ncsa.mmdb.web.client.dispatch.Oauth2ServerFlowTokenRequest;
import edu.illinois.ncsa.mmdb.web.client.dispatch.Oauth2ServerFlowTokenRequestResult;
import edu.illinois.ncsa.mmdb.web.client.ui.LoginPage;
import edu.illinois.ncsa.mmdb.web.server.Mail;
import edu.illinois.ncsa.mmdb.web.server.OrcidClient;
import edu.illinois.ncsa.mmdb.web.server.TokenStore;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;

/**
 * Retrieve Google user info based on Oauth2 code (in server-side flow).
 *
 * @author myersjd@umich.edu
 *
 */
public class Oauth2ServerFlowTokenRequestHandler implements ActionHandler<Oauth2ServerFlowTokenRequest, Oauth2ServerFlowTokenRequestResult> {

    /** Commons logging **/
    private static Log                            log            = LogFactory.getLog(Oauth2ServerFlowTokenRequestHandler.class);

    private static final String                   USER_INFO_URL  = "https://www.googleapis.com/plus/v1/people/me/openIdConnect";
    private static final HttpTransport            HTTP_TRANSPORT = new NetHttpTransport();

    private static final ThreadLocal<HttpSession> session        = new ThreadLocal<HttpSession>();

    public static void setSession(HttpSession session) {
        Oauth2ServerFlowTokenRequestHandler.session.set(session);
    }

    @Override
    public Oauth2ServerFlowTokenRequestResult execute(Oauth2ServerFlowTokenRequest action, ExecutionContext arg1)
            throws ActionException {

        /*
        log.debug("Getting user information from google using OAuth2, token = " + action.getCode());

        try {
            //First verify token is for us
            String[] client_ids = new String[1];
            client_ids[0] = TupeloStore.getInstance().getConfiguration(ConfigurationKey.GoogleClientId);
            JSONObject tokenInfo = MediciProxy.getValidatedGoogleToken(client_ids, action.getToken());
            if (tokenInfo != null) {
                int ttl = tokenInfo.getInt("expires_in");
                int exp = ttl + (int) (System.currentTimeMillis() / 1000L);

                //Now get user info, which validates the token in the process
                GoogleCredential credential = new GoogleCredential().setAccessToken(action.getToken());
                HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(credential);
                GenericUrl url = new GenericUrl(USER_INFO_URL);
                HttpRequest request = requestFactory.buildGetRequest(url);
                request.getHeaders().setContentType("application/json");
                String result = request.execute().parseAsString();
                JSONObject personInfo = new JSONObject(result);
                log.debug(personInfo.toString());
                boolean verified = personInfo.getBoolean("email_verified");
                if (verified) {
                    boolean created = checkUsersExists(personInfo.getString("name"), personInfo.getString("email"), action.isAccessRequest());
                    return new GoogleUserInfoResult(created, personInfo.getString("name"), personInfo.getString("email"), exp);
                }
            }
            //Something's not right - wrong audience, expired token, email not yet verified by google
            throw new ActionException("Invalid google user");

        } catch (IOException e) {
            log.error("Error retrieving google user info", e);
            throw new ActionException("Error retrieving google user info");
        } catch (JSONException e) {
            log.error("Error parsing google tokenInfo", e);
            throw new ActionException("Error parsing Google tokeninfo");
        }
        */
        log.debug("Received state token: " + action.getState());
        if (TokenStore.isValidToken(action.getState(), GetOauth2ServerFlowStateHandler.OAUTH2_STATE)) {
            log.debug("Validated");
            if (LoginPage.OrcidProvider.equals(action.getProvider())) {
                log.debug("Orcid Provider");
                Oauth2ServerFlowTokenRequestResult requestResult = OrcidClient.requestAccessToken(action.getCode());
                session.get().setAttribute("orcid_id", requestResult.getId());
                session.get().setAttribute("expires_at", requestResult.getExpirationTime());
                return requestResult;
            }
        }
        return null;

    }

    @Override
    public Class<Oauth2ServerFlowTokenRequest> getActionType() {
        return Oauth2ServerFlowTokenRequest.class;
    }

    private boolean checkUsersExists(String name, String email, boolean accessRequest) {
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
    public void rollback(Oauth2ServerFlowTokenRequest action, Oauth2ServerFlowTokenRequestResult result, ExecutionContext context) throws ActionException {
        // TODO Auto-generated method stub

    }

}
