/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;


import net.customware.gwt.dispatch.shared.Action;

/**
 * Text based search of the repository.
 * 
 * @author Luigi Marini
 *
 */
@SuppressWarnings("serial")
public class Search implements Action<SearchResult> {

	private String query;

	public Search() {}
	
	public Search(String query) {
		this.query = query;
	}

	public String getQuery() {
		return query;
	}
}
