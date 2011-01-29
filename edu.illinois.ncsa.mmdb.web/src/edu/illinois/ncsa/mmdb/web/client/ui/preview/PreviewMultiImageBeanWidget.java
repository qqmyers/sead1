package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import com.google.gwt.user.client.ui.Label;

import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.PreviewMultiImageBean;

public class PreviewMultiImageBeanWidget extends PreviewBeanWidget<PreviewMultiImageBean> {
    /** current image currently shown */
    private final int current = 0;

    public PreviewMultiImageBeanWidget() {
        super(new Label("Nothing to see"));
    }

    @Override
    public PreviewBeanWidget<PreviewMultiImageBean> newWidget() {
        return new PreviewMultiImageBeanWidget();
    }

    @Override
    public Class<? extends PreviewBean> getPreviewBeanClass() {
        return PreviewMultiImageBean.class;
    }

    @Override
    public String getAnchorText() {
        return "Multi Image";
    }

    @Override
    public String getCurrent() {
        return Integer.toString(current + 1);
    }
}
