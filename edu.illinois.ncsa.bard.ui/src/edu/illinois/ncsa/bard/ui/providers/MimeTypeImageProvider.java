package edu.illinois.ncsa.bard.ui.providers;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import edu.illinois.ncsa.bard.HasMimeType;
import edu.illinois.ncsa.bard.ui.services.mimetype.IMimeTypeImageService;

public class MimeTypeImageProvider
{
    private static MimeTypeImageProvider instance;

    protected MimeTypeImageProvider()
    {
        // no op
    }

    public static MimeTypeImageProvider getInstance()
    {
        if ( instance == null )
            instance = new MimeTypeImageProvider();

        return instance;
    }

    public Image getImage( Object element )
    {
        String mimeType = null;

        Platform.getAdapterManager().getAdapter( element, HasMimeType.class );

        if ( element instanceof HasMimeType ) {
            HasMimeType b = (HasMimeType) element;
            mimeType = b.getMimeType();
        } else if ( element instanceof String ) {
            mimeType = (String) element;
        } else {
            HasMimeType t = (HasMimeType) Platform.getAdapterManager().getAdapter( element, HasMimeType.class );
            if ( t != null )
                mimeType = t.getMimeType();
        }
        
        IMimeTypeImageService imageService = (IMimeTypeImageService) PlatformUI.getWorkbench().getService( IMimeTypeImageService.class );

        String[] parts = mimeType.toLowerCase().split( "[/ ]" ); //$NON-NLS-1$

        Image i = null;
        // check from major match
        i = imageService.getImage( parts[0] );
        if ( i == null )
            i = imageService.getImage( mimeType );

        return i;
    }
}
