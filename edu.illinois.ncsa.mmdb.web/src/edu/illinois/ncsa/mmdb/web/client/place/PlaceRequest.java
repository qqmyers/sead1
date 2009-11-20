/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.place;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lmarini
 *
 */
public class PlaceRequest {

	private static final String PARAMETERS_SPLIT = "?";

	private static final String PARAMETER_ASSIGNMENT = "=";

	private final Place id;
	
    private final Map<String, String> params;
	
    public PlaceRequest(Place id, Map<String, String> params) {
		this.id = id;
		this.params = params;
	}

	public PlaceRequest(String value) {
		int split = value.indexOf(PARAMETERS_SPLIT);
		id = new Place(value.substring(0, split));
		String[] paramsString = value.substring(split+1).split(PARAMETER_ASSIGNMENT);
		params = new HashMap<String, String>();
		for (int i=0; i < paramsString.length; i++) {
			params.put(paramsString[i], paramsString[i+1]);
		}
	}

	public Map<String, String> getParams() {
		return params;
	}

	public String getParameter(String id) {
		return params.get(id);
	}

	public Place getId() {
		return id;
	}
    
}
