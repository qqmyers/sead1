package edu.illinois.ncsa.mmdb.web.rest;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

/**
 * Servlet responsible for handling the extractor requests.
 * 
 * @author Rob Kooper
 * 
 */
@SuppressWarnings( { "serial" })
public class ExtractorServlet extends AuthenticatedServlet {
    public ExtractorServlet() {
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!authenticate(req, resp)) {
            return;
        }
        String uri = req.getParameter("uri");
        String result = TupeloStore.getInstance().extractPreviews(uri);

        resp.setStatus(200);
        PrintWriter pw = new PrintWriter(resp.getOutputStream());
        pw.println(result); //$NON-NLS-1$
        pw.flush();
    }
}
