package edu.illinois.ncsa.mmdb.web.server.dashboard;

import org.sead.acr.common.SparqlQueryServlet;
import org.sead.acr.common.utilities.Queries;

public class GetTeamMembers extends SparqlQueryServlet {

    /**
	 *
	 */
    private static final long serialVersionUID = -8642016544488942571L;

    protected String getQuery(String tagID) {
        return Queries.TEAM_MEMBERS;
    }
}
