/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.dispatch.shared.Result;

/**
 * Text based search result.
 * 
 * @author Luigi Marini
 *
 */
@SuppressWarnings("serial")
public class SearchResult implements Result {

	private List<String> hits = new ArrayList<String>();
	
	public SearchResult() {}
	
	public SearchResult(List<String> hits) {
		this.hits = hits;
	}
	
	public void addHit(String id) {
		getHits().add(id);
	}

	public void setHits(List<String> hits) {
		this.hits = hits;
	}

	public List<String> getHits() {
		return hits;
	}
}
