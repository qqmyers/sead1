package edu.illinois.ncsa.bard.ui.services.mimetype;

import java.net.URL;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

import edu.illinois.ncsa.bard.ui.osgi.Activator;

public class MimeTypeImageService implements IMimeTypeImageService
{
    private ImageRegistry imageRegistry = Activator.getDefault().getImageRegistry();

    public MimeTypeImageService()
    {
        read();
    }

    public Image getImage( String mimeType )
    {
        return imageRegistry.get( mimeType );
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
        final IConfigurationElement[] commandImagesExtensionPoint = registry.getConfigurationElementsFor( "ncsa.mmdb.ui.mimeTypeImages" );
        for ( int i = 0; i < commandImagesExtensionPoint.length; i++ ) {
            final IConfigurationElement configurationElement = commandImagesExtensionPoint[i];
            String mimeType = configurationElement.getAttribute( "mimeType" );
            String imageAtt = configurationElement.getAttribute( "image" );

            IContributor contributor = configurationElement.getContributor();
            Bundle bundle = Platform.getBundle( contributor.getName() );
            URL entry = bundle.getEntry( imageAtt );

            ImageDescriptor descriptor = ImageDescriptor.createFromURL( entry );
            if ( imageRegistry.get( mimeType ) == null ) {
                imageRegistry.put( mimeType, descriptor );
                System.err.println( "registered image for: " + mimeType );
            } else {
                // Add a warning in case this is a bug and two images are assigned to the same key
                System.err.println( "warning: image key " + mimeType + " already exists in image registry, skipping.  Two images might be assigned the same registry key." );
            }
        }
    }
}
