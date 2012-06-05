package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.common.RestEndpoints;
import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.PreviewMultiTabularDataBean;
import edu.uiuc.ncsa.cet.bean.PreviewTabularDataBean;

public class PreviewMultiTabularDataBeanWidget extends PreviewBeanWidget<PreviewMultiTabularDataBean> {
    /** current image currently shown */
    private String        current = new String();
    private final HTML    html    = new HTML();
    private final ListBox lb      = new ListBox();

    public PreviewMultiTabularDataBeanWidget(HandlerManager eventBus) {
        super(eventBus);

        VerticalPanel vp = new VerticalPanel();
        vp.addStyleName("centered"); //$NON-NLS-1$
        vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        html.addStyleName("tablePreview");
        vp.add(html);
        lb.addStyleName("centered");
        vp.add(lb);

        lb.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                current = lb.getValue(lb.getSelectedIndex());
                show();
            }
        });

        setWidget(vp);
    }

    @Override
    public PreviewBeanWidget<PreviewMultiTabularDataBean> newWidget() {
        return new PreviewMultiTabularDataBeanWidget(eventBus);
    }

    @Override
    public Class<? extends PreviewBean> getPreviewBeanClass() {
        return PreviewMultiTabularDataBean.class;
    }

    @Override
    public String getAnchorText() {
        return "SpreadSheet";
    }

    @Override
    public void setSection(String section) throws IllegalArgumentException {
        if ((section == null) || (section.length() < 7) || !section.toLowerCase().startsWith("sheet ")) {
            throw (new IllegalArgumentException("Expected text to start with sheet"));
        }

        current = section.substring(6);
    }

    @Override
    public String getSection() {
        return "Sheet " + current;
    }

    @Override
    protected void showSection() {
        if (lb.getItemCount() == 0) {
            GWT.log("add sheet's names");
            String[] names = getPreviewBean().getTables().keySet().toArray(new String[0]);

            for (int i = 0; i < names.length; i++ ) {
                lb.addItem(names[i]);
            }
            current = lb.getValue(lb.getSelectedIndex());
        }
        for (int i = 0; i < lb.getItemCount(); i++ ) {
            if (lb.getItemText(i).equals(current)) {
                lb.setSelectedIndex(i);
                i = lb.getItemCount();
            }

        }
        PreviewTabularDataBean ptb = getPreviewBean().getTables().get(current);
        if (ptb != null) {
            String url = GWT.getHostPageBaseURL() + RestEndpoints.BLOB_URL + ptb.getUri();
            if (!getEmbedded()) {
                setWidth(600);
                setHeight(500);
            }

            RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(url));
            try {
                @SuppressWarnings("unused")
                Request request = builder.sendRequest(null, new RequestCallback() {
                    public void onError(Request request, Throwable exception) {
                        html.setText("Error\n" + exception.getMessage());
                    }

                    // get saved csv file created by an extractor and convert it to HTML
                    public void onResponseReceived(Request request, Response response) {
                        if (200 == response.getStatusCode()) {
                            String text = response.getText();
                            String current_html = PreviewTabularDataBeanWidget.toHTML(text);
                            if (getEmbedded()) {
                                //TODO get font and set height of table accordingly
                                html.addStyleName("tablePreview");
                                //                            ((HTML) getWidget()).setSize("600" + "px", "500" + "px");
                            }
                            html.setHTML(current_html);
                        } else {
                            html.setText("Error\n" + response.getStatusText());
                        }
                    }
                });
            } catch (RequestException e) {
                html.setText("Error\n" + e.getMessage());
            }
        }
    }
}
