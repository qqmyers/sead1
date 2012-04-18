package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTML;

import edu.illinois.ncsa.mmdb.web.common.RestEndpoints;
import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.PreviewMultiVideoBean;
import edu.uiuc.ncsa.cet.bean.PreviewVideoBean;

public class PreviewMultiVideoBeanWidget extends PreviewBeanWidget<PreviewMultiVideoBean> {
    private final HTML videowidget;

    public PreviewMultiVideoBeanWidget(HandlerManager eventBus) {
        super(eventBus);

        videowidget = new HTML("Here be video");
        videowidget.getElement().setId(DOM.createUniqueId());
        setWidget(videowidget);
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
        return "Video (HTML5)";
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

    /*
     * <video width="640" height="360" controls>
        <source src="vid_360.mp4"   type="video/mp4" />
        <source src="vid_360.webm"  type="video/webm" />
        <object width="640" height="360" type="application/x-shockwave-flash" data="player.swf">
                <param name="movie" value="player.swf" />
                <param name="flashvars" value="controlbar=over&amp;image=vid_360.png&amp;file=vid_360.m4v" />
                <img src="vid_360.png" width="640" height="360" alt="video" title="No video playback capabilities" />
        </object>
        </video>
     */
    @Override
    protected void showSection() {
        StringBuilder sb = new StringBuilder();

        long width = getPreviewBean().getWidth();
        long height = getPreviewBean().getHeight();
        if (!getEmbedded()) {
            setWidth((int) width);
            setHeight((int) height);
        } else {
            width = getWidth();
            height = getHeight();
        }

        String preview = null;
        if (getPreviewBean().getPreviewImage() != null) {
            preview = RestEndpoints.BLOB_URL + getPreviewBean().getPreviewImage().getUri();
        }

        sb.append("<video width=\"" + width + "\" height=\"" + height + "\" controls poster=\"" + preview + "\">");

        // show videos
        String mp4 = null;
        for (PreviewVideoBean video : getPreviewBean().getVideos() ) {
            if (video.getMimeType().equals("video/flv")) {
                continue;
            } else if (video.getMimeType().equals("video/m4v")) {
                mp4 = RestEndpoints.BLOB_URL + video.getUri();
                sb.append("<source src=\"" + RestEndpoints.BLOB_URL + video.getUri() + ".m4v\" type=\"" + video.getMimeType() + "\" />");
            } else if (video.getMimeType().equals("video/mp4")) {
                mp4 = RestEndpoints.BLOB_URL + video.getUri();
                sb.append("<source src=\"" + RestEndpoints.BLOB_URL + video.getUri() + ".mp4\" type=\"" + video.getMimeType() + "\" />");
            } else {
                sb.append("<source src=\"" + RestEndpoints.BLOB_URL + video.getUri() + "\" type=\"" + video.getMimeType() + "\" />");
            }
        }

        // fall back on flash
        if (mp4 != null) {
            sb.append("<object width=\"" + width + "\" height=\"" + height + "\" type=\"application/x-shockwave-flash\" data=\"player.swf\">");
            sb.append("<param name=\"movie\" value=\"player.swf\" />");
            sb.append("<param name=\"flashvars\" value=\"controlbar=over&amp;image=" + preview + "&amp;file=" + mp4 + "\" />");
            sb.append("<img src=\"" + preview + "\" width=\"" + width + "\" height=\"" + height + "\" alt=\"video\" title=\"No video playback capabilities\" />");
            sb.append("</object>");
        }

        // show error message
        sb.append("Your browser does not support the html video tag.");

        // done
        sb.append("</video>");

        videowidget.setHTML(sb.toString());
    }
}
