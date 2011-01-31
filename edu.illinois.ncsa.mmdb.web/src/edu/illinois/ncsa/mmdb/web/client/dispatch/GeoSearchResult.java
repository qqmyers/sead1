/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.dispatch.shared.Result;

/**
 * @author lmarini
 * 
 */
@SuppressWarnings("serial")
public class GeoSearchResult implements Result {

    private List<String> hits = new ArrayList<String>();

    public GeoSearchResult() {
    }

    public GeoSearchResult(List<String> hits) {
        this.hits = hits;
    }

    public List<String> getHits() {
        return hits;
    }

    public void addHit(String resource) {
        hits.add(resource);
    }
}
