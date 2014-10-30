package edu.illinois.ncsa.mmdb.web.server.geo;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sead.acr.common.MediciProxy;

import edu.illinois.ncsa.mmdb.web.client.geo.service.MediciProxyService;
import edu.illinois.ncsa.mmdb.web.common.geo.LayerInfo;
import edu.illinois.ncsa.mmdb.web.common.geo.LocationInfo;

/**
 *
 * @author Jong Lee <jonglee1@illinois.edu>
 * @author Jim Myers <myersjd@umich.edu>
 */

@SuppressWarnings("serial")
public class MediciProxyServiceImpl extends ProxiedRemoteServiceServlet
        implements MediciProxyService {

    protected static Log log = LogFactory.getLog(MediciProxyServiceImpl.class);

    @Override
    public String[] getTags() {

        dontCache();

        List<String> tags = null;
        try {
            MediciProxy mp = getProxy();
            tags = MediciRestUtil.getTags(mp);
        } catch (Exception e) {
            log.warn("Error contacting medici ", e);
        }
        if ((tags == null) || (tags.isEmpty())) {
            return null;
        } else {
            return tags.toArray(new String[tags.size()]);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.illinois.ncsa.mmdb.web.client.geo.service.MediciProxyService#
     * getLayers(java.lang.String)
     */
    @Override
    public LayerInfo[] getLayers(String tag) {
        dontCache();

        List<LayerInfo> layers = null;

        try {
            MediciProxy mp = getProxy();
            layers = MediciRestUtil.getLayersByTag(tag, mp);
        } catch (Exception e) {
            log.warn("getLayers - Error contacting medici or JSON has the error: ", e);
        }
        if ((layers == null) || (layers.isEmpty())) {
            return null;
        } else {
            return layers.toArray(new LayerInfo[layers.size()]);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.illinois.ncsa.mmdb.web.client.geo.service.MediciProxyService#
     * getLocations(java.lang.String)
     */
    @Override
    public LocationInfo[] getLocations(String tag) {
        dontCache();

        List<LocationInfo> locations = null;

        try {
            MediciProxy mp = getProxy();
            locations = MediciRestUtil.getLocationsByTag(tag, mp);
        } catch (Exception e) {
            log.warn("getLocations - Error contacting medici or JSON has the error: " + e);
        }
        if ((locations == null) || (locations.isEmpty())) {
            return null;
        } else {
            return locations.toArray(new LocationInfo[locations.size()]);
        }
    }

}
