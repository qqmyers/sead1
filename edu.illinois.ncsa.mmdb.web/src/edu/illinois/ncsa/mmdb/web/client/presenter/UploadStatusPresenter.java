package edu.illinois.ncsa.mmdb.web.client.presenter;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasValue;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.event.AllDatasetsUnselectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetSelectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetSelectedHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedHandler;
import edu.illinois.ncsa.mmdb.web.client.mvp.BasePresenter;
import edu.illinois.ncsa.mmdb.web.client.ui.DatasetSelectionCheckboxHandler;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * This presenter manages the relationship between DND upload applet callbacks
 * and the display of upload status for a batch of uploads
 * 
 * @author futrelle
 * 
 */
public class UploadStatusPresenter extends BasePresenter<UploadStatusPresenter.Display> {
    MyDispatchAsync dispatch;

    int             nDropped  = 0;
    int             nUploaded = 0;

    public UploadStatusPresenter(MyDispatchAsync dispatch, HandlerManager eventBus, Display display) {
        super(display, eventBus);
        this.dispatch = dispatch;
    }

    public interface Display {
        void clear();

        void onDropped(int ix, String filename, String sizeString);

        void onProgress(int ix, int percent);

        void onComplete(int ix, String uri, int total);

        void onPostComplete(int ix, DatasetBean dataset);

        HasValue<Boolean> getSelectionControl(int ix);
    }

    /**
     * Applet calls back for all files dropped, then for each file in order,
     * <ol>
     * <li>calls back with progress, repeatedly</li>
     * <li>calls back on completion with dataset uri</li>
     * </ol>
     * 
     * @param file
     */
    public void onDropped(String filename, String sizeString) {
        if (nDropped == 0 || nUploaded > 0) {
            nUploaded = 0;
            nDropped = 0;
            display.clear();
            // clear the selection
            eventBus.fireEvent(new AllDatasetsUnselectedEvent());
        }
        display.onDropped(nDropped++, filename, sizeString);
    }

    public void onProgress(int percent) {
        if (nUploaded < nDropped) { // ignore spurious updates to last completed upload
            display.onProgress(nUploaded, percent);
        }
    }

    public void onComplete(final String uri) {
        final int ix = nUploaded;
        display.onComplete(ix, uri, nDropped);
        display.getSelectionControl(ix).addValueChangeHandler(new DatasetSelectionCheckboxHandler(uri, eventBus));
        display.getSelectionControl(ix).setValue(true, true); // select, and fire the selection event
        // checkbox should also respond to selection and deselection events coming from elsewhere
        addHandler(DatasetSelectedEvent.TYPE, new DatasetSelectedHandler() {
            public void onDatasetSelected(DatasetSelectedEvent event) {
                if (event.getUri().equals(uri)) {
                    display.getSelectionControl(ix).setValue(true, false); // select, but don't fire another selection event
                }
            }
        });
        addHandler(DatasetUnselectedEvent.TYPE, new DatasetUnselectedHandler() {
            public void onDatasetUnselected(DatasetUnselectedEvent datasetUnselectedEvent) {
                if (datasetUnselectedEvent.getUri().equals(uri)) {
                    display.getSelectionControl(ix).setValue(false, false); // deselect, but don't fire another deselection event
                }
            }
        });
        fetchDataset(ix, uri);
        nUploaded++;
    }

    void fetchDataset(final int ix, final String uri) {
        DeferredCommand.addCommand(new Command() {
            public void execute() {
                dispatch.execute(new GetDataset(uri), new AsyncCallback<GetDatasetResult>() {
                    public void onFailure(Throwable caught) {
                        Window.alert("GetDataset failed for " + uri); // FIXME debug
                    }

                    public void onSuccess(GetDatasetResult result) {
                        display.onPostComplete(ix, result.getDataset());
                    }
                });
            }
        });
    }

    @Override
    public void unbind() {
    }
}
