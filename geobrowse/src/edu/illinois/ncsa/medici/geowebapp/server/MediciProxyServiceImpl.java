package edu.illinois.ncsa.medici.geowebapp.server;


import java.util.List;

import edu.illinois.ncsa.medici.geowebapp.client.service.MediciProxyService;

import org.sead.acr.common.MediciProxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Jong Lee <jonglee1@illinois.edu>
 * @author Jim Myers <myersjd@umich.edu>
 */

@SuppressWarnings("serial")
public class MediciProxyServiceImpl extends ProxiedRemoteServiceServlet implements
		MediciProxyService {

	protected static Log log = LogFactory.getLog(MediciProxyServiceImpl.class);

	@Override
	public String[] getTags() {
		
		dontCache();
		
		List<String> tags=null;
		try {
		MediciProxy mp = getProxy();
		tags = MediciRestUtil.getTags(mp);
		} catch (Exception e) {
			invalidateSession();
			log.warn("Error contacting medici: " + e);
		}
		if ((tags==null) || (tags.isEmpty())) {
			return null;
		} else {
		return tags.toArray(new String[tags.size()]);
		}
	}
}