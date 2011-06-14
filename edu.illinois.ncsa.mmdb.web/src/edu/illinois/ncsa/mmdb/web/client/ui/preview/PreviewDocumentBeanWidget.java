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
import edu.uiuc.ncsa.cet.bean.PreviewDocumentBean;

public class PreviewDocumentBeanWidget extends PreviewBeanWidget<PreviewDocumentBean> {
    public PreviewDocumentBeanWidget(HandlerManager eventBus) {
        super(eventBus);

        HTML widget = new HTML();
        widget.getElement().setId(DOM.createUniqueId());
        setWidget(widget);
    }

    @Override
    public PreviewDocumentBeanWidget newWidget() {
        return new PreviewDocumentBeanWidget(eventBus);
    }

    public Class<? extends PreviewBean> getPreviewBeanClass() {
        return PreviewDocumentBean.class;
    }

    @Override
    public String getAnchorText() {
        return "Text";
    }

    @Override
    public PreviewDocumentBean bestFit(PreviewDocumentBean obj1, PreviewDocumentBean obj2, int width, int height) {
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
        String url = GWT.getHostPageBaseURL() + RestEndpoints.BLOB_URL + getPreviewBean().getUri();
        setWidth(600);
        setHeight(500);

        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(url));
        try {
            @SuppressWarnings("unused")
            Request request = builder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    ((HTML) getWidget()).setText("Error\n" + exception.getMessage());
                }

                public void onResponseReceived(Request request, Response response) {
                    if (200 == response.getStatusCode()) {
                        String text = response.getText();
                        text = text.replaceAll("<", "&lt;");
                        text = text.replaceAll(" ", "&nbsp;");
                        text = text.replaceAll("\n", "<br />");
                        if (getEmbedded()) {
                            ((HTML) getWidget()).setSize(getWidth() + "px", getHeight() + "px");
                            ((HTML) getWidget()).addStyleName("textboxPreview");
                        }
                        ((HTML) getWidget()).setHTML("<div class='textboxPreview'>" + text + "</div>");
                    } else {
                        ((HTML) getWidget()).setText("Error\n" + response.getStatusText());
                    }

                }
            });
        } catch (RequestException e) {
            ((HTML) getWidget()).setText("Error\n" + e.getMessage());
        }
    }
}
