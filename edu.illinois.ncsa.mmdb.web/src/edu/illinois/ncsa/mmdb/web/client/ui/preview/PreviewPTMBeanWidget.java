/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HTML;

import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.PreviewPTMBean;

/**
 * @author Nicholas Tenczar <tenczar2@illinois.edu>
 * 
 */
public class PreviewPTMBeanWidget extends PreviewBeanWidget<PreviewPTMBean> {
    private static final String ANCHOR_TEXT  = "Polynomial Texture Map";
    private static final String DOWNLOAD_URL = "api/image/";

    private final HTML          panel        = new HTML();
    private boolean             initialized  = false;

    public PreviewPTMBeanWidget(HandlerManager eventBus) {
        super(eventBus);

        setWidget(panel);
    }

    @Override
    public PreviewBeanWidget<PreviewPTMBean> newWidget() {
        return new PreviewPTMBeanWidget(eventBus);
    }

    @Override
    public Class<? extends PreviewBean> getPreviewBeanClass() {
        return PreviewPTMBean.class;
    }

    @Override
    protected void showSection() {
        if (initialized) {
            return;
        }

        StringBuilder sb = new StringBuilder();

        sb.append("<object archive=\"plugins/envlib.jar\" classid=\"java:jpview/gui/Standalone.class\" type=\"application/x-java-applet\" width=\"400\" height=\"400\">");
        sb.append(createParam("archive", "plugins/envlib.jar"));
        sb.append(createParam("ptmfile", "../../" + DOWNLOAD_URL + getPreviewBean().getUri()));
        sb.append(createParam("bg_red", "100"));
        sb.append(createParam("bg_green", "100"));
        sb.append(createParam("bg_blue", "100"));
        sb.append(createParam("pw", "400"));
        sb.append(createParam("ph", "400"));

        sb.append("<object classid=\"clsid:8AD9C840-044E-11D1-B3E9-00805F499D93\" codebase=\"http://java.sun.com/update/1.6.0/jinstall-6u20-windows-i586.cab\" width=\"400\" height=\"400\" />");
        sb.append(createParam("code", "Standalone.class"));
        sb.append(createParam("archive", "plugins/envlib.jar"));
        sb.append(createParam("ptmfile", "../../" + DOWNLOAD_URL + getPreviewBean().getUri()));
        sb.append(createParam("bg_red", "100"));
        sb.append(createParam("bg_green", "100"));
        sb.append(createParam("bg_blue", "100"));
        sb.append(createParam("pw", "400"));
        sb.append(createParam("ph", "400"));

        sb.append("</object></object>");

        sb = new StringBuilder();
        sb.append("<applet archive=\"plugins/envlib.jar\" code=\"jpview/gui/Standalone.class\" height=\"400\" width=\"400\">");
        sb.append(createParam("ptmfile", "../../" + DOWNLOAD_URL + getPreviewBean().getUri()));
        sb.append(createParam("bg_red", "100"));
        sb.append(createParam("bg_green", "100"));
        sb.append(createParam("bg_blue", "100"));
        sb.append(createParam("pw", "400"));
        sb.append(createParam("ph", "400"));
        sb.append("</applet>");

        panel.setHTML(sb.toString());

        initialized = true;
    }

    @Override
    public void setSection(String section) throws IllegalArgumentException {
        // TODO Auto-generated method stub

    }

    @Override
    public String getSection() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAnchorText() {
        return ANCHOR_TEXT;
    }

    private String createParam(String name, String value) {
        return "<param name=\"" + name + "\" value=\"" + value + "\" />";
    }

}
