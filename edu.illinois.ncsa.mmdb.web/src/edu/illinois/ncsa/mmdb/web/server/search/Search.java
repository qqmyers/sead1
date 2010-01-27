package edu.illinois.ncsa.mmdb.web.server.search;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.tupeloproject.rdf.terms.Dc;

import edu.illinois.ncsa.cet.search.Hit;
import edu.illinois.ncsa.cet.search.Searchable;
import edu.illinois.ncsa.cet.search.StringHit;
import edu.illinois.ncsa.cet.search.TextIndex;
import edu.illinois.ncsa.mmdb.web.server.dispatch.ListDatasetsHandler;

public class Search extends TextIndex<Object> implements Searchable {
	@Override
	public Iterable<Hit> search(String searchString) {
		// here we just list datasets
		return search(searchString, 0, 30);
	}

	@Override
	public Iterable<Hit> search(String searchString, int limit) {
		// TODO Auto-generated method stub
		return search(searchString, limit, 0);
	}

	@Override
	public Iterable<Hit> search(String searchString, int limit, int offset) {
		// TODO Auto-generated method stub
		List<Hit> result = new LinkedList<Hit>();
		// TODO don't rely on ListDatasetsHandler, move that impl code into a common class
		for(String uri : ListDatasetsHandler.listDatasetUris(Dc.DATE.getString(), true, limit, offset, null)) {
			result.add(new StringHit(uri));
		}
		return result;
	}

	@Override
	public void deindex(String id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void index(String id, String text) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Iterator<String> iterator() throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return null;
	}
}
