package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.common.RestEndpoints;
import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.PreviewImageBean;
import edu.uiuc.ncsa.cet.bean.PreviewMultiImageBean;

public class PreviewMultiImageBeanWidget extends PreviewBeanWidget<PreviewMultiImageBean> {
    /** maximum width of a preview image */
    private static final long MAX_WIDTH  = 600;

    /** maximum height of a preview image */
    private static final long MAX_HEIGHT = 600;

    /** current image currently shown */
    private int               current    = 0;

    /** What image are we looking at? */
    private final Label       page;

    /** Image that is shown */
    private final Image       image;

    private final Anchor      prev;

    private final Anchor      next;

    public PreviewMultiImageBeanWidget() {
        VerticalPanel vp = new VerticalPanel();
        vp.addStyleName("centered");

        HorizontalPanel hp = new HorizontalPanel();
        hp.addStyleName("centered");
        vp.add(hp);

        prev = new Anchor("< ");
        prev.addStyleName("previewActionLink");
        prev.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                current--;
                show();
            }
        });
        hp.add(prev);

        image = new Image();
        image.getElement().setId(DOM.createUniqueId());
        hp.add(image);

        next = new Anchor(" >");
        next.addStyleName("previewActionLink");
        next.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                current++;
                show();
            }
        });
        hp.add(next);

        page = new Label("");
        page.addStyleName("centered");
        vp.add(page);

        setWidget(vp);
    }

    @Override
    public PreviewBeanWidget<PreviewMultiImageBean> newWidget() {
        return new PreviewMultiImageBeanWidget();
    }

    @Override
    public Class<? extends PreviewBean> getPreviewBeanClass() {
        return PreviewMultiImageBean.class;
    }

    @Override
    public String getAnchorText() {
        return "Multi Image";
    }

    @Override
    public String getCurrent() {
        return Integer.toString(current + 1);
    }

    @Override
    public void show() {
        int maximage = (getPreviewBean().getImages().size() - 1);
        if (maximage < 0) {
            return;
        }
        if (current < 0) {
            current = 0;
        }
        if (current > maximage) {
            current = maximage;
        }

        if (current == 0) {
            prev.addStyleName("deadlink");
        } else if (maximage != 0) {
            prev.removeStyleName("deadlink");
        }

        if (current == maximage) {
            next.addStyleName("deadlink");
        } else if (maximage != 0) {
            next.removeStyleName("deadlink");
        }

        PreviewImageBean pib = getPreviewBean().getImages().get(current);
        long w = pib.getWidth();
        long h = pib.getHeight();
        if (w > h) {
            if (w > MAX_WIDTH) {
                h = (long) (h * (double) MAX_WIDTH / w);
                w = MAX_WIDTH;
            }
        } else {
            if (h > MAX_HEIGHT) {
                w = (long) (w * (double) MAX_HEIGHT / h);
                h = MAX_HEIGHT;
            }
        }

        showImage(RestEndpoints.BLOB_URL + pib.getUri(), image.getElement().getId(), Long.toString(w), Long.toString(h));
        page.setText((current + 1) + " / " + (maximage + 1));
    }

    public final native void showImage(String url, String id, String w, String h) /*-{
        img = $doc.getElementById(id);
        img.src=url;
        img.width=w;
        img.height=h;
    }-*/;

}
