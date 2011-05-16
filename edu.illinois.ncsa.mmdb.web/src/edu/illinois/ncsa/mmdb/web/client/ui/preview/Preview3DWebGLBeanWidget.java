package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.dispatch.Create3DImage;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.common.RestEndpoints;
import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.PreviewThreeDimensionalBean;

public class Preview3DWebGLBeanWidget extends PreviewBeanWidget<PreviewThreeDimensionalBean> {
    private final HTML widget;
    DispatchAsync      dispatch;
    Label              convert;

    public Preview3DWebGLBeanWidget(HandlerManager eventBus) {
        super(eventBus);
        VerticalPanel vp = new VerticalPanel();
        vp.addStyleName("centered"); //$NON-NLS-1$

        widget = new HTML();
        widget.getElement().setId(DOM.createUniqueId());
        vp.add(widget);

        final Anchor setImage = new Anchor("Create Thumbnail");
        setImage.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                setImage();
            }
        });
        vp.add(setImage);

        convert = new Label("(Creating...)");
        convert.addStyleName("hidden");
        vp.add(convert);

        vp.setCellHorizontalAlignment(setImage, HasHorizontalAlignment.ALIGN_CENTER);
        vp.setCellHorizontalAlignment(convert, HasHorizontalAlignment.ALIGN_CENTER);

        setWidget(vp);
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

    private final native String getCanvasData() /*-{
		// initialize HTML5 application
		return $wnd.saveImgWebGL();
    }-*/;

    private void setImage() {
        final String fileData = getCanvasData();
        convert.removeStyleName("hidden");
        dispatch = getDispatch();
        dispatch.execute(new Create3DImage(fileData, MMDB.getUsername(), getDataset()), new AsyncCallback<EmptyResult>() {

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error creating 3d image from html5 canvas");
            }

            @Override
            public void onSuccess(final EmptyResult result) {
                //reload page 
                Window.Location.reload();
            }
        });

    }

    @Override
    protected void showSection() {
        String url = GWT.getHostPageBaseURL() + RestEndpoints.BLOB_URL + getPreviewBean().getUri();

        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(url));
        try {
            @SuppressWarnings("unused")
            Request request = builder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    widget.setText("Error\n" + exception.getMessage());
                }

                public void onResponseReceived(Request request, Response response) {
                    if (200 == response.getStatusCode()) {

                        //Read file successfully; call Javascript to initialize html5 canvas
                        readWebGL(response.getText());

                    } else {
                        widget.setText("Error\n" + response.getStatusText());
                    }
                }
            });
        } catch (RequestException e) {
            widget.setText("Error\n" + e.getMessage());
        }

        widget.setHTML("<STYLE type='text/css'> canvas {border:solid 1px #000;} body{overflow:hidden;}</STYLE>" +
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
