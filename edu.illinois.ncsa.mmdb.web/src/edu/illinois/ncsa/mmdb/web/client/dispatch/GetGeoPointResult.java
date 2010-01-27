/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.Collection;
import java.util.HashSet;

import net.customware.gwt.dispatch.shared.Result;
import edu.uiuc.ncsa.cet.bean.gis.GeoPointBean;

/**
 * Return a dataset and associated previews if available.
 * 
 * @author Luigi Marini
 * 
 */
public class GetGeoPointResult implements Result
{

    private static final long            serialVersionUID = -86488013616325220L;

    private Collection<GeoPointBean>     geoloc;

    public GetGeoPointResult()
    {
    }

    public GetGeoPointResult( Collection<GeoPointBean> geoloc )
    {
        setGeoPoints( geoloc );
    }

    public void setGeoPoints( Collection<GeoPointBean> geoloc )
    {
        this.geoloc = geoloc;
    }

    public Collection<GeoPointBean> getGeoPoints()
    {
        if ( geoloc == null ) {
            return new HashSet<GeoPointBean>();
        }
        return geoloc;
    }

}
