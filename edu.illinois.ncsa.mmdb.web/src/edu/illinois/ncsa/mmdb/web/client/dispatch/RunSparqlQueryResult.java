package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.List;

import net.customware.gwt.dispatch.shared.Result;

public class RunSparqlQueryResult implements Result {
	/**
	 * 
	 */
	private static final long serialVersionUID = 486461734263672506L;
	
	private List<List<String>> result;

	public void setResult(List<List<String>> result) {
		this.result = result;
	}

	public List<List<String>> getResult() {
		return result;
	}
	
	
}
