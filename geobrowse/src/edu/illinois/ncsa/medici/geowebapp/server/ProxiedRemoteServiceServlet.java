package edu.illinois.ncsa.medici.geowebapp.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.sead.acr.common.MediciProxy;

public class ProxiedRemoteServiceServlet extends RemoteServiceServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1537463574790795585L;

	protected static String _proxy = "proxy";

	/**
	 * 
	 * @author Jim Myers <myersjd@umich.edu>
	 */

	protected MediciProxy getProxy() {
		MediciProxy mp = null;
		HttpSession session = getThreadLocalRequest().getSession(false);
		if (session != null) {
			mp = (MediciProxy) session.getAttribute(_proxy);
		}
		return mp;
	}

	protected void invalidateSession() {
		HttpSession session = getThreadLocalRequest().getSession(false);
		if (session != null) {
			session.invalidate();
		}
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

}
