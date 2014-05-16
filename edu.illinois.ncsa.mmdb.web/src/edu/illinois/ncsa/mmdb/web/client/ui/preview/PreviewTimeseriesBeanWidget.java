/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;

import edu.illinois.ncsa.mmdb.web.common.RestEndpoints;
import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.PreviewTabularDataBean;

/**
 * Time series visualization of csv files.
 * 
 * https://opensource.ncsa.illinois.edu/stash/projects/MMDB/repos/timeseriesvis
 * 
 * @author Luigi Marini
 * 
 */
public class PreviewTimeseriesBeanWidget extends PreviewBeanWidget<PreviewTabularDataBean> {
    private final Panel widget;

    public PreviewTimeseriesBeanWidget(HandlerManager eventBus) {
        super(eventBus);
        widget = new SimplePanel();
        widget.getElement().setId(DOM.createUniqueId());
        setWidget(widget);
    }

    @Override
    public PreviewBeanWidget<PreviewTabularDataBean> newWidget() {
        return new PreviewTimeseriesBeanWidget(eventBus);
    }

    @Override
    public Class<? extends PreviewBean> getPreviewBeanClass() {
        return PreviewTabularDataBean.class;
    }

    @Override
    protected void showSection() {
        String url = RestEndpoints.BLOB_URL + getDataset().getUri();
        widget.add(new HTML("<div id='graphControls'></div><div id='d3TimeseriesVis'><svg></svg></div>"));
        initNativeVis(url);
    }

    @Override
    public void setSection(String section) throws IllegalArgumentException {
        throw (new IllegalArgumentException("Could not parse section."));
    }

    @Override
    public String getSection() {
        return "Document";
    }

    @Override
    public String getAnchorText() {
        return "Time series";
    }

    public final native void initNativeVis(String url) /*-{
		//		$wnd.LazyLoad.js('js/timeseriesvis/timeseriesvis.js', function() {
		$wnd.setupControls();
		$wnd.loadDataByUrl(url);
		//		});
    }-*/;

}
