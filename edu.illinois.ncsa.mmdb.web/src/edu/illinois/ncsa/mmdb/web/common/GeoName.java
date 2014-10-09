package edu.illinois.ncsa.mmdb.web.common;

import java.io.Serializable;

/**
 * The bean to hold the information about geo names
 * 
 * @author Jong Lee
 *
 */
public class GeoName implements Serializable {

    private static final long serialVersionUID = 652067111846561437L;

    String                    name;
    double                    lat;
    double                    lng;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

}
