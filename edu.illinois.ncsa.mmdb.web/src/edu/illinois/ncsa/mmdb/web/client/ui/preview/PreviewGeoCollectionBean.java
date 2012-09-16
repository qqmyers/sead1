/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import java.util.Collection;
import java.util.List;

import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.PreviewGeoserverBean;
import edu.uiuc.ncsa.cet.bean.gis.GeoPointBean;

/**
 * @author Jong Lee <jonglee1@illinois.edu>
 * 
 */
public class PreviewGeoCollectionBean extends PreviewBean {
    private PreviewGeoserverCollectionBean previewGeoserverCollectionBean;
    private PreviewGeoPointBean            previewGeoPointBean;

    public PreviewGeoCollectionBean() {
    }

    public List<DatasetBean> getGeoserverDatasets() {
        return this.getPreviewGeoserverCollectionBean().getDatasets();
    }

    public List<DatasetBean> getGeopointDatasets() {
        return this.getPreviewGeoPointBean().getDatasets();
    }

    public List<PreviewGeoserverBean> getPreviewGeoserverBeans() {
        return getPreviewGeoserverCollectionBean().getPreviewGeoservers();
    }

    public List<GeoPointBean> getPreviewGeoPoint() {
        return getPreviewGeoPointBean().getGeoPoints();
    }

    public void add(PreviewGeoserverBean previewGeoserverBean, DatasetBean dataset) {
        this.getPreviewGeoserverCollectionBean().add(previewGeoserverBean, dataset);
    }

    public void add(GeoPointBean geoPointBean, DatasetBean dataset) {
        this.getPreviewGeoPointBean().add(geoPointBean, dataset);
    }

    public void addGeoserverAll(Collection<PreviewGeoserverBean> previewGeoserverBeans, Collection<DatasetBean> datasets) {
        this.getPreviewGeoserverCollectionBean().addAll(previewGeoserverBeans, datasets);
    }

    public void addGeoPointAll(Collection<GeoPointBean> geoPointBeans, Collection<DatasetBean> datasets) {
        this.getPreviewGeoPointBean().addAll(geoPointBeans, datasets);
    }

    /**
     * @return the previewGeoserverCollectionBean
     */
    public PreviewGeoserverCollectionBean getPreviewGeoserverCollectionBean() {
        return previewGeoserverCollectionBean;
    }

    /**
     * @param previewGeoserverCollectionBean the previewGeoserverCollectionBean to set
     */
    public void setPreviewGeoserverCollectionBean(PreviewGeoserverCollectionBean previewGeoserverCollectionBean) {
        this.previewGeoserverCollectionBean = previewGeoserverCollectionBean;
    }

    /**
     * @return the previewGeoPointBean
     */
    public PreviewGeoPointBean getPreviewGeoPointBean() {
        return previewGeoPointBean;
    }

    /**
     * @param previewGeoPointBean the previewGeoPointBean to set
     */
    public void setPreviewGeoPointBean(PreviewGeoPointBean previewGeoPointBean) {
        this.previewGeoPointBean = previewGeoPointBean;
    }

}
