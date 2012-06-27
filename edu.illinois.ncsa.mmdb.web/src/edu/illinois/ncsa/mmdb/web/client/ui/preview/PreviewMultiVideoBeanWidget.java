package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.common.RestEndpoints;
import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.PreviewMultiVideoBean;
import edu.uiuc.ncsa.cet.bean.PreviewVideoBean;

public class PreviewMultiVideoBeanWidget extends PreviewBeanWidget<PreviewMultiVideoBean> {
    public PreviewMultiVideoBeanWidget(HandlerManager eventBus) {
        super(eventBus);

        Label widget = new Label("HTML5 Video");
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

    @Override
    protected void showSection() {
        long width = getPreviewBean().getWidth();
        long height = getPreviewBean().getHeight();
        if (!getEmbedded()) {
            setWidth((int) width);
            setHeight((int) height);
        } else {
            width = getWidth();
            height = getHeight();
        }

        // poster image
        String preview = null;
        if (getPreviewBean().getPreviewImage() != null) {
            preview = RestEndpoints.BLOB_URL + getPreviewBean().getPreviewImage().getUri();
        }

        // videos
        JsArrayString urls = (JsArrayString) JsArrayString.createArray();
        for (PreviewVideoBean video : getPreviewBean().getVideos() ) {
            String ext = "." + video.getMimeType().substring(6);
            String url = video.getUri();
            String magic = "tag:cet.ncsa.uiuc.edu,2008:/bean/PreviewVideo/";
            if (video.getUri().startsWith(magic)) {
                url = "api/video/" + video.getUri().substring(magic.length()) + ext;
            } else {
                url = RestEndpoints.BLOB_URL + video.getUri() + ext;
            }
            urls.push(url);
        }

        // call javascript
        showVideo(urls, preview, getWidgetID(), Long.toString(width), Long.toString(height));
    }

    public final native void showVideo(JsArrayString urls, String preview, String id, String w, String h) /*-{
		if (urls != null) {
			console.log(urls);
			// create the levels
			var levels = $wnd.createAnArray();
			var len = urls.length;
			for ( var i = 0; i < len; i++) {
				levels.push({
					file : urls[i]
				});
				console.log(urls[i]);
			}

			// force html5 first
			var modes = $wnd.createAnArray();
			modes.push({
				type : "html5"
			});
			modes.push({
				type : "flash",
				src : "player.swf"
			});
			modes.push({
				type : "download"
			});

			// create the player
			$wnd.jwplayer(id).setup({
				height : h,
				width : w,
				image : preview,
				modes : modes,
				levels : levels,
				provider : 'video',
			//                controlbar: 'over',
			//                skin: 'skins/glow/glow.zip',
			//                provider: "http",
			//                "http.startparam":"starttime"
			});
		} else {
			$wnd.jwplayer(id).remove();
		}
    }-*/;
}
