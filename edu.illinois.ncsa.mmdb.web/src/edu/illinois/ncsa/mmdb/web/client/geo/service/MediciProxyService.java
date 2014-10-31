package edu.illinois.ncsa.mmdb.web.client.geo.service;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.illinois.ncsa.mmdb.web.common.geo.LayerInfo;
import edu.illinois.ncsa.mmdb.web.common.geo.LocationInfo;

/**
 *
 * @author Jong Lee <jonglee1@illinois.edu>
 *
 */

@RemoteServiceRelativePath("mediciProxy")
public interface MediciProxyService extends RemoteService {

    /**
     * get tags
     * 
     * @return
     */
    String[] getTags();

    /**
     * get layers filtered by tag. if tag is null or "", then, it will return
     * all layers
     * 
     * @param tag
     * @return array of LayerInfo
     */
    LayerInfo[] getLayers(String tag);

    /**
     * get dataset locations filtered by tag. if tag is null or "", then it will
     * return all locations
     * 
     * @param tag
     * @return
     */
    LocationInfo[] getLocations(String tag);

}
