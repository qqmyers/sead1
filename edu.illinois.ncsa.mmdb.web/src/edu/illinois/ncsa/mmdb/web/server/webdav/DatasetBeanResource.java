package edu.illinois.ncsa.mmdb.web.server.webdav;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import org.tupeloproject.kernel.BlobFetcher;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.rdf.Resource;

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
public class DatasetBeanResource extends AbstractResource implements GetableResource
{
    private long     size;
    private Resource uri;
    private String   mimetype;

    public DatasetBeanResource( String name, Resource uri, long size, Date date, String mimetype, Context context, SecurityManager security )
    {
        super( name, uri, date, context, security );
        this.uri = uri;
        this.size = size;
        this.mimetype = mimetype;
    }

    // ----------------------------------------------------------------------
    // Resource
    // ----------------------------------------------------------------------

    @Override
    public String getName()
    {
        // FIXME add extention if wrong one.
        String name = super.getName();
        if ((mimetype.equals( "image/jpeg" ) || mimetype.equals( "image/jpg" )) && !name.endsWith( ".jpg" )) {
            name = name + ".jpg";            
        }
        return name;
    }

    // ----------------------------------------------------------------------
    // GetableResource
    // ----------------------------------------------------------------------

    @Override
    public Long getContentLength()
    {
        if ( size <= 0 ) {
            return null;
        }
        return size;
    }

    @Override
    public String getContentType( String accepts )
    {
        // FIXME should really use accepts
        return mimetype;
    }

    @Override
    public Long getMaxAgeSeconds( Auth auth )
    {
        return new Long( 601 );
    }

    @Override
    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException
    {
        BlobFetcher bf = new BlobFetcher();
        bf.setSubject( uri );
        try {
            getContext().perform( bf );
        } catch ( OperatorException e ) {
            throw (new IOException( e ));
        }
        byte[] buf = new byte[1024];
        int len;
        while ( (len = bf.getInputStream().read( buf )) > 0 ) {
            out.write( buf, 0, len );
        }
        bf.getInputStream().close();
    }
}
