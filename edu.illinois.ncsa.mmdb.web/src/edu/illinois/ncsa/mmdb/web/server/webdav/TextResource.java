package edu.illinois.ncsa.mmdb.web.server.webdav;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.tupeloproject.kernel.Context;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.SecurityManager;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

/**
 * Wrapper around the DatasetBean. This will show information about the dataset
 * and allow to download it.
 * 
 * @author Rob Kooper
 * 
 */
public class TextResource extends AbstractResource implements GetableResource
{
    private String text;

    public TextResource( String name, String text, Context context, SecurityManager security )
    {
        super( name, context, security );
        this.text = text;
    }

    // ----------------------------------------------------------------------
    // GetableResource
    // ----------------------------------------------------------------------
    @Override
    public Long getContentLength()
    {
        return new Long( text.length() );
    }

    @Override
    public String getContentType( String accepts )
    {
        return "text/plain"; //$NON-NLS-1$
    }

    @Override
    public Long getMaxAgeSeconds( Auth auth )
    {
        return new Long( 601 );
    }

    @Override
    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException
    {
        out.write( text.getBytes( "UTF-8" ) ); //$NON-NLS-1$
    }
}
