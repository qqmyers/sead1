package edu.illinois.ncsa.mmdb.web.server;

import org.tupeloproject.server.HttpTupeloServlet;
import org.tupeloproject.kernel.Context;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

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
}