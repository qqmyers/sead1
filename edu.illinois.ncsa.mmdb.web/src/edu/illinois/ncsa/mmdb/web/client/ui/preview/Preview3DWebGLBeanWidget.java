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

public class Preview3DWebGLBeanWidget extends PreviewBeanWidget<PreviewThreeDimensionalBean> {
    public Preview3DWebGLBeanWidget(HandlerManager eventBus) {
        super(eventBus);

        HTML widget = new HTML();
        widget.getElement().setId(DOM.createUniqueId());
        setWidget(widget);
    }

    @Override
    public Preview3DWebGLBeanWidget newWidget() {
        return new Preview3DWebGLBeanWidget(eventBus);
    }

    public Class<? extends PreviewBean> getPreviewBeanClass() {
        return PreviewThreeDimensionalBean.class;
    }

    @Override
    public String getAnchorText() {
        return "3D (WebGL)";
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
    public void hide() {
        hideWebGL();
    }

    @Override
    protected void showSection() {
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

                        //Read file successfully; call Javascript to initialize html5 canvas
                        readWebGL(response.getText());

                    } else {
                        ((HTML) getWidget()).setText("Error\n" + response.getStatusText());
                    }
                }
            });
        } catch (RequestException e) {
            ((HTML) getWidget()).setText("Error\n" + e.getMessage());
        }

        ((HTML) getWidget()).setHTML("<STYLE type='text/css'> canvas {border:solid 1px #000;} body{overflow:hidden;}</STYLE>" +
                "<CANVAS id='c' width='480' height='360'><P>If you are seeing this, " +
                "your browser does not support <a href='http://www.google.com/chrome/'>" +
                "HTML5</a></P></CANVAS>" + "<p id='info'></p>");

    }

    public final native void readWebGL(String fileData) /*-{
		// initialize WebGL application
		$wnd.init_webGL(fileData);
    }-*/;

    public final native void hideWebGL() /*-{
		// hide the current WebGL viewer if open
		$wnd.hide_webGL();
    }-*/;
}
