package edu.illinois.ncsa.mmdb.web.client.place;

/**
 * 
 * @author lmarini
 *
 */
public class Place {
	
	private final String id;
	
	public Place(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Place) {
			Place place = (Place) obj;
			return id.equals(place.id);
		} else {
		return false;
		}
	}
}
