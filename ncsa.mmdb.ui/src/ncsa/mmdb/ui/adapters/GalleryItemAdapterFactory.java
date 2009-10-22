package ncsa.mmdb.ui.adapters;

import java.util.Map;

import ncsa.bard.model.ISubjectSource;
import ncsa.bard.model.SubjectSource;
import ncsa.mmdb.ui.DatasetImageHolder;
import ncsa.mmdb.ui.ImageHolder;
import ncsa.mmdb.ui.MMDBFrame;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.swt.graphics.Image;
import org.tupeloproject.rdf.Resource;

import edu.uiuc.ncsa.cet.bean.DatasetBean;

public class GalleryItemAdapterFactory implements IAdapterFactory
{
    private Class<?>[] classes = new Class[] { ISubjectSource.class };

    private Map<ImageHolder, Image> images = MMDBFrame.getInstance().getImageCache();

    @SuppressWarnings("unchecked")
    public Object getAdapter( Object adaptableObject, Class adapterType )
    {
        if ( adapterType == ISubjectSource.class && adaptableObject instanceof GalleryItem ) {
            GalleryItem gi = (GalleryItem) adaptableObject;
            Object data = gi.getData( "holder" );
            if ( data instanceof DatasetImageHolder ) {
                DatasetImageHolder ih = (DatasetImageHolder) data;
                DatasetBean bean = ih.getBean();
                return new SubjectSource( Resource.resource( bean.getUri() ) );
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public Class[] getAdapterList()
    {
        return classes;
    }
}
