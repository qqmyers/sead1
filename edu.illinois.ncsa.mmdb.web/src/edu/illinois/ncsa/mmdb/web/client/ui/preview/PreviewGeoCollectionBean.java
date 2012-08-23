/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.PreviewGeoserverBean;

/**
 * @author Jong Lee <jonglee1@illinois.edu>
 * 
 */
public class PreviewGeoCollectionBean extends PreviewBean {
    private List<PreviewGeoserverBean> previewGeoserverBeans = new ArrayList<PreviewGeoserverBean>();
    private List<PreviewGeoPointBean>  previewGeoPointBeans  = new ArrayList<PreviewGeoPointBean>();
    private List<DatasetBean>          geoserverDatasetBeans = new ArrayList<DatasetBean>();
    private List<DatasetBean>          geoPointDatasetBeans  = new ArrayList<DatasetBean>();

    public PreviewGeoCollectionBean() {
    }

    public List<DatasetBean> getGeoserverDatasets() {
        return this.geoserverDatasetBeans;
    }

    public List<DatasetBean> getGeopointDatasets() {
        return this.geoPointDatasetBeans;
    }

    public List<PreviewGeoserverBean> getPreviewGeoservers() {
        return previewGeoserverBeans;
    }

    public List<PreviewGeoPointBean> getPreviewGeoPoints() {
        return previewGeoPointBeans;
    }

    public void add(PreviewGeoserverBean previewGeoserverBean, DatasetBean dataset) {
        this.previewGeoserverBeans.add(previewGeoserverBean);
        this.geoserverDatasetBeans.add(dataset);
    }

    public void add(PreviewGeoPointBean previewGeoPointBean, DatasetBean dataset) {
        this.previewGeoPointBeans.add(previewGeoPointBean);
        this.geoPointDatasetBeans.add(dataset);
    }

    public void addGeoserverAll(Collection<PreviewGeoserverBean> previewGeoserverBean, Collection<DatasetBean> datasets) {
        this.previewGeoserverBeans.addAll(previewGeoserverBean);
        this.geoserverDatasetBeans.addAll(datasets);
    }

    public void addGeoPointAll(Collection<PreviewGeoPointBean> previewGeoPointBean, Collection<DatasetBean> datasets) {
        this.previewGeoPointBeans.addAll(previewGeoPointBean);
        this.geoPointDatasetBeans.addAll(datasets);
    }

    private void doNotCallMe() {
        this.previewGeoserverBeans = new ArrayList<PreviewGeoserverBean>();
        this.previewGeoPointBeans = new ArrayList<PreviewGeoPointBean>();
        this.geoserverDatasetBeans = new ArrayList<DatasetBean>();
        this.geoPointDatasetBeans = new ArrayList<DatasetBean>();
    }
}
