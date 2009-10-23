package ncsa.mmdb.ui;

import java.util.Set;

import ncsa.mmdb.ui.osgi.Activator;
import ncsa.mmdb.ui.utils.ImageUtils;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.nebula.widgets.gallery.AbstractGalleryItemRenderer;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.rdf.Resource;

import edu.uiuc.ncsa.cet.bean.AnnotationBean;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;

public class DatasetImageHolder extends ImageHolder
{
    private final BeanSession session;
    private final DatasetBean bean;
    private final DatasetBeanUtil util;

    static {
        ImageDescriptor imageDescriptor = Activator.getImageDescriptor( "icons/note.png" );
        Activator.getDefault().getImageRegistry().put( "note", imageDescriptor );
    }

    public DatasetImageHolder( BeanSession session, DatasetBean bean )
    {
        this.session = session;
        this.bean = bean;
        this.util = new DatasetBeanUtil( session );
    }

    public DatasetBean getBean()
    {
        return bean;
    }

    /**
     * This should really only be called on a per item basis as it makes a heavyweight call
     * to the underlying system.
     */
    public void updateOverlays( GalleryItem item )
    {
        try {
            session.refetch( Resource.resource( bean.getUri() ) );
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        Set<AnnotationBean> annotations = bean.getAnnotations();
        System.err.println( "Annotations: " + annotations );

        if ( annotations.size() > 0 )
            item.setData( AbstractGalleryItemRenderer.OVERLAY_BOTTOM_RIGHT, Activator.getDefault().getImageRegistry().get( "note" ) );
        else
            item.setData( AbstractGalleryItemRenderer.OVERLAY_BOTTOM_RIGHT, null );
    }

    protected Image realizeOriginal()
    {
        try {
            Image i = new Image( Display.getDefault(), util.getData( bean ) );
            return i;
        } catch ( Throwable e ) {
            e.printStackTrace();
            return null;
        }
    }

    protected Image realizeThumbnail()
    {
        Image orig = realizeOriginal();
        Image i = ImageUtils.resizeBestSize( orig, tWidth, tHeight );
        orig.dispose();
        return i;
    }
}
