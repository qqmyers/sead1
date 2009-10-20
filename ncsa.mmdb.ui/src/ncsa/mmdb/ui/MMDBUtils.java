package ncsa.mmdb.ui;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.impl.MemoryContext;

import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CETBeans;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;

public class MMDBUtils
{
    private static Context localContext;
    private static Context remoteContext;

    private static BeanSession localBean;
    private static BeanSession remoteBean;

    private static Map<String, String> mimeTypes = new HashMap<String, String>();

    public static Context getLocalContext()
    {
        if ( localContext == null )
            localContext = new MemoryContext();

        return localContext;
    }

    public static Context getRemoteContext()
    {
        return null;
    }

    public static Context getDefaultContext()
    {
        return getLocalContext();
    }

    public static BeanSession getLocalBeanSession()
    {
        if ( localBean == null ) {
            try {
                localBean = CETBeans.createBeanSession( getLocalContext() );
            } catch ( OperatorException e ) {
                e.printStackTrace();
            } catch ( ClassNotFoundException e ) {
                e.printStackTrace();
            }
        }

        return localBean;
    }

    public static BeanSession getRemoteBeanSession()
    {
        if ( remoteBean == null ) {
            try {
                remoteBean = CETBeans.createBeanSession( getRemoteContext() );
            } catch ( OperatorException e ) {
                e.printStackTrace();
            } catch ( ClassNotFoundException e ) {
                e.printStackTrace();
            }
        }

        return remoteBean;
    }

    public static BeanSession getDefaultBeanSession()
    {
        return getLocalBeanSession();
    }

    public static PersonBean getCurrentUser()
    {
        return PersonBeanUtil.getAnonymous();
    }

    public static String getMimeType( File file )
    {
        String extension = getExtension( file );
        return getMimeTypeForExtension( extension );
    }

    public static String getMimeType( String name )
    {
        String extension = getExtension( name );
        return getMimeTypeForExtension( extension );
    }
    
    public static String getMimeTypeForExtension( String extension )
    {
        if ( mimeTypes.isEmpty() ) {
            mimeTypes.put( "bmp", "image/bmp" );
            mimeTypes.put( "cod", "image/cis-cod" );
            mimeTypes.put( "gif", "image/gif" );
            mimeTypes.put( "ief", "image/ief" );
            mimeTypes.put( "jpe", "image/jpeg" );
            mimeTypes.put( "jpeg", "image/jpeg" );
            mimeTypes.put( "jpg", "image/jpeg" );
            mimeTypes.put( "jfif", "image/pipeg" );
            mimeTypes.put( "svg", "image/svg+xml" );
            mimeTypes.put( "tif", "image/tiff" );
            mimeTypes.put( "tiff", "image/tiff" );
            mimeTypes.put( "ras", "image/x-cmu-raster" );
            mimeTypes.put( "cmx", "image/x-cmx" );
            mimeTypes.put( "ico", "image/x-icon" );
            mimeTypes.put( "png", "image/png" );
            mimeTypes.put( "pnm", "image/x-portable-anymap" );
            mimeTypes.put( "pbm", "image/x-portable-bitmap" );
            mimeTypes.put( "pgm", "image/x-portable-graymap" );
            mimeTypes.put( "ppm", "image/x-portable-pixmap" );
            mimeTypes.put( "rgb", "image/x-rgb" );
            mimeTypes.put( "xbm", "image/x-xbitmap" );
            mimeTypes.put( "xpm", "image/x-xpixmap" );
            mimeTypes.put( "xwd", "image/x-xwindowdump" );
        }
        
        if ( extension.startsWith( "." ) )
            extension = extension.substring( 1 );
        
        String type = mimeTypes.get( extension );
        if ( type == null )
            type = "unk";
        
        return type;
    }
    
    public static String getName( File file )
    {
        String s = file.getName();        
        int i = s.lastIndexOf( "." );
        s = s.substring( 0, i );
        return s;
    }

    public static String getExtension( File file )
    {
        return getExtension( file.getName() );
    }
    
    public static String getName( String name )
    {
        File f = new File( name );
        return getName( f );
    }

    public static String getExtension( String name )
    {
        int i = name.lastIndexOf( "." );
        name = name.substring( i + 1 );
        
        return name;
    }

}
