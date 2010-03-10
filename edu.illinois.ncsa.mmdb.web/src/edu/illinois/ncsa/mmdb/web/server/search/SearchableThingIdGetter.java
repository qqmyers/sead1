package edu.illinois.ncsa.mmdb.web.server.search;

import edu.illinois.ncsa.cet.search.IdGetter;

public class SearchableThingIdGetter implements IdGetter<String> {
	@Override
	public String getId(String object) {
		assert object != null;
		return object;
	}

}
