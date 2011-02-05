package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTML;

import edu.illinois.ncsa.mmdb.web.common.RestEndpoints;
import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.PreviewThreeDimensionalBean;

public class Preview3DHTML5BeanWidget extends PreviewBeanWidget<PreviewThreeDimensionalBean> {
    public Preview3DHTML5BeanWidget(HandlerManager eventBus) {
        super(eventBus);

        HTML widget = new HTML();
        widget.getElement().setId(DOM.createUniqueId());
        setWidget(widget);
    }

    @Override
    public Preview3DHTML5BeanWidget newWidget() {
        return new Preview3DHTML5BeanWidget(eventBus);
    }

    public Class<? extends PreviewBean> getPreviewBeanClass() {
        return PreviewThreeDimensionalBean.class;
    }

    @Override
    public String getAnchorText() {
        return "3D (HTML5)";
    }

    @Override
    public void setSection(String section) throws IllegalArgumentException {
        throw (new IllegalArgumentException("Could not parse section."));
    }

    @Override
    public void hide() {
        hideHTML5();
    }

    public final native void hideHTML5() /*-{
        // hide the current WebGL viewer if open
        $wnd.hideThingView();
    }-*/;

    @Override
    public String getSection() {
        return "Document"; //$NON-NLS-1$
    }

    public final native void readOBJ(String fileData) /*-{
        // initialize HTML5 application
        $wnd.initialize(fileData);
    }-*/;

    @Override
    protected void showSection() {
        ((HTML) getWidget()).setHTML("<center><div id='viewer' style='border:solid 1px #A8A8A8;width:480px;height:360px'></div></center>");

        String url = GWT.getHostPageBaseURL() + RestEndpoints.BLOB_URL + getPreviewBean().getUri();

        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(url));
        try {
            @SuppressWarnings("unused")
            Request request = builder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    ((HTML) getWidget()).setText("Error\n" + exception.getMessage());
                }

                public void onResponseReceived(Request request, Response response) {
                    if (200 == response.getStatusCode()) {
                        initThingView(response.getText());

                    } else {
                        ((HTML) getWidget()).setText("Error\n" + response.getStatusText());
                    }
                }
            });
        } catch (RequestException e) {
            ((HTML) getWidget()).setText("Error\n" + e.getMessage());
        }

        //OLD HTML5 Widget
        //Read file successfully; call Javascript to initialize html5 canvas
        //readOBJ(response.getText());
        //<script src='js/html5_3Dplugin.js' ></script>
        //String url = GWT.getHostPageBaseURL() + RestEndpoints.BLOB_URL + getPreviewBean().getUri();
        /*((HTML) getWidget()).setHTML("<STYLE type='text/css'> canvas {border:solid 1px #000;}</STYLE>" +
                "<CANVAS id='canvas' width='480' height='360'><P>If you are seeing this, " +
                "your browser does not support <a href='http://www.google.com/chrome/'>" +
                "HTML5</a></P></CANVAS>");
                */
    }

    public final native void initThingView(String fileData) /*-{
        // hide the current WebGL viewer if open
        $wnd.initThingView(fileData);
    }-*/;

}
