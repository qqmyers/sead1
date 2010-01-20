package edu.illinois.ncsa.mmdb.web.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tupeloproject.server.HttpTupeloServlet;
import org.tupeloproject.kernel.Context;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import edu.illinois.ncsa.mmdb.web.rest.AuthenticatedServlet;

/**
 * TupeloServlet
 */
public class TupeloServlet extends HttpTupeloServlet {
    Log log = LogFactory.getLog(TupeloServlet.class);

    @Override
    public Context getContext() {
        Context context = TupeloStore.getInstance().getContext();
        log.info("Tupelo Servlet got context "+context);
        return context;
    }

	@Override
	public void doDelete(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		if(!AuthenticatedServlet.doAuthenticate(arg0,arg1)) {
			return;
		}
		super.doDelete(arg0, arg1);
	}

	@Override
	public void doGet(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		if(!AuthenticatedServlet.doAuthenticate(arg0,arg1)) {
			return;
		}
		super.doGet(arg0, arg1);
	}

	@Override
	public void doPost(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		if(!AuthenticatedServlet.doAuthenticate(arg0,arg1)) {
			return;
		}
		super.doPost(arg0, arg1);
	}

	@Override
	public void doPut(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		if(!AuthenticatedServlet.doAuthenticate(arg0,arg1)) {
			return;
		}
		super.doPut(arg0, arg1);
	}
}