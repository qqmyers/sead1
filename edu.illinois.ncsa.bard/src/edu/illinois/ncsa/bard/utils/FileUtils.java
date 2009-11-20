package edu.illinois.ncsa.bard.utils;

import java.io.File;

public class FileUtils
{
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
