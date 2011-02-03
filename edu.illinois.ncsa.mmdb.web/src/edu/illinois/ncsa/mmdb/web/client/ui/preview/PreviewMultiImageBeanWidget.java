package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import java.text.ParseException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
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

    /** Image that is shown */
    private final Image       image;

    /** Previous page navigation Icon */
    private final Image       prevImage;

    /** Next page navigation Icon */
    private final Image       nextImage;

    /** What page are we looking at? */
    private final TextBox     currPage;

    /** How many pages are there? */
    private final Label       maxPage;

    public PreviewMultiImageBeanWidget(HandlerManager eventBus) {
        super(eventBus);

        VerticalPanel vp = new VerticalPanel();
        vp.addStyleName("centered"); //$NON-NLS-1$

        HorizontalPanel hp = new HorizontalPanel();
        hp.addStyleName("centered"); //$NON-NLS-1$
        vp.add(hp);

        prevImage = new Image("images/go-previous-gray.png"); //$NON-NLS-1$
        prevImage.addStyleName("previewActionLink"); //$NON-NLS-1$
        prevImage.setTitle("Previous");
        prevImage.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                current--;
                show();
            }
        });
        hp.add(prevImage);
        hp.setCellVerticalAlignment(prevImage, HasVerticalAlignment.ALIGN_MIDDLE);

        image = new Image();
        hp.add(image);

        nextImage = new Image("images/go-next-gray.png"); //$NON-NLS-1$
        nextImage.addStyleName("previewActionLink"); //$NON-NLS-1$
        nextImage.setTitle("Next");
        nextImage.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                current++;
                show();
            }
        });
        hp.add(nextImage);
        hp.setCellVerticalAlignment(nextImage, HasVerticalAlignment.ALIGN_MIDDLE);

        hp = new HorizontalPanel();
        hp.addStyleName("centered"); //$NON-NLS-1$
        vp.add(hp);

        hp.add(new Label("Page "));

        currPage = new TextBox();
        //currPage.addStyleName("multiAnchor");
        currPage.setVisibleLength(4);
        hp.add(currPage);

        Label lbl = new Label(" of ");
        lbl.addStyleName("multiAnchor");
        hp.add(lbl);

        maxPage = new Label();
        maxPage.addStyleName("multiAnchor");
        hp.add(maxPage);

        currPage.addKeyDownHandler(new KeyDownHandler() {

            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    try {
                        current = Integer.parseInt(currPage.getText()) - 1;
                    } catch (NumberFormatException e) {
                        GWT.log("Could not parse " + currPage.getText(), e);
                    }
                    show();
                }
            }
        });

        setWidget(vp);
    }

    @Override
    public PreviewBeanWidget<PreviewMultiImageBean> newWidget() {
        return new PreviewMultiImageBeanWidget(eventBus);
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
    public void setSection(String section) throws ParseException {
        if ((section == null) || (section.length() < 6) || !section.toLowerCase().startsWith("page ")) {
            throw (new ParseException("Expected text to start with page", 0));
        }

        String text = section.substring(5);
        try {
            int page = Integer.parseInt(text) - 1;
            if (current == page) {
                return;
            }
            current = page;
        } catch (NumberFormatException e) {
            throw (new ParseException("Could not parse " + section, 6));
        }
    }

    @Override
    public String getSection() {
        return "Image " + Integer.toString(current + 1);
    }

    @Override
    protected void showSection() {
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
            prevImage.addStyleName("hidden"); //$NON-NLS-1$
        } else if (maximage != 0) {
            prevImage.removeStyleName("hidden"); //$NON-NLS-1$
        }

        if (current == maximage) {
            nextImage.addStyleName("hidden"); //$NON-NLS-1$
        } else if (maximage != 0) {
            nextImage.removeStyleName("hidden"); //$NON-NLS-1$
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

        image.setWidth(Long.toString(w));
        image.setHeight(Long.toString(h));
        image.setUrl(RestEndpoints.BLOB_URL + pib.getUri());

        maxPage.setText(Integer.toString(maximage + 1));
        currPage.setText(Integer.toString(current + 1));
    }
}
