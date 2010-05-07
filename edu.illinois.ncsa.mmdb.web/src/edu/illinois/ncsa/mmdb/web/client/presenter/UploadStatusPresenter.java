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
import edu.illinois.ncsa.mmdb.web.client.mvp.Presenter;
import edu.illinois.ncsa.mmdb.web.client.mvp.View;
import edu.illinois.ncsa.mmdb.web.client.ui.DatasetSelectionCheckboxHandler;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * This presenter manages the relationship between DND upload applet callbacks
 * and the display of upload status for a batch of uploads
 * 
 * @author futrelle
 * 
 */
public class UploadStatusPresenter implements Presenter {
    MyDispatchAsync dispatch;
    HandlerManager  eventBus;
    Display         display;

    int             nDropped  = 0;
    int             nUploaded = 0;

    public UploadStatusPresenter(MyDispatchAsync dispatch, HandlerManager eventBus, Display display) {
        this.dispatch = dispatch;
        this.eventBus = eventBus;
        this.display = display;
    }

    public interface Display {
        void clear();

        void onDropped(int ix, String filename, String sizeString);

        void onProgress(int ix, int percent);

        void onComplete(int ix, String uri);

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
        nUploaded = 0;
        if (nUploaded > 0) {
            display.clear();
        }
        display.onDropped(nDropped++, filename, sizeString);
    }

    public void onProgress(int percent) {
        if (nUploaded < nDropped) { // ignore spurious updates to last completed upload
            display.onProgress(nUploaded, percent);
        }
    }

    public void onComplete(String uri) {
        display.onComplete(nUploaded, uri);
        display.getSelectionControl(nUploaded).addValueChangeHandler(new DatasetSelectionCheckboxHandler(uri, eventBus));
        fetchDataset(nUploaded, uri);
        nUploaded++;
    }

    void fetchDataset(final int ix, final String uri) {
        DeferredCommand.addCommand(new Command() {
            public void execute() {
                dispatch.execute(new GetDataset(uri), new AsyncCallback<GetDatasetResult>() {
                    public void onFailure(Throwable caught) {
                        Window.alert("fileUploaded dispatch failed: " + caught.getMessage()); // FIXME
                    }

                    public void onSuccess(GetDatasetResult result) {
                        display.onPostComplete(ix, result.getDataset());
                    }
                });
            }
        });
    }

    @Override
    public void bind() {
        // TODO Auto-generated method stub

    }

    @Override
    public View getView() {
        // TODO Auto-generated method stub
        return null;
    }

}
