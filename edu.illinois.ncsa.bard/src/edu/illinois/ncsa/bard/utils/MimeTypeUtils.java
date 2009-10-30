package edu.illinois.ncsa.bard.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MimeTypeUtils
{
    private static Map<String, String> mimeTypes = new HashMap<String, String>();

    private MimeTypeUtils()
    {
        
    }
    
    public static String getMimeType( File file )
    {
        String extension = FileUtils.getExtension( file );
        return getMimeTypeForExtension( extension );
    }

    public static String getMimeType( String name )
    {
        String extension = FileUtils.getExtension( name );
        return getMimeTypeForExtension( extension );
    }

    public static String getMimeTypeForExtension( String extension )
    {
        extension = extension.toLowerCase();
        
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

}
