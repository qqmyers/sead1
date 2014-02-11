package org.sead.acr.discovery;

import org.sead.acr.common.SparqlQueryServlet;
import org.sead.acr.common.utilities.Queries;

public class GetPublishedCollections extends SparqlQueryServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2553450304238410849L;

	protected String getQuery(String tagID) {
		return Queries.ALL_PUBLISHED_COLLECTIONS;
	}
}
