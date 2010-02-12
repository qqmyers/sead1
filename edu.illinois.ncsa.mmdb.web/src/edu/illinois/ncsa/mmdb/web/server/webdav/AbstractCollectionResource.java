package edu.illinois.ncsa.mmdb.web.server.webdav;

import java.io.IOException;
import java.io.OutputStream;
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

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.MiltonServlet;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.SecurityManager;
import com.bradmcevoy.http.XmlWriter;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

/**
 * Helper class to easily create a collection (folder). This will take care of
 * handling the getChildren and child functions and makes sure there is no name
 * collision (by prepending the file/folder with a number).
 * 
 * @author Rob Kooper
 * 
 */
public abstract class AbstractCollectionResource extends AbstractResource implements CollectionResource, GetableResource
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
    // GetableResource
    // ----------------------------------------------------------------------

    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException
    {
        String path = MiltonServlet.request().getRequestURL().toString();
        if (!path.endsWith( "/" )) {
            path = path + "/";
        }
        XmlWriter w = new XmlWriter( out );
        w.open( "html" );
        w.open( "body" );
        w.begin( "h1" ).open().writeText( this.getName() ).close();
        w.open( "table" );
        for ( Resource r : getChildren() ) {
            w.open( "tr" );

            String url = path + r.getName();
            
            w.open( "td" );
            w.begin( "a" ).writeAtt( "href", url ).open().writeText( r.getName() ).close();
            w.close( "td" );

            w.begin( "td" ).open().writeText( r.getModifiedDate() + "" ).close();
            w.close( "tr" );
        }
        w.close( "table" );
        w.close( "body" );
        w.close( "html" );
        w.flush();
    }

    public Long getMaxAgeSeconds( Auth auth )
    {
        return null;
    }

    public String getContentType( String accepts )
    {
        return "text/html";
    }

    public Long getContentLength()
    {
        return null;
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
