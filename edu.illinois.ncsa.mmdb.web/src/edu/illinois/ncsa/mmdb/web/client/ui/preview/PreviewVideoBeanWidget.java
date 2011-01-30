package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.common.RestEndpoints;
import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.PreviewVideoBean;

public class PreviewVideoBeanWidget extends PreviewBeanWidget<PreviewVideoBean> {
    public PreviewVideoBeanWidget() {
        Label widget = new Label();
        widget.getElement().setId(DOM.createUniqueId());
        setWidget(widget);
    }

    @Override
    public PreviewVideoBeanWidget newWidget() {
        return new PreviewVideoBeanWidget();
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
    public String getCurrent() {
        return "1"; //$NON-NLS-1$
    }

    @Override
    public void show() {
        String preview = null;
        long width = getPreviewBean().getWidth();
        long height = getPreviewBean().getHeight();
        if (getPreviewBean().getPreviewImage() != null) {
            preview = RestEndpoints.BLOB_URL + getPreviewBean().getPreviewImage().getUri();
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
        $wnd.player.addVariable('provider','video');
        $wnd.player.write(id);
        }
    }-*/;
}
