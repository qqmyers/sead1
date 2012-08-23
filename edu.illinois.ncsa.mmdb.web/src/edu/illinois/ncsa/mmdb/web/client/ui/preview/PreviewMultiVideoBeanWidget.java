package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTML;

import edu.illinois.ncsa.mmdb.web.common.RestEndpoints;
import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.PreviewMultiVideoBean;
import edu.uiuc.ncsa.cet.bean.PreviewVideoBean;

public class PreviewMultiVideoBeanWidget extends PreviewBeanWidget<PreviewMultiVideoBean> {
    public PreviewMultiVideoBeanWidget(HandlerManager eventBus) {
        super(eventBus);

        HTML widget = new HTML("HTML5 Video");
        widget.getElement().setId(DOM.createUniqueId());
        setWidget(widget);
    }

    @Override
    public PreviewMultiVideoBeanWidget newWidget() {
        return new PreviewMultiVideoBeanWidget(eventBus);
    }

    public Class<? extends PreviewBean> getPreviewBeanClass() {
        return PreviewMultiVideoBean.class;
    }

    @Override
    public String getAnchorText() {
        return "Video";
    }

    @Override
    public PreviewMultiVideoBean bestFit(PreviewMultiVideoBean obj1, PreviewMultiVideoBean obj2, int width, int height) {
        if ((width > -1) && (Math.abs(width - obj1.getWidth()) < Math.abs(width - obj2.getWidth()))) {
            return obj1;
        }
        return obj2;
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
        String html = "<video controls";

        // compute widht and height of video widget
        long width = getPreviewBean().getWidth();
        long height = getPreviewBean().getHeight();
        if (!getEmbedded()) {
            setWidth((int) width);
            setHeight((int) height);
        } else {
            width = getWidth();
            height = getHeight();
        }
        html += " width=\"" + width + "px\"";
        html += " height=\"" + height + "px\"";

        // poster image
        if (getPreviewBean().getPreviewImage() != null) {
            String poster = RestEndpoints.BLOB_URL + getPreviewBean().getPreviewImage().getUri();
            html += " poster=\"" + poster + "\"";
        }

        // videos
        html += ">";
        for (PreviewVideoBean video : getPreviewBean().getVideos() ) {
            String ext = "." + video.getMimeType().substring(6);
            String url = video.getUri();
            String magic = "tag:cet.ncsa.uiuc.edu,2008:/bean/PreviewVideo/";
            if (video.getUri().startsWith(magic)) {
                url = "api/video/" + video.getUri().substring(magic.length()) + ext;
            } else {
                url = RestEndpoints.BLOB_URL + video.getUri() + ext;
            }
            html += "<source src=\"" + url + "\" type=\"" + video.getMimeType() + "\">";
        }
        html += "</video>";

        // show the video
        GWT.log(html);
        ((HTML) getWidget()).setHTML(html);
    }
}