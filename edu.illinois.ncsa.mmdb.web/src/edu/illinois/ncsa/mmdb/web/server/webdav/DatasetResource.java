package edu.illinois.ncsa.mmdb.web.server.webdav;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.BlobFetcher;
import org.tupeloproject.kernel.OperatorException;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * Wrapper around the DatasetBean. This will show information about the dataset
 * and allow to download it.
 * 
 * @author Rob Kooper
 * 
 */
public class DatasetResource extends AbstractResource implements GetableResource
{
    private DatasetBean bean;

    public DatasetResource( DatasetBean bean, BeanSession beanSession, SecurityManager security )
    {
        super( bean.getLabel(), bean.getUri(), bean.getDate(), beanSession, security );
        this.bean = bean;

        if ( bean.getLabel() == null ) {
            this.name = bean.getTitle();
        }
    }

    // ----------------------------------------------------------------------
    // GetableResource
    // ----------------------------------------------------------------------
    @Override
    public Long getContentLength()
    {
        long size = bean.getSize();
        if ( size <= 0 ) {
            return null;
        }
        return size;
    }

    @Override
    public String getContentType( String accepts )
    {
        // FIXME should really use accepts
        return bean.getMimeType();
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
        bf.setSubject( org.tupeloproject.rdf.Resource.uriRef( bean.getUri() ) );
        try {
            TupeloStore.getInstance().getContext().perform( bf );
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
