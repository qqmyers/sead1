package ncsa.mmdb.ui.providers;

import ncsa.mmdb.ui.services.IMimeTypeImageService;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import edu.uiuc.ncsa.cet.bean.DatasetBean;

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
        
        if ( element instanceof DatasetBean ) {
            DatasetBean b = (DatasetBean) element;
            mimeType = b.getMimeType();
        } else if ( element instanceof String ) {
            mimeType = (String) element;            
        }
        
        IMimeTypeImageService imageService = (IMimeTypeImageService) PlatformUI.getWorkbench().getService(
                IMimeTypeImageService.class );

        String[] parts = mimeType.toLowerCase().split("[/ ]"); //$NON-NLS-1$

        Image i = null;
        // check from major match
        i = imageService.getImage( parts[0] );
        if ( i == null )
            i = imageService.getImage( mimeType );
        
        return i;
    }
}
