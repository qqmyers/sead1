package edu.illinois.ncsa.mmdb.web.server.webdav;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.tupeloproject.kernel.BeanSession;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;

/**
 * Helper class to easily create a collection (folder). This will take care of
 * handling the getChildren and child functions and makes sure there is no name
 * collision (by prepending the file/folder with a number).
 * 
 * @author Rob Kooper
 * 
 */
public abstract class AbstractCollectionResource extends AbstractResource implements CollectionResource
{
    protected Map<String, Resource> resourcemap = new HashMap<String, Resource>();

    public AbstractCollectionResource( String name, String id, BeanSession beanSession, SecurityManager security )
    {
        this( name, id, null, beanSession, security );
    }

    public AbstractCollectionResource( String name, String id, Date created, BeanSession beanSession, SecurityManager security )
    {
        super( name, id, created, beanSession, security );
    }

    // ----------------------------------------------------------------------
    // CollectionResource
    // ----------------------------------------------------------------------

    @Override
    public Resource child( String childName )
    {
        if ( resourcemap.size() == 0 ) {
            getChildren();
        }
        return resourcemap.get( childName );
    }

    @Override
    public List<? extends Resource> getChildren()
    {
        List<String> dups = new ArrayList<String>();
        Set<String> done = new HashSet<String>();

        resourcemap.clear();
        Map<String, AbstractResource> map = getResourceList();

        // look for dups
        for ( Entry<String, AbstractResource> entry1 : map.entrySet() ) {
            // skip if already processed (duplicate)
            if ( done.contains( entry1.getKey() ) ) {
                continue;
            }

            // search for duplicate names
            dups.clear();
            for ( Entry<String, AbstractResource> entry2 : map.entrySet() ) {
                if ( !entry1.getKey().equals( entry2.getKey() ) && entry1.getValue().getName().equals( entry2.getValue().getName() ) ) {
                    if ( !dups.contains( entry1.getKey() ) ) {
                        dups.add( entry1.getKey() );
                    }
                    if ( !dups.contains( entry2.getKey() ) ) {
                        dups.add( entry2.getKey() );
                    }
                }
            }

            // make sure all names are unique
            if ( dups.size() > 0 ) {
                Collections.sort( dups );
                int c = 1;
                for ( String d1 : dups ) {
                    AbstractResource r = map.get( d1 );
                    r.setName( String.format( "%03d %s", c++, r.getName() ) ); //$NON-NLS-1$
                    resourcemap.put( r.getName(), r );
                    done.add( d1 );
                }
            } else {
                resourcemap.put( entry1.getValue().getName(), entry1.getValue() );
                done.add( entry1.getKey() );
            }
        }

        return new ArrayList<Resource>( resourcemap.values() );
    }

    /**
     * Return a collection of resources that should be shown to the user. This
     * is used by getChildren to get the initial list of resources after which
     * the names of resources will be made unique. The key in the map is used to
     * sort the collection of resources to generate names in case of collision
     * of names. This key has to be consistent between different calls (for
     * example use the URI of the bean).
     * 
     * @return collection of resources to show to the user.
     */
    public abstract Map<String, AbstractResource> getResourceList();
}
