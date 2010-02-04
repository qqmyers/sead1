/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.Collection;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetGeoPoint;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetGeoPointResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.gis.GeoPointBean;
import edu.uiuc.ncsa.cet.bean.tupelo.gis.GeoPointBeanUtil;

/**
 * Get geopoints attached to dataset.
 * 
 * @author Luigi Marini
 * 
 */
public class GetGeoPointHandler implements
		ActionHandler<GetGeoPoint, GetGeoPointResult> {

	/** Commons logging **/
	private static Log log = LogFactory.getLog(GetGeoPointHandler.class);

	@Override
	public GetGeoPointResult execute(GetGeoPoint arg0, ExecutionContext arg1)
			throws ActionException {

		BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

		try {
			// geoloc
			GeoPointBeanUtil gpbu = new GeoPointBeanUtil(beanSession);
			Collection<GeoPointBean> geoloc = gpbu.getAssociationsFor(arg0
					.getUri());
			for (GeoPointBean bean : geoloc) {
				log.debug("GeoLoc " + bean.getLatitude() + " "
						+ bean.getLongitude() + " " + bean.getAltitude());
			}
			if (geoloc.isEmpty()) {
				log.debug("No geoloc available for " + arg0.getUri());
			}
			return new GetGeoPointResult(geoloc);
		} catch (Exception e) {
			log.error("Error getting geopoints for " + arg0.getUri(), e);
			throw new ActionException(e);
		}

	}

	@Override
	public Class<GetGeoPoint> getActionType() {
		return GetGeoPoint.class;
	}

	@Override
	public void rollback(GetGeoPoint arg0, GetGeoPointResult arg1,
			ExecutionContext arg2) throws ActionException {
		// TODO Auto-generated method stub

	}

}
