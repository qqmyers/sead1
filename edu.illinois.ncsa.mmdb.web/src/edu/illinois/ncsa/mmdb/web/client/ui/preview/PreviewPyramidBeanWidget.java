package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.common.RestEndpoints;
import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.PreviewPyramidBean;

public class PreviewPyramidBeanWidget extends PreviewBeanWidget<PreviewPyramidBean> {
    public PreviewPyramidBeanWidget(HandlerManager eventBus) {
        super(eventBus);

        Label widget = new Label();
        widget.addStyleName("seadragon"); //$NON-NLS-1$
        widget.getElement().setId(DOM.createUniqueId());
        setWidget(widget);
    }

    public Class<? extends PreviewBean> getPreviewBeanClass() {
        return PreviewPyramidBean.class;
    }

    @Override
    public PreviewPyramidBeanWidget newWidget() {
        return new PreviewPyramidBeanWidget(eventBus);
    }

    @Override
    public String getAnchorText() {
        return "Zoom";
    }

    @Override
    public void setSection(String section) throws IllegalArgumentException {
        throw (new IllegalArgumentException("Could not parse section."));
    }

    @Override
    public String getSection() {
        return "Document"; //$NON-NLS-1$
    }

    @Override
    protected void showSection() {
        showSeadragon(RestEndpoints.PYRAMID_URL + URL.encodeComponent(getPreviewBean().getUri()) + "/xml", getWidgetID()); //$NON-NLS-1$
    }

    @Override
    public void hide() {
        hideSeadragon();
    }

    public final native void showSeadragon(String url, String id) /*-{
        $wnd.Seadragon.Config.debug = true;
        $wnd.Seadragon.Config.imagePath = "img/";
        $wnd.Seadragon.Config.autoHideControls = true;

        // close existing viewer
        if ($wnd.viewer) {
        $wnd.viewer.setFullPage(false);
        $wnd.viewer.setVisible(false);
        $wnd.viewer.close();
        $wnd.viewer = null;            
        }

        // open with new url
        if (url != null) {
        $wnd.viewer = new $wnd.Seadragon.Viewer(id);
        $wnd.viewer.openDzi(url);
        }
    }-*/;

    public final native void hideSeadragon() /*-{
        // hide the current viewer if open
        if ($wnd.viewer) {
        $wnd.viewer.setFullPage(false);
        $wnd.viewer.setVisible(false);
        $wnd.viewer.close();
        $wnd.viewer = null;            
        }
    }-*/;
}
