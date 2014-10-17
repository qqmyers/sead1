/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010, NCSA.  All rights reserved.
 *
 * Developed by:
 * Cyberenvironments and Technologies (CET)
 * http://cet.ncsa.illinois.edu/
 *
 * National Center for Supercomputing Applications (NCSA)
 * http://www.ncsa.illinois.edu/
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the names of CET, University of Illinois/NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *******************************************************************************/
/**
 *
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpSession;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.sead.acr.common.MediciProxy;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Foaf;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GoogleUserInfo;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GoogleUserInfoResult;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.server.Mail;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;

/**
 * Retrieve Google user info.
 *
 * @author Luigi Marini
 * @author myersjd@umich.edu
 *
 */
public class GoogleUserInfoHandler implements ActionHandler<GoogleUserInfo, GoogleUserInfoResult> {

    /** Commons logging **/
    private static Log                            log            = LogFactory.getLog(GoogleUserInfoHandler.class);

    private static final String                   USER_INFO_URL  = "https://www.googleapis.com/plus/v1/people/me/openIdConnect";
    private static final HttpTransport            HTTP_TRANSPORT = new NetHttpTransport();

    private static final ThreadLocal<HttpSession> session        = new ThreadLocal<HttpSession>();

    public static void setSession(HttpSession session) {
        GoogleUserInfoHandler.session.set(session);
    }

    @Override
    public GoogleUserInfoResult execute(GoogleUserInfo action, ExecutionContext arg1)
            throws ActionException {

        log.debug("Getting user information from google using OAuth2, token = " + action.getToken());

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
    }

    @Override
    public Class<GoogleUserInfo> getActionType() {
        return GoogleUserInfo.class;
    }

    @Override
    public void rollback(GoogleUserInfo arg0, GoogleUserInfoResult arg1,
            ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub
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

}
