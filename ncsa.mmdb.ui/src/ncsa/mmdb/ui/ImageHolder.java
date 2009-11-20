package ncsa.mmdb.ui;

import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.swt.graphics.Image;

public abstract class ImageHolder
{
    protected int tHeight = 64;
    protected int tWidth = 64;

    protected abstract Image realizeOriginal();
    protected abstract Image realizeThumbnail();

    public Image getOriginal()
    {
        return realizeOriginal();
    }

    public Image getThumbnail()
    {
        return realizeThumbnail();
    }
    
    public abstract void updateOverlays( GalleryItem item );
}
