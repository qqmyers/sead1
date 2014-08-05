/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Handles client OAuth redirection and access token requests.
 * 
 * For now it only supports Orcid.
 * 
 * @author Luigi Marini
 * 
 */
@SuppressWarnings("serial")
public class OAuth extends HttpServlet {

    private static final String DEFAULT_PROVIDER = "orcid";
    private static Log          log              = LogFactory.getLog(OAuth.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // extract provider from url
        String url = req.getRequestURL().toString();
        log.debug("Requested " + url);
        Pattern pattern = Pattern.compile(".*/oauth/(.+)");
        Matcher matcher = pattern.matcher(url);
        matcher.find();
        String provider = DEFAULT_PROVIDER;
        try {
            provider = matcher.group(1);
        } catch (IllegalStateException ex) {
            log.debug("Defaulting to default provider for oauth redirection. Default provider is " + DEFAULT_PROVIDER);
        }
        log.debug("Provider is " + provider);

        // extract parameters from url
        String code = req.getParameter("code");
        String state = req.getParameter("state");
        String error = req.getParameter("error");

        // print response
        PrintWriter out = resp.getWriter();
        log.debug("code = " + code + ", state = " + state + ", error = " + error);
        if (error != null) {
            log.debug("There was an error " + error);
            out.println("Error");
            resp.setStatus(200);
        } else if (code != null) {
            log.debug("Code submitted");
            if (provider.equals("orcid")) {
                requestOrcidAccessToken(code);
                resp.setStatus(200);
                out.println("Success");
            } else {
                log.debug("Provider not recognized");
                resp.setStatus(500);
                out.println("Provider not recognized");
            }
        } else {
            log.error("Neither an error or a valid code was submitted to /oauth endpoint");
            out.println("Error");
            resp.setStatus(500);
        }
    }

    private void requestOrcidAccessToken(String code) {
        log.debug("Requesting orcid access token");
    }
}
