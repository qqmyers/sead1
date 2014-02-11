package edu.illinois.ncsa.medici.geowebapp.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LayerInfo implements IsSerializable {
	// title: friendly name of the dataset
	String title;
	
	// name: layer name used in geoserver
	String name;
	
	// srs: spatial reference system (e.g. EPSG:4326)
	String srs;
	
	// uri: dataset uri of the dataset
	String uri;
	
	// bounding box (minx, miny), (maxx, maxy)
	double minx;
	double miny;
	double maxx;
	double maxy;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSrs() {
		return srs;
	}

	public void setSrs(String srs) {
		this.srs = srs;
	}

	public double getMinx() {
		return minx;
	}

	public void setMinx(double minx) {
		this.minx = minx;
	}

	public double getMiny() {
		return miny;
	}

	public void setMiny(double miny) {
		this.miny = miny;
	}

	public double getMaxx() {
		return maxx;
	}

	public void setMaxx(double maxx) {
		this.maxx = maxx;
	}

	public double getMaxy() {
		return maxy;
	}

	public void setMaxy(double maxy) {
		this.maxy = maxy;
	}

	public String toString() {
		return this.name + " (" + this.srs + "): " + minx + ", " + miny + " "
				+ maxx + ", " + maxy;
	}

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
}
