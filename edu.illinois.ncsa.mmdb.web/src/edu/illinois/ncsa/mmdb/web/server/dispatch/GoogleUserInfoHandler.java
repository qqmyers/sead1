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
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.Foaf;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GoogleUserInfo;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GoogleUserInfoResult;
import edu.illinois.ncsa.mmdb.web.server.Mail;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;

/**
 * Retrieve Google user info.
 * 
 * @author Luigi Marini
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
            GoogleCredential credential = new GoogleCredential().setAccessToken(action.getToken());
            HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(credential);
            GenericUrl url = new GenericUrl(USER_INFO_URL);
            HttpRequest request = requestFactory.buildGetRequest(url);
            request.getHeaders().setContentType("application/json");
            log.debug("Identity = " + request.execute().parseAsString());
            String identity = request.execute().parseAsString();
            String[] info = parseJson(identity);
            log.debug(info);
            //            Userinfo userInfo = request.execute().parseAs(Userinfo.class);
            //            log.debug("User credentials retrieved from Google: " + userInfo.getEmail());
            boolean created = checkUsersExists(info[0], info[1]);
            // register user with session
            //            session.get().setAttribute(AuthenticatedServlet.AUTHENTICATED_AS, userInfo.getEmail());
            return new GoogleUserInfoResult(created, info[0], info[1]);
        } catch (IOException e) {
            log.error("Error retrieving google user info", e);
            throw new ActionException("Error retrieving google user info");
        }
    }

    private String[] parseJson(String identity) throws JsonParseException, IOException {
        JsonFactory jfactory = new JsonFactory();
        JsonParser jParser = jfactory.createJsonParser(identity);
        String[] info = new String[2];
        while (jParser.nextToken() != JsonToken.END_OBJECT) {
            String fieldname = jParser.getCurrentName();
            if ("name".equals(fieldname)) {
                jParser.nextToken();
                info[0] = jParser.getText();
            }
            if ("email".equals(fieldname)) {
                jParser.nextToken();
                info[1] = jParser.getText();
            }
        }
        return info;
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

    private boolean checkUsersExists(String name, String email) {
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
                PersonBean pb = pbu.get((UriRef) uris.get(0));
                log.debug("User retrieved " + pb.getUri());
                return false;
            } else if (uris.size() == 0) {
                log.debug("User not in the system " + email);
                PersonBean pb = createUser(email, name);
                TupeloStore.getInstance().getBeanSession().save(pb);
                Mail.userAdded(pb);
                log.debug("User created " + pb.getUri());
                return true;
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
