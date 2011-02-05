package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTML;

import edu.illinois.ncsa.mmdb.web.common.RestEndpoints;
import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.PreviewThreeDimensionalBean;

public class Preview3DJavaBeanWidget extends PreviewBeanWidget<PreviewThreeDimensionalBean> {
    public Preview3DJavaBeanWidget(HandlerManager eventBus) {
        super(eventBus);

        HTML widget = new HTML();
        widget.getElement().setId(DOM.createUniqueId());
        setWidget(widget);
    }

    @Override
    public Preview3DJavaBeanWidget newWidget() {
        return new Preview3DJavaBeanWidget(eventBus);
    }

    public Class<? extends PreviewBean> getPreviewBeanClass() {
        return PreviewThreeDimensionalBean.class;
    }

    @Override
    public String getAnchorText() {
        return "3D (Java)";
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
        String url = GWT.getHostPageBaseURL() + RestEndpoints.EXTENSION_URL + getPreviewBean().getUri();

        ((HTML) getWidget()).setHTML("<div class='Java3DPreview'>" +
                "<APPLET name=jvLite code='jvLite.class' width=480 " +
                "height=360 archive='plugins/jvLite.jar'>" +
                "<PARAM NAME='model' VALUE='" + url + ".obj" + "'>" +
                "<PARAM NAME='border' VALUE='hide'>" +
                "</APPLET></div>");

    }
}
