package edu.illinois.ncsa.medici.geowebapp.server;

import java.util.List;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.illinois.ncsa.medici.geowebapp.client.service.MediciProxyService;

/**
 * 
 * @author Jong Lee <jonglee1@illinois.edu>
 * 
 */

@SuppressWarnings("serial")
public class MediciProxyServiceImpl extends RemoteServiceServlet implements
		MediciProxyService {
	@Override
	public String[] getTags() {
		List<String> tags = MediciRestUtil.getTags();
		if (tags.isEmpty())
			return null;
		return tags.toArray(new String[tags.size()]);
	}

}
