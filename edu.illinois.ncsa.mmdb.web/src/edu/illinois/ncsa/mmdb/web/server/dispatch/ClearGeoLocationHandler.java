package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.mmdb.web.client.dispatch.ClearGeoLocation;
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
public class ClearGeoLocationHandler implements ActionHandler<ClearGeoLocation, EmptyResult> {
    Log log = LogFactory.getLog(ClearGeoLocationHandler.class);

    @Override
    public EmptyResult execute(ClearGeoLocation arg0, ExecutionContext arg1) throws ActionException {
        BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

        try {
            // geoloc
            Resource uri = Resource.uriRef(arg0.getUri());
            GeoPointBeanUtil gpbu = new GeoPointBeanUtil(beanSession);
            TripleWriter tw = new TripleWriter();
            for (GeoPointBean geoPoint : gpbu.getAssociationsFor(arg0.getUri()) ) {
                tw.remove(uri, gpbu.getAssociationPredicate(), Resource.uriRef(geoPoint.getUri()));
            }
            beanSession.getContext().perform(tw);
        } catch (Exception e) {
            log.error("Could not remove geolocations.", e);
            throw new ActionException(e);
        }
        return new EmptyResult();
    }

    @Override
    public Class<ClearGeoLocation> getActionType() {
        return ClearGeoLocation.class;
    }

    @Override
    public void rollback(ClearGeoLocation arg0, EmptyResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
