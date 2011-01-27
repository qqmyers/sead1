package edu.illinois.ncsa.mmdb.web.server.rest;

import java.net.URLDecoder;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Cookie;
import org.restlet.security.User;
import org.restlet.security.Verifier;

import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;

/**
 * This class is responsible for checking to see if the user is authenticated.
 * 
 * FIXME this class needs correct implementation. One option is to always allow
 * authentication to pass and either be anonymous or authenticated.
 * 
 * @author Rob Kooper
 * 
 */
public class MediciAuthentication implements Verifier {
    @Override
    public int verify(Request request, Response response) {
        // Check valid username/passowrd/cookie
        //String username = request.getChallengeResponse().getIdentifier();
        //String password = new String(request.getChallengeResponse().getSecret());
        //if (false) {
        //    return Verifier.RESULT_MISSING;
        //}

        // FIXME hack
        String userid = PersonBeanUtil.getAnonymousURI().getString();
        for (Cookie c : request.getCookies() ) {
            if (c.getName().equals("sid")) {
                userid = URLDecoder.decode(c.getValue());
            }
        }

        // set the user
        request.getClientInfo().setUser(new User(userid));

        // all done
        return Verifier.RESULT_VALID;
    }
}
