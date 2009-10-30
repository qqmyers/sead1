package ncsa.mmdb.ui;

import java.io.File;
import java.io.FileInputStream;
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

        if ( annotations.size() > 0 )
            item.setData( AbstractGalleryItemRenderer.OVERLAY_BOTTOM_RIGHT, Activator.getDefault().getImageRegistry().get( "note" ) );
        else
            item.setData( AbstractGalleryItemRenderer.OVERLAY_BOTTOM_RIGHT, null );
    }

    protected Image realizeOriginal()
    {
        try {
            // XXX: Hacks...not sure why but you get a "Unsupported or unrecognized format" if you read the stream directly into an image
            File dataFile = util.getDataFile( bean, File.createTempFile( "mmdb", ".tmp" ) );
            System.err.println( "Datafile: " + dataFile.getAbsolutePath() );
            dataFile.deleteOnExit();
            Image i = new Image( Display.getDefault(), new FileInputStream( dataFile ) );

            // XXX: What I think should work.
//            InputStream data = util.getData( bean );
//            Image i = new Image( Display.getDefault(), data );

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
