package edu.illinois.ncsa.medici.geowebapp.server;

import java.util.List;

import edu.illinois.ncsa.medici.geowebapp.client.service.MediciProxyService;
import edu.illinois.ncsa.medici.geowebapp.shared.LayerInfo;
import edu.illinois.ncsa.medici.geowebapp.shared.LocationInfo;

import org.sead.acr.common.MediciProxy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
			invalidateSession();
			log.warn("getTags: Error contacting medici: " + e);
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
	 * edu.illinois.ncsa.medici.geowebapp.client.service.MediciProxyService#
	 * getLayers(java.lang.String)
	 */
	@Override
	public LayerInfo[] getLayers(String tag) {
		dontCache();

		List<LayerInfo> layers = null;

		try {
			MediciProxy mp = getProxy();
			if (tag == null || "".equals(tag)) {
				layers = MediciRestUtil.getLayers(mp);
			} else {
				layers = MediciRestUtil.getLayersByTag(tag, mp);
			}
		} catch (Exception e) {
			invalidateSession();
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
	 * edu.illinois.ncsa.medici.geowebapp.client.service.MediciProxyService#
	 * getLocations(java.lang.String)
	 */
	@Override
	public LocationInfo[] getLocations(String tag) {
		dontCache();

		List<LocationInfo> locations = null;

		try {
			MediciProxy mp = getProxy();
			if (tag == null || "".equals(tag)) {
				locations = MediciRestUtil.getLocations(mp);
			} else {
				locations = MediciRestUtil.getLocationsByTag(tag, mp);
			}
		} catch (Exception e) {
			invalidateSession();
			log.warn("getLocations - Error contacting medici or JSON has the error: " + e);
		}
		if ((locations == null) || (locations.isEmpty())) {
			return null;
		} else {
			return locations.toArray(new LocationInfo[locations.size()]);
		}
	}

}