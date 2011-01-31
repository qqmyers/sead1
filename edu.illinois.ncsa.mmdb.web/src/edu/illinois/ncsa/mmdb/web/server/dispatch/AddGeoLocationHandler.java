package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;

import edu.illinois.ncsa.mmdb.web.client.dispatch.AddGeoLocation;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.gis.GeoPointBean;
import edu.uiuc.ncsa.cet.bean.tupelo.gis.GeoPointBeanUtil;

/**
 * Associate resource with a geolocation.
 * 
 * @author Luigi Marini <lmarini@ncsa.illinois.edu>
 * 
 */
public class AddGeoLocationHandler implements ActionHandler<AddGeoLocation, EmptyResult> {
    Log log = LogFactory.getLog(AddGeoLocationHandler.class);

    @Override
    public EmptyResult execute(AddGeoLocation arg0, ExecutionContext arg1) throws ActionException {
        BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

        try {
            // geoloc
            GeoPointBeanUtil gpbu = new GeoPointBeanUtil(beanSession);
            GeoPointBean geoPoint = new GeoPointBean();
            geoPoint.setLatitude((float) arg0.getLat());
            geoPoint.setLongitude((float) arg0.getLon());
            gpbu.update(geoPoint);
            gpbu.addAssociationTo(arg0.getUri(), geoPoint);
            beanSession.save();
            log.debug("Associated " + arg0.getUri() + " with location " + geoPoint.getLatitude() + " / " + geoPoint.getLongitude());
        } catch (Exception e) {
            log.error("Error associating " + arg0.getUri() + " with location " + arg0.getLat() + " / " + arg0.getLon());
            throw new ActionException(e);
        }
        return new EmptyResult();
    }

    @Override
    public Class<AddGeoLocation> getActionType() {
        return AddGeoLocation.class;
    }

    @Override
    public void rollback(AddGeoLocation arg0, EmptyResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
