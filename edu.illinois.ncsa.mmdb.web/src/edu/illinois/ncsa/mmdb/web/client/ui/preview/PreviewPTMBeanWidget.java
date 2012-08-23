/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import java.math.BigDecimal;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HTML;

import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.PreviewPTMBean;

/**
 * @author Nicholas Tenczar <tenczar2@illinois.edu>
 * 
 */
public class PreviewPTMBeanWidget extends PreviewBeanWidget<PreviewPTMBean> {
    private static final int    MAX_WIDTH    = 600;
    private static final int    MAX_HEIGHT   = 600;

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

        long height = getPreviewBean().getHeight();
        long width = getPreviewBean().getWidth();

        if (height > MAX_HEIGHT || width > MAX_WIDTH) {
            BigDecimal max = new BigDecimal(MAX_WIDTH);
            BigDecimal original = new BigDecimal(Math.max(height, width));

            BigDecimal quotient = max.divide(original, BigDecimal.ROUND_HALF_UP);

            height = quotient.multiply(new BigDecimal(height)).longValue();
            width = quotient.multiply(new BigDecimal(width)).longValue();
        }

        StringBuilder sb = new StringBuilder();

        sb = new StringBuilder();
        sb.append("<applet archive=\"plugins/envlib.jar\" code=\"jpview/gui/Standalone.class\" height=\"" + height + "\" width=\"" + width + "\">");
        sb.append(createParam("ptmfile", "../../" + DOWNLOAD_URL + getPreviewBean().getUri()));
        sb.append(createParam("bg_red", "100"));
        sb.append(createParam("bg_green", "100"));
        sb.append(createParam("bg_blue", "100"));
        sb.append(createParam("pw", width + ""));
        sb.append(createParam("ph", height + ""));
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
