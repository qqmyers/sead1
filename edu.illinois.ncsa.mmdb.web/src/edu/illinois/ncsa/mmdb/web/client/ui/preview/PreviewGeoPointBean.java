/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.gis.GeoPointBean;

/**
 * @author Nicholas Tenczar <tenczar2@illinois.edu>
 * 
 */
public class PreviewGeoPointBean extends PreviewBean {
    private ArrayList<GeoPointBean> geoPointBeans = new ArrayList<GeoPointBean>();
    private ArrayList<DatasetBean>  datasetBeans  = new ArrayList<DatasetBean>();

    public PreviewGeoPointBean() {
    }

    public List<DatasetBean> getDatasets() {
        return this.datasetBeans;
    }

    public List<GeoPointBean> getGeoPoints() {
        return this.geoPointBeans;
    }

    public void add(GeoPointBean geoPoint, DatasetBean dataset) {
        this.geoPointBeans.add(geoPoint);
        this.datasetBeans.add(dataset);
    }

    public void addAll(Collection<GeoPointBean> geoPoints, Collection<DatasetBean> datasets) {
        this.geoPointBeans.addAll(geoPoints);
        this.datasetBeans.addAll(datasets);
    }

    private void doNotCallMe() {
        this.geoPointBeans = new ArrayList<GeoPointBean>();
        this.datasetBeans = new ArrayList<DatasetBean>();
    }

    public void put(GeoPointBean geoPoint, DatasetBean dataset) {
        this.geoPointBeans.add(geoPoint);
        this.datasetBeans.add(dataset);
    }
}
