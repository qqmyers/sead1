package edu.illinois.ncsa.mmdb.web.server.geo;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.sead.acr.common.MediciProxy;
import org.sead.acr.common.utilities.PropertiesLoader;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ProxiedRemoteServiceServlet extends RemoteServiceServlet {

    /**
	 *
	 */
    private static final long serialVersionUID = 1537463574790795585L;

    protected static String   _proxy           = "proxy";

    // == edu.illinois.ncsa.mmdb.web.rest.AuthenticatedServlet.AUTHENTICATED_AS
    private static String     AUTHENTICATED_AS = "edu.illinois.ncsa.mmdb.web.server.auth.authenticatedAs";

    private static String     _server;
    private static String     _geoserver;
    private static String     _proxiedgeoserver;

    /**
     *
     * @author Jim Myers <myersjd@umich.edu>
     */

    protected MediciProxy getProxy() {
        MediciProxy mp = null;
        HttpSession session = getThreadLocalRequest().getSession(false);
        if (session != null) {
            mp = (MediciProxy) session.getAttribute(_proxy);
            if (mp == null) {
                mp = new MediciProxy();
                getRemoteServerProperties();
                mp.setLocalCredentials(session.getId(), _server, null);
                mp.setGeoCredentials(null, null, _geoserver);
                session.setAttribute(_proxy, mp);
            }
        }
        return mp;
    }

    void dontCache() {
        HttpServletResponse response = getThreadLocalResponse();
        // OK, we REALLY don't want the browser to cache this. For reals
        response.addHeader("cache-control",
                "no-store, no-cache, must-revalidate, max-age=-1"); // don't
                                                                    // cache
        response.addHeader("cache-control", "post-check=0, pre-check=0, false"); // really
                                                                                 // don't
                                                                                 // cache
        response.addHeader("pragma", "no-cache, no-store"); // no, we mean it,
                                                            // really don't
                                                            // cache
        response.addHeader("expires", "-1"); // if you cache, we're going to be
                                             // very, very angry
    }

    private void getRemoteServerProperties() {

        if (_server == null) {

            // Find Properties file and retrieve the domain/sparql endpoint of
            // the
            // remote Medici instance
            _server = PropertiesLoader.getProperties().getProperty("domain");
            _proxiedgeoserver = PropertiesLoader.getProperties().getProperty("proxiedgeoserver");
            _geoserver = PropertiesLoader.getProperties().getProperty("geoserver");
        }
    }

    public String[] getUrls() {
        dontCache();
        getRemoteServerProperties();
        String[] urls = new String[2];
        urls[1] = _server;
        urls[0] = _geoserver;
        urls[0] = urls[0] + "/wms";

        return urls;
    }

    public static String getProxiedGeoServer() {
        String server = _proxiedgeoserver;
        if (server == null) {
            server = _geoserver;
        }
        return server;
    }
}
