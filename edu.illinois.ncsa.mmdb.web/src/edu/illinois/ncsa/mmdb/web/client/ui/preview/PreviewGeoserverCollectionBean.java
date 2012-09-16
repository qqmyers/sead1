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
public class PreviewGeoserverCollectionBean extends PreviewBean {
    private List<PreviewGeoserverBean> previewGeoserverBeans = new ArrayList<PreviewGeoserverBean>();
    private ArrayList<DatasetBean>     datasetBeans          = new ArrayList<DatasetBean>();

    public PreviewGeoserverCollectionBean() {
    }

    public List<DatasetBean> getDatasets() {
        return this.datasetBeans;
    }

    public List<PreviewGeoserverBean> getPreviewGeoservers() {
        return previewGeoserverBeans;
    }

    public void add(PreviewGeoserverBean previewGeoserverBean, DatasetBean dataset) {
        this.previewGeoserverBeans.add(previewGeoserverBean);
        this.datasetBeans.add(dataset);
    }

    public void addAll(Collection<PreviewGeoserverBean> previewGeoserverBean, Collection<DatasetBean> datasets) {
        this.previewGeoserverBeans.addAll(previewGeoserverBean);
        this.datasetBeans.addAll(datasets);
    }

    private void doNotCallMe() {
        this.previewGeoserverBeans = new ArrayList<PreviewGeoserverBean>();
        this.datasetBeans = new ArrayList<DatasetBean>();
    }

    public void put(PreviewGeoserverBean previewGeoserverBean, DatasetBean dataset) {
        this.previewGeoserverBeans.add(previewGeoserverBean);
        this.datasetBeans.add(dataset);
    }

}
