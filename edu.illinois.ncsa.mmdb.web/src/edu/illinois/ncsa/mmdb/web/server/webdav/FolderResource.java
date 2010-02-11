package edu.illinois.ncsa.mmdb.web.server.webdav;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.tupeloproject.rdf.terms.Cet;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;

/**
 * Helper class to create a folder structure. This can take any resource as
 * child and will show it in the folder.
 * 
 * @author Rob Kooper
 * 
 */
public class FolderResource extends AbstractResource implements CollectionResource
{
    private List<Resource> children;

    public FolderResource( String folder, SecurityManager security )
    {
        super( folder, Cet.cet( "folders#" + folder ).getString(), null, security ); //$NON-NLS-1$
        this.children = new ArrayList<Resource>();
    }

    public void add( Resource child ) throws IOException
    {
        for ( Resource r : children ) {
            if ( r.getName().equals( child.getName() ) ) {
                throw (new IOException( "Name has to be unique" ));
            }
        }
        children.add( child );
    }

    // ----------------------------------------------------------------------
    // CollectionResource
    // ----------------------------------------------------------------------

    @Override
    public Resource child( String childName )
    {
        if ( childName == null ) {
            return null;
        }
        for ( Resource r : children ) {
            if ( childName.equals( r.getName() ) ) {
                return r;
            }
        }
        return null;
    }

    @Override
    public List<? extends Resource> getChildren()
    {
        return children;
    }
}
