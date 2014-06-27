package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.common.RestEndpoints;
import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.PreviewTabularDataBean;

public class PreviewTabularDataBeanWidget extends PreviewBeanWidget<PreviewTabularDataBean> {
    //    private static final long ROW_SIZE = 15;
    private static final long   COL_SIZE = 100;
    private static final String regex    = "\",\\s*\"";
    private static final int    MAX_ROWS = 50;
    private static final int    MAX_COLS = 20;

    private final HTML          html     = new HTML();

    public PreviewTabularDataBeanWidget(HandlerManager eventBus) {
        super(eventBus);

        VerticalPanel vp = new VerticalPanel();
        vp.addStyleName("centered"); //$NON-NLS-1$
        vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        html.addStyleName("tablePreview");
        vp.add(html);
        setWidget(vp);
    }

    @Override
    public PreviewTabularDataBeanWidget newWidget() {
        return new PreviewTabularDataBeanWidget(eventBus);
    }

    public Class<? extends PreviewBean> getPreviewBeanClass() {
        return PreviewTabularDataBean.class;
    }

    @Override
    public String getAnchorText() {
        return "Table";
    }

    @Override
    public PreviewTabularDataBean bestFit(PreviewTabularDataBean obj1, PreviewTabularDataBean obj2, int width, int height) {
        if ((width > -1) && (Math.abs(width - (COL_SIZE * obj1.getCol())) < Math.abs(width - (COL_SIZE * obj2.getCol())))) {
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

                public void onResponseReceived(Request request, Response response) {
                    if (200 == response.getStatusCode()) {
                        String text = response.getText();
                        String html_text = toHTML(text);
                        if (getEmbedded()) {
                            //TODO get font and set height of table accordingly
                            html.addStyleName("tablePreview");
                            //                            ((HTML) getWidget()).setSize("600" + "px", "500" + "px");
                        }
                        html.setHTML(html_text);

                        addSort();

                    } else {
                        html.setText("Error\n" + response.getStatusText());
                    }
                }
            });
        } catch (RequestException e) {
            ((HTML) getWidget()).setText("Error\n" + e.getMessage());
        }
    }

    public static native void addSort() /*-{
		$wnd.jQuery("#sortedtable").tablesorter();
    }-*/;

    public static String toHTML(String text) {
        // parse the saved csv String cell by cell and encode it into html table
        String html = new String();
        String code = new String();
        code = "<table id=\"sortedtable\" class=\"tablesorter\">";
        html = html + code + "\n";
        String[] line = text.split("\n");
        String[] col = line[0].substring(1, line[0].length() - 1).split(
                regex);
        int num_row = Integer.parseInt(col[0]);
        int num_col = Integer.parseInt(col[1]);
        int linelength = line.length;
        if (linelength > 1) {
            html += "<thead>\n";
        } else {
            html += "<tbody>\n";
        }
        String item = "td";
        for (int i = 1; i < line.length; i++ ) {
            if (i % 2 == 0) {
                code = "\t<tr>";
            } else {
                code = "\t<tr>";
            }
            html = html + code + "\n";
            if ((i == 1) && (linelength > 1)) {
                item = "th";
            } else {
                item = "td";
            }
            col = line[i].substring(1, line[i].length() - 1).split(regex);
            for (int j = 0; j < col.length; j++ ) {
                code = "\t\t<" + item + ">" + col[j].replace("\\\"", "\"").replace("\\\\", "\\") + "</" + item + ">";
                html = html + code + "\n";
            }
            code = "\t</tr>";
            html = html + code + "\n";
            if ((i == 1) && (linelength > 1)) {
                html += "</thead>\n<tbody>\n";
            }
        }
        code = "</tbody>\n</table>";
        html = html + code + "\n";
        if (num_col > MAX_COLS) {
            html = "<table> <tr> <td>" + html + "</td> <td> More... </td> </tr> </table>";
        }
        if (num_row > MAX_ROWS) {
            html = "<table style=\"text-align:center;\"> <tr> <td>" + html + "</td> </tr> <tr> <td colspan=\"" + Math.min(num_col, MAX_COLS) + "\"> More... </td> </tr> </table>";
        }
        html = "<div>" + html + "</div>";

        return html;
    }
}
