package ncsa.mmdb.ui.utils;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.rdf.Resource;

import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;

public class MMDBUtils
{
    private static Map<String, String> mimeTypes = new HashMap<String, String>();

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

    public static DatasetBean importDatasetFromFile( BeanSession session, String fileName )
    {
        DatasetBeanUtil util = new DatasetBeanUtil( session );
        DatasetBean bean = new DatasetBean();
        bean.setCreator( MMDBUtils.getCurrentUser() );
        bean.setDate( Calendar.getInstance().getTime() );
        bean.setTitle( MMDBUtils.getName( fileName ) );
        bean.setMimeType( MMDBUtils.getMimeType( fileName ) );

        try {
            Resource subject = session.registerAndSave( bean );
            util.setData( bean, fileName );
        } catch ( Throwable t ) {
            t.printStackTrace();
        }

        return bean;
    }

}
