package edu.illinois.ncsa.bard.ui.services.predicate;

import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

public class PredicateImageServiceFactory extends AbstractServiceFactory
{
    public PredicateImageServiceFactory()
    {
    }

    @SuppressWarnings("unchecked")
    public Object create( Class serviceInterface, IServiceLocator parentLocator, IServiceLocator locator )
    {
        return new PredicateImageService();
    }
}
