package edu.illinois.ncsa.bard.adapters;

import org.eclipse.core.runtime.IAdapterFactory;

import edu.illinois.ncsa.bard.HasMimeType;
import edu.illinois.ncsa.bard.SimpleMimeTypeContainer;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

public class DatasetBeanAdapterFactory implements IAdapterFactory
{
    private Class<?>[] classes = new Class[] { HasMimeType.class };

    @SuppressWarnings("unchecked")
    public Object getAdapter( Object adaptableObject, Class adapterType )
    {
        if ( adapterType == HasMimeType.class && adaptableObject instanceof DatasetBean ) {
            DatasetBean bean = (DatasetBean) adaptableObject;
            return new SimpleMimeTypeContainer( bean.getMimeType() );
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public Class[] getAdapterList()
    {
        return classes;
    }
}
