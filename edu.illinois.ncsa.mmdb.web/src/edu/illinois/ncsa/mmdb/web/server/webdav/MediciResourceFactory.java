package edu.illinois.ncsa.mmdb.web.server.webdav;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.Context;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.MiltonServlet;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.SecurityManager;

import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

/**
 * This is base of Medici WebDav. This defines the root level folders
 * (collections, tags and people) and has the magic to find the right resource
 * given a URL.
 * 
 * @author Rob Kooper
 */
public class MediciResourceFactory implements ResourceFactory
{
    private static Log     log = LogFactory.getLog( MediciResourceFactory.class );

    private FolderResource root;

    public MediciResourceFactory()
    {
        Context context = TupeloStore.getInstance().getContext();
        SecurityManager security = new MediciSecurityManager( context );

        root = new FolderResource( "/", security ); //$NON-NLS-1$
        try {
            root.add( new CollectionRootResource( context, security ) );
        } catch ( IOException e ) {
            log.warn( "Could not add collections.", e );
        }
        try {
            root.add( new TagRootResource( context, security ) );
        } catch ( IOException e ) {
            log.warn( "Could not add tags.", e );
        }
        try {
            root.add( new PersonRootResource( context, security ) );
        } catch ( IOException e ) {
            log.warn( "Could not add people.", e );
        }
    }

    @Override
    public Resource getResource( String host, String path )
    {
        // get path minus servlet
        path = MiltonServlet.request().getPathInfo();
        if ( path == null ) {
            path = "";
        }

        // remove leading slash
        if ( path.startsWith( "/" ) ) { //$NON-NLS-1$
            path = path.substring( 1 );
        }

        // special case for root
        if ( path.equals( "" ) || path.equals( "/" ) ) { //$NON-NLS-1$ //$NON-NLS-2$
            return root;
        }

        // find the path and return item
        Resource found = root;
        for ( String part : path.split( "/" ) ) { //$NON-NLS-1$
            if ( found instanceof CollectionResource ) {
                found = ((CollectionResource) found).child( part );
                if ( found == null ) {
                    log.debug( "Did not find " + path );
                    return null;
                }
            } else {
                log.debug( "Found non collectionresource " + path );
                return null;
            }
        }
        return found;
    }
}
