package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.common.RestEndpoints;
import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.preview.PreviewAudioBean;

public class PreviewAudioBeanWidget extends PreviewBeanWidget<PreviewAudioBean> {
    public PreviewAudioBeanWidget() {
        Label widget = new Label();
        widget.getElement().setId(DOM.createUniqueId());
        setWidget(widget);
    }

    @Override
    public PreviewAudioBeanWidget newWidget() {
        return new PreviewAudioBeanWidget();
    }

    public Class<? extends PreviewBean> getPreviewBeanClass() {
        return PreviewAudioBean.class;
    }

    @Override
    public String getAnchorText() {
        return "Audio";
    }

    @Override
    public String getCurrent() {
        return "1"; //$NON-NLS-1$
    }

    @Override
    public void show() {
        String preview = null;
        long width = 320;
        long height = 240;
        if (getPreviewBean().getPreviewImage() != null) {
            preview = RestEndpoints.BLOB_URL + getPreviewBean().getPreviewImage().getUri();
            width = getPreviewBean().getPreviewImage().getWidth();
            height = getPreviewBean().getPreviewImage().getHeight();
        }

        showAudioVideo(RestEndpoints.BLOB_URL + getPreviewBean().getUri(), preview, getWidgetID(), Long.toString(width), Long.toString(height));
    }

    public final native void showAudioVideo(String url, String preview, String id, String w, String h) /*-{
        if (url != null) {
        $wnd.player = new $wnd.SWFObject('player.swf', 'player', w, h, '9');
        $wnd.player.addParam('allowfullscreen','true');
        $wnd.player.addParam('allowscriptaccess','always');
        $wnd.player.addParam('wmode','opaque');
        $wnd.player.addVariable('file',url);
        $wnd.player.addVariable('autostart','false');
        if (preview != null) {
        $wnd.player.addVariable('image',preview);
        }            
        //            $wnd.player.addVariable('author','Joe');
        //            $wnd.player.addVariable('description','Bob');
        //            $wnd.player.addVariable('title','title');
        //            $wnd.player.addVariable('debug','console');
        $wnd.player.addVariable('provider','sound');
        $wnd.player.write(id);
        }
    }-*/;
}