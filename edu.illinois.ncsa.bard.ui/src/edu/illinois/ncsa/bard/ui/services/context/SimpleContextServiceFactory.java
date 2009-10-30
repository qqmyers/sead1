package edu.illinois.ncsa.bard.ui.services.context;

import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

public class SimpleContextServiceFactory extends AbstractServiceFactory
{
    public SimpleContextServiceFactory()
    {
    }

    @SuppressWarnings("unchecked")
    public Object create( Class serviceInterface, IServiceLocator parentLocator, IServiceLocator locator )
    {
        return new SimpleContextService();
    }
}
