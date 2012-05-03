package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.common.RestEndpoints;
import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.PreviewVideoBean;

public class PreviewVideoBeanWidget extends PreviewBeanWidget<PreviewVideoBean> {
    public PreviewVideoBeanWidget(HandlerManager eventBus) {
        super(eventBus);

        Label widget = new Label("Get Adobe Flash: http://www.adobe.com/products/flashplayer/");
        widget.getElement().setId(DOM.createUniqueId());
        setWidget(widget);
    }

    @Override
    public PreviewVideoBeanWidget newWidget() {
        return new PreviewVideoBeanWidget(eventBus);
    }

    public Class<? extends PreviewBean> getPreviewBeanClass() {
        return PreviewVideoBean.class;
    }

    @Override
    public String getAnchorText() {
        return "Video";
    }

    @Override
    public PreviewVideoBean bestFit(PreviewVideoBean obj1, PreviewVideoBean obj2, int width, int height) {
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
        return "Document"; //$NON-NLS-1$
    }

    @Override
    protected void showSection() {
        String preview = null;
        long width = getPreviewBean().getWidth();
        long height = getPreviewBean().getHeight();
        if (!getEmbedded()) {
            setWidth((int) width);
            setHeight((int) height);
        } else {
            width = getWidth();
            height = getHeight();
        }
        if (getPreviewBean().getPreviewImage() != null) {
            preview = RestEndpoints.BLOB_URL + getPreviewBean().getPreviewImage().getUri();
        }
        showAudioVideo(RestEndpoints.BLOB_URL + getPreviewBean().getUri(), preview, getWidgetID(), Long.toString(width), Long.toString(height));
    }

    public final native void showAudioVideo(String url, String preview, String id, String w, String h) /*-{
		if (urls != null) {
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
				file : url,
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
