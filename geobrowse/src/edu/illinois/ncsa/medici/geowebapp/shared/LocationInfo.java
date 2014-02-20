package edu.illinois.ncsa.medici.geowebapp.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LocationInfo implements IsSerializable {
	// title: friendly name of the dataset
	String title;
	
	// uri: dataset uri of the dataset
	String uri;
	
	// lat
	double lat;
	
	// lon
	double lon;

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}
}
