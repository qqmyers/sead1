package ncsa.mmdb.ui;

import java.util.Set;

import ncsa.mmdb.ui.osgi.Activator;
import ncsa.mmdb.ui.utils.ImageUtils;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.tupeloproject.kernel.BeanSession;

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

    public void updateOverlays( GalleryItem item )
    {
        try {
            util.update( bean );
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        Set<AnnotationBean> annotations = bean.getAnnotations();
        System.err.println( "Annotations: " + annotations );
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
