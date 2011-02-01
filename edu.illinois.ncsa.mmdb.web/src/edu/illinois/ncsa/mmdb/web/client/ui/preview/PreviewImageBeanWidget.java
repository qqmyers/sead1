package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Image;

import edu.illinois.ncsa.mmdb.web.common.RestEndpoints;
import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.PreviewImageBean;

public class PreviewImageBeanWidget extends PreviewBeanWidget<PreviewImageBean> {
    /** maximum width of a preview image */
    private static final long MAX_WIDTH  = 600;

    /** maximum height of a preview image */
    private static final long MAX_HEIGHT = 600;

    public PreviewImageBeanWidget(HandlerManager eventBus) {
        super(eventBus);

        Image widget = new Image();
        widget.getElement().setId(DOM.createUniqueId());
        setWidget(widget);
    }

    @Override
    public PreviewImageBeanWidget newWidget() {
        return new PreviewImageBeanWidget(eventBus);
    }

    public Class<? extends PreviewBean> getPreviewBeanClass() {
        return PreviewImageBean.class;
    }

    @Override
    public String getAnchorText() {
        return "Image";
    }

    @Override
    public PreviewImageBean bestFit(PreviewImageBean obj1, PreviewImageBean obj2, int width, int height) {
        if ((width > -1) && (Math.abs(width - obj1.getWidth()) < Math.abs(width - obj2.getWidth()))) {
            return obj1;
        }
        return obj2;
    }

    @Override
    public void setSection(String section) {
    }

    @Override
    public String getSection() {
        return "Image 1"; //$NON-NLS-1$
    }

    @Override
    protected void showSection() {
        long w = getPreviewBean().getWidth();
        long h = getPreviewBean().getHeight();
        if (w > h) {
            if (w > MAX_WIDTH) {
                h = (long) (h * (double) MAX_WIDTH / w);
                w = MAX_WIDTH;
            }
        } else {
            if (h > MAX_HEIGHT) {
                w = (long) (w * (double) MAX_HEIGHT / h);
                h = MAX_HEIGHT;
            }
        }

        showImage(RestEndpoints.BLOB_URL + getPreviewBean().getUri(), getWidgetID(), Long.toString(w), Long.toString(h));
    }

    public final native void showImage(String url, String id, String w, String h) /*-{
        img = $doc.getElementById(id);
        img.src=url;
        img.width=w;
        img.height=h;
    }-*/;

}
