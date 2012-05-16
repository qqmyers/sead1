package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.common.RestEndpoints;
import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.preview.PreviewAudioBean;

public class PreviewAudioBeanWidget extends PreviewBeanWidget<PreviewAudioBean> {
    public PreviewAudioBeanWidget(HandlerManager eventBus) {
        super(eventBus);

        Label widget = new Label("Audio Player");
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
        return "Document"; //$NON-NLS-1$
    }

    @Override
    protected void showSection() {
        String preview = null;
        long width = 320;
        long height = 240;

        if (getPreviewBean().getPreviewImage() != null) {
            preview = RestEndpoints.BLOB_URL + getPreviewBean().getPreviewImage().getUri();
            width = getPreviewBean().getPreviewImage().getWidth();
            height = getPreviewBean().getPreviewImage().getHeight();
        }
        if (!getEmbedded()) {
            setWidth((int) width);
            setHeight((int) height);
        } else {
            width = getWidth();
            height = getHeight();
        }

        showAudioVideo(RestEndpoints.BLOB_URL + getPreviewBean().getUri(), preview, getWidgetID(), Long.toString(width), Long.toString(height));
    }

    public final native void showAudioVideo(String url, String preview, String id, String w, String h) /*-{
		if (url != null) {
			// force html5 first
			var modes = $wnd.createAnArray();
			modes.push({
				type : "html5"
			});
			//			modes.push({
			//				type : "flash",
			//				src : "player.swf"
			//			});
			modes.push({
				type : "download"
			});
			console.log(modes);

			// create the player
			$wnd.jwplayer(id).setup({
				height : h,
				width : w,
				image : preview,
				modes : modes,
				file : url,
				provider : 'sound',
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
