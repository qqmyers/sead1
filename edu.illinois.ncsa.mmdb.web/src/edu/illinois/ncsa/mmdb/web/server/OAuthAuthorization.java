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

import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;

/**
 * Redirect user to appropriate login endpoint on external service.
 * 
 * @author Luigi Marini
 * 
 */
@SuppressWarnings("serial")
public class OAuthAuthorization extends HttpServlet {

    private static final String DEFAULT_PROVIDER = "orcid";
    private static Log          log              = LogFactory.getLog(OAuthAuthorization.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // extract provider from url
        String url = req.getRequestURL().toString();
        log.debug("Requested " + url);
        Pattern pattern = Pattern.compile(".*/oauth2/auth/(.+)");
        Matcher matcher = pattern.matcher(url);
        matcher.find();
        String provider = DEFAULT_PROVIDER;
        try {
            provider = matcher.group(1);
        } catch (IllegalStateException ex) {
            log.debug("Defaulting to default provider for oauth redirection. Default provider is " + DEFAULT_PROVIDER);
        }
        log.debug("Provider is " + provider);

        if (provider.equals(DEFAULT_PROVIDER)) {
            String clientId = TupeloStore.getInstance().getConfiguration(ConfigurationKey.OrcidClientId);
            String protocol = "http:";
            if (req.isSecure()) {
                protocol = "https:";
            }
            String redirectURI = protocol + "//" + req.getServerName() + ":" + req.getServerPort() + "/oauth2callback/orcid";
            String orcidURL = OrcidClient.authenticationURL(clientId, redirectURI);
            log.debug("Redirecting to " + orcidURL);
            resp.sendRedirect(orcidURL);
        } else {
            PrintWriter out = resp.getWriter();
            log.debug("Provider not recognized");
            resp.setStatus(500);
            out.println("Provider not recognized");
        }
    }
}
