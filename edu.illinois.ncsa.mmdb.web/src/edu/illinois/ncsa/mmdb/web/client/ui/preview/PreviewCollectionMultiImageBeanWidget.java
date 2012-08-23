/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui.preview;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.common.RestEndpoints;
import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.PreviewImageBean;
import edu.uiuc.ncsa.cet.bean.PreviewMultiImageBean;

/**
 * @author Nicholas Tenczar <tenczar2@illinois.edu>
 * 
 */
public class PreviewCollectionMultiImageBeanWidget extends PreviewBeanWidget<PreviewMultiImageBean> {
    private static final String   ANCHOR_TEXT        = "Collage";

    private static int            MAX_HEIGHT         = 150;
    private static int            MAX_WIDTH          = 300;

    private final HorizontalPanel container          = new HorizontalPanel();
    private boolean               displayInitialized = false;

    public PreviewCollectionMultiImageBeanWidget(HandlerManager eventBus) {
        super(eventBus);

        // Initialized the Composite.
        container.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        container.addStyleName("centered");
        setWidget(container);
    }

    @Override
    public PreviewBeanWidget<PreviewMultiImageBean> newWidget() {
        return new PreviewCollectionMultiImageBeanWidget(eventBus);
    }

    @Override
    public Class<? extends PreviewBean> getPreviewBeanClass() {
        return PreviewMultiImageBean.class;
    }

    @Override
    protected void showSection() {
        if (displayInitialized) {
            return;
        }

        VerticalPanel vert = new VerticalPanel();
        container.add(vert);

        int height = 0;

        for (PreviewImageBean image : getPreviewBean().getImages() ) {
            if (height + image.getHeight() > MAX_HEIGHT) {
                vert = new VerticalPanel();
                height = (int) image.getHeight();
                container.add(vert);
            } else {
                height += image.getHeight();
            }

            Image img = new Image(RestEndpoints.BLOB_URL + image.getUri());
            img.setTitle(image.getLabel());
            img.addStyleName("collectionImageCollage");

            vert.add(img);
        }

        displayInitialized = true;
    }

    @Override
    public void setSection(String section) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSection() {
        return "Image Collage";
    }

    @Override
    public String getAnchorText() {
        return ANCHOR_TEXT;
    }

}
