package edu.illinois.ncsa.mmdb.web.server.dashboard;

import org.sead.acr.common.SparqlQueryServlet;
import org.sead.acr.common.utilities.Queries;

public class GetCollections extends SparqlQueryServlet {

    /**
	 *
	 */
    private static final long serialVersionUID = -8642016544488942571L;

    protected String getQuery(String tagID) {
        if (tagID == null) {
            return Queries.ALL_TOPLEVEL_COLLECTIONS;
        } else {
            return Queries.getCollectionContents(tagID);
        }
    }
}
