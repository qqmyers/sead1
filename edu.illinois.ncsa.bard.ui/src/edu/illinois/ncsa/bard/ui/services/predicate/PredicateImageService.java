package edu.illinois.ncsa.bard.ui.services.predicate;

import java.net.URL;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.bard.ui.osgi.Activator;

public class PredicateImageService implements IPredicateImageService
{
    private ImageRegistry imageRegistry = Activator.getDefault().getImageRegistry();

    public PredicateImageService()
    {
        read();
    }

    public Image getImage( Resource predicate, Resource value )
    {
        return imageRegistry.get( predicate.getString() + ":" + value.getString() );
    }

    /**
     * Reads all of the images from the registry.
     */
    protected final void read()
    {
        final IExtensionRegistry registry = Platform.getExtensionRegistry();
        int imageCount = 0;
        final IConfigurationElement[][] indexedConfigurationElements = new IConfigurationElement[1][];

        // Sort the commands extension point based on element name.
        final IConfigurationElement[] commandImagesExtensionPoint = registry.getConfigurationElementsFor( "ncsa.bard.ui.predicateImages" );
        for ( int i = 0; i < commandImagesExtensionPoint.length; i++ ) {
            final IConfigurationElement configurationElement = commandImagesExtensionPoint[i];
            String predicateAtt = configurationElement.getAttribute( "predicate" );
            String valueAtt = configurationElement.getAttribute( "value" );
            String imageAtt = configurationElement.getAttribute( "image" );

            IContributor contributor = configurationElement.getContributor();
            Bundle bundle = Platform.getBundle( contributor.getName() );
            URL entry = bundle.getEntry( imageAtt );

            ImageDescriptor descriptor = ImageDescriptor.createFromURL( entry );
            // CMN: this will fail if we call read() twice because the imageRegistry
            // will throw an illegal argument exception if a key already points to a real image
            if ( imageRegistry.get( predicateAtt + ":" + valueAtt ) == null ) {
                imageRegistry.put( predicateAtt + ":" + valueAtt, descriptor );
            } else {
                // Add a warning in case this is a bug and two images are assigned to the same key
//                logger.warn( "warning: image key " + predicateAtt + ":" + valueAtt
//                        + " already exists in image registry, skipping.  Two images might be assigned the same registry key." );
            }
        }
    }
}
