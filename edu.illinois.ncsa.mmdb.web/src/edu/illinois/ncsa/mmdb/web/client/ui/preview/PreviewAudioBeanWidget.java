package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTML;

import edu.illinois.ncsa.mmdb.web.common.RestEndpoints;
import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.preview.PreviewAudioBean;

public class PreviewAudioBeanWidget extends PreviewBeanWidget<PreviewAudioBean> {
    public PreviewAudioBeanWidget(HandlerManager eventBus) {
        super(eventBus);

        HTML widget = new HTML("Audio Player");
        widget.getElement().setId(DOM.createUniqueId());
        setWidget(widget);
    }

    @Override
    public PreviewAudioBeanWidget newWidget() {
        return new PreviewAudioBeanWidget(eventBus);
    }

    public Class<? extends PreviewBean> getPreviewBeanClass() {
        return PreviewAudioBean.class;
    }

    @Override
    public String getAnchorText() {
        return "Audio";
    }

    @Override
    public void setSection(String section) throws IllegalArgumentException {
        throw (new IllegalArgumentException("Could not parse section."));
    }

    @Override
    public String getSection() {
        return "Time"; //$NON-NLS-1$
    }

    @Override
    protected void showSection() {
        String html = "";

        // width and height
        long width = getEmbedded() ? getWidth() : 320;
        long height = getEmbedded() ? getHeight() : 240;

        // poster image
        if (getPreviewBean().getPreviewImage() != null) {
            if (!getEmbedded()) {
                width = getPreviewBean().getPreviewImage().getWidth();
                height = getPreviewBean().getPreviewImage().getHeight();
            }

            String preview = RestEndpoints.BLOB_URL + getPreviewBean().getPreviewImage().getUri();
            html += "<img src=\"" + preview + "\" width=\"" + width + "px\" height=\"" + height + "\" /><br />";
        }

        // set width and height
        setWidth((int) width);
        setHeight((int) height);

        // audio
        html += "<audio controls>";
        PreviewAudioBean audio = getPreviewBean();
        String url = RestEndpoints.BLOB_URL + audio.getUri();
        html += "<source src=\"" + url + "\" type=\"" + audio.getMimeType() + "\">";
        html += "</audio>";

        // show the video
        GWT.log(html);
        ((HTML) getWidget()).setHTML(html);
    }
}