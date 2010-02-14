package edu.illinois.ncsa.mmdb.web.server.webdav;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bradmcevoy.http.SecurityManager;

/**
 * Helper class to create a folder structure. This can take any resource as
 * child and will show it in the folder.
 * 
 * @author Rob Kooper
 * 
 */
public class FolderResource extends AbstractCollectionResource
{
    private List<AbstractResource> children;

    public FolderResource( String folder, SecurityManager security )
    {
        super( folder, null, security ); //$NON-NLS-1$
        this.children = new ArrayList<AbstractResource>();
    }

    public void add( AbstractResource child ) throws IOException
    {
        for ( AbstractResource r : children ) {
            if ( r.getName().equals( child.getName() ) ) {
                throw (new IOException( "Name has to be unique" ));
            }
        }
        children.add( child );
    }

    // ----------------------------------------------------------------------
    // AbstractCollectionResource
    // ----------------------------------------------------------------------

    @Override
    public Map<String, AbstractResource> getResourceList()
    {
        Map<String, AbstractResource> map = new HashMap<String, AbstractResource>();
        for ( AbstractResource r : children ) {
            map.put( r.getName(), r );
        }
        return map;
    }
}
