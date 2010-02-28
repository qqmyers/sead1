/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.Collection;
import java.util.HashSet;

import net.customware.gwt.dispatch.shared.Result;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PreviewBean;

/**
 * Return a dataset and associated previews if available.
 * 
 * @author Luigi Marini
 * 
 */
public class GetDatasetResult implements Result
{

    private static final long       serialVersionUID = -86488013616325220L;

    private DatasetBean             dataset;

    private Collection<PreviewBean> previews;

    public GetDatasetResult()
    {
    }

    public GetDatasetResult( DatasetBean datasetBean, Collection<PreviewBean> previews )
    {
        setDataset( datasetBean );
        setPreviews( previews );
    }

    public void setDataset( DatasetBean dataset )
    {
        this.dataset = dataset;
    }

    public DatasetBean getDataset()
    {
        return dataset;
    }

    public void setPreviews( Collection<PreviewBean> previews )
    {
        this.previews = previews;
    }

    public Collection<PreviewBean> getPreviews()
    {
        if ( previews == null ) {
            return new HashSet<PreviewBean>();
        }
        return previews;
    }
}
