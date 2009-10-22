package ncsa.mmdb.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import ncsa.mmdb.ui.utils.ImageUtils;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.swt.graphics.Image;

public class TestImageHolder extends ImageHolder
{
    protected static Random rnd = new Random();

    protected String url = "http://www.scifi.co.uk/news/spider_Man_lizard-thumb-550x344-24925.jpg";
    //    protected URI url = new File( "/home/shawn/Desktop/maeviz-arch.png" ).toURI();
    protected String key;

    public TestImageHolder()
    {
        key = Long.toString( rnd.nextLong() );
    }

    public void updateOverlays( GalleryItem item )
    {
    }

    protected Image realizeOriginal()
    {
        try {
            ImageDescriptor descriptor = ImageDescriptor.createFromURL( new URL( url ) );
            Image i = descriptor.createImage();

            return i;
        } catch ( MalformedURLException e ) {
            e.printStackTrace();
            return null;
        }
    }

    protected Image realizeThumbnail()
    {
        try {
            ImageDescriptor descriptor = ImageDescriptor.createFromURL( new URL( url ) );
            Image orig = descriptor.createImage();
            Image i = ImageUtils.resizeBestSize( descriptor.createImage(), tWidth, tHeight );
            orig.dispose();
            return i;
        } catch ( MalformedURLException e ) {
            e.printStackTrace();
            return null;
        }
    }
}
