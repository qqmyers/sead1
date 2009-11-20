package ncsa.mmdb.ui.services;

import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

public class MimeTypeImageServiceFactory extends AbstractServiceFactory
{
    public MimeTypeImageServiceFactory()
    {
    }

    public Object create( Class serviceInterface, IServiceLocator parentLocator, IServiceLocator locator )
    {
        return new MimeTypeImageService();
    }
}
