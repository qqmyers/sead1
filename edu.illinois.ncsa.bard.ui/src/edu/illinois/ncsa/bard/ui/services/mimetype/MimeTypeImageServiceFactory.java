package edu.illinois.ncsa.bard.ui.services.mimetype;

import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

public class MimeTypeImageServiceFactory extends AbstractServiceFactory
{
    public MimeTypeImageServiceFactory()
    {
    }

    @SuppressWarnings("unchecked")
    public Object create( Class serviceInterface, IServiceLocator parentLocator, IServiceLocator locator )
    {
        return new MimeTypeImageService();
    }
}
