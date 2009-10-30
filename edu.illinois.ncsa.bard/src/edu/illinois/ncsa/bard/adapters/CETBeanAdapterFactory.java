package edu.illinois.ncsa.bard.adapters;

import org.eclipse.core.runtime.IAdapterFactory;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.bard.ISubjectSource;
import edu.illinois.ncsa.bard.SubjectSource;
import edu.uiuc.ncsa.cet.bean.CETBean;

public class CETBeanAdapterFactory implements IAdapterFactory
{
    private Class<?>[] classes = new Class[] { ISubjectSource.class };

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
