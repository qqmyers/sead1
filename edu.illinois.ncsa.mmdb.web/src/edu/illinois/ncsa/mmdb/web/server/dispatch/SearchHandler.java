/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.cet.search.Hit;
import edu.illinois.ncsa.cet.search.SearchableTextIndex;
import edu.illinois.ncsa.mmdb.web.client.dispatch.Search;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SearchResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

/**
 * Text base search of the repository.
 * 
 * @author Luigi Marini
 *
 */
public class SearchHandler implements ActionHandler<Search, SearchResult> {
	Log log = LogFactory.getLog(SearchHandler.class);
	
	@Override
	public SearchResult execute(Search arg0, ExecutionContext arg1)
			throws ActionException {
		SearchResult searchResult = new SearchResult();
		SearchableTextIndex<String> search = TupeloStore.getInstance().getSearch();
		long then = System.currentTimeMillis();
		Iterable<Hit> result = search.search(arg0.getQuery());
		for (Hit hit : result) {
				searchResult.addHit(hit.getId());
		}
		long elapsed = System.currentTimeMillis() - then;
		log.debug("Search for '"+arg0.getQuery()+"' took "+elapsed+"ms");
		return searchResult;
	}

	@Override
	public Class<Search> getActionType() {
		return Search.class;
	}

	@Override
	public void rollback(Search arg0, SearchResult arg1, ExecutionContext arg2)
			throws ActionException {
		// TODO Auto-generated method stub
		
	}

}