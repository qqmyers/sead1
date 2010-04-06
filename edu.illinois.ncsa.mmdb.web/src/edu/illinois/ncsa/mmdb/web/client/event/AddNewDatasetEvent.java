/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.event;

import java.util.Collection;

import com.google.gwt.event.shared.GwtEvent;

import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PreviewImageBean;

/**
 * Triggered when a new dataset is added to the interface.
 * 
 * @author Luigi Marini
 * 
 */
public class AddNewDatasetEvent extends GwtEvent<AddNewDatasetHandler> {

    public static final GwtEvent.Type<AddNewDatasetHandler> TYPE     = new GwtEvent.Type<AddNewDatasetHandler>();

    private DatasetBean                                     dataset  = new DatasetBean();

    private Collection<PreviewImageBean>                    previews;

    private int                                             position = -1;

    @Override
    protected void dispatch(AddNewDatasetHandler handler) {
        handler.onAddNewDataset(this);
    }

    @Override
    public GwtEvent.Type<AddNewDatasetHandler> getAssociatedType() {
        return TYPE;
    }

    public void setDataset(DatasetBean dataset) {
        this.dataset = dataset;
    }

    public DatasetBean getDataset() {
        return dataset;
    }

    public void setPreviews(Collection<PreviewImageBean> previews) {
        this.previews = previews;
    }

    public Collection<PreviewImageBean> getPreviews() {
        return previews;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

}
