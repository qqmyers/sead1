package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Action;

public class RunSparqlQuery implements Action<RunSparqlQueryResult> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7861005992961939168L;
	
	private String query;

	public void setQuery(String query) {
		this.query = query;
	}

	public String getQuery() {
		return query;
	}
}
