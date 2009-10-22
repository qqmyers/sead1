package ncsa.mmdb.ui.adapters;

import java.util.Map;

import ncsa.bard.model.ISubjectSource;
import ncsa.bard.model.SubjectSource;
import ncsa.mmdb.ui.ImageHolder;
import ncsa.mmdb.ui.MMDBFrame;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.swt.graphics.Image;
import org.tupeloproject.rdf.Resource;

import edu.uiuc.ncsa.cet.bean.CETBean;

public class CETBeanAdapterFactory implements IAdapterFactory
{
    private Class<?>[] classes = new Class[] { ISubjectSource.class };

    private Map<ImageHolder, Image> images = MMDBFrame.getInstance().getImageCache();

    @SuppressWarnings("unchecked")
    public Object getAdapter( Object adaptableObject, Class adapterType )
    {
        if ( adapterType == ISubjectSource.class && adaptableObject instanceof CETBean ) {
            CETBean bean = (CETBean) adaptableObject;
            return new SubjectSource( Resource.resource( bean.getUri() ) );
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public Class[] getAdapterList()
    {
        return classes;
    }
}
