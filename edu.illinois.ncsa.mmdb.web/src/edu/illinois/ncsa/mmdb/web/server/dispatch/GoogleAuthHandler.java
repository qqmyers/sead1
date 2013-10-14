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
import java.util.Arrays;
import java.util.Collection;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GoogleUserInfo;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GoogleUserInfoResult;

/**
 * Create a new collection.
 * 
 * @author Luigi Marini
 * 
 */
public class GoogleAuthHandler implements ActionHandler<GoogleUserInfo, GoogleUserInfoResult> {

    /** Commons logging **/
    private static Log                    log            = LogFactory.getLog(GoogleAuthHandler.class);

    private static final String           CLIENT_ID      = "830666247381.apps.googleusercontent.com";
    private static final String           CLIENT_SECRET  = "e0aKfbORDRifU4BpnKabvSoq";
    private static final String           CALLBACK_URI   = "http://localhost:8080/OAuth2v1/index.jsp";
    private static final Iterable<String> SCOPE          = Arrays.asList("https://www.googleapis.com/auth/userinfo.profile;https://www.googleapis.com/auth/userinfo.email".split(";"));
    private static final String           USER_INFO_URL  = "https://www.googleapis.com/oauth2/v1/userinfo";
    private static final JsonFactory      JSON_FACTORY   = new JacksonFactory();
    private static final HttpTransport    HTTP_TRANSPORT = new NetHttpTransport();

    private GoogleAuthorizationCodeFlow   flow;

    @Override
    public GoogleUserInfoResult execute(GoogleUserInfo action, ExecutionContext arg1)
            throws ActionException {

        log.debug("Getting user information from google using OAuth2, token = " + action.getToken());
        flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, CLIENT_ID, CLIENT_SECRET, (Collection<String>) SCOPE).build();

        try {
            GoogleTokenResponse response = flow.newTokenRequest(action.getToken()).setRedirectUri(CALLBACK_URI).execute();
            Credential credential = flow.createAndStoreCredential(response, null);
            HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(credential);
            GenericUrl url = new GenericUrl(USER_INFO_URL);
            HttpRequest request = requestFactory.buildGetRequest(url);
            request.getHeaders().setContentType("application/json");
            String jsonIdentity = request.execute().parseAsString();
            log.debug("User credentials retrieved from Google: " + jsonIdentity);
            return new GoogleUserInfoResult(jsonIdentity);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return new GoogleUserInfoResult();
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

}
