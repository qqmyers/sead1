package org.sead.acr.dashboard;

import org.sead.acr.common.SparqlQueryServlet;
import org.sead.acr.common.utilities.Queries;

public class CollectionContents extends SparqlQueryServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5202731150389612465L;

	protected String getQuery(String tagID) {
		return (Queries.getCollectionContents(tagID));
	}

}
