package edu.illinois.ncsa.mmdb.web.server.webdav;

import java.util.Date;

import org.tupeloproject.kernel.Context;
import org.tupeloproject.rdf.Resource;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.SecurityManager;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.http11.auth.DigestResponse;

/**
 * Helper class to easily create a resource (file/folder). This will take care
 * of handling the name, id, date and security.
 * 
 * @author Rob Kooper
 * 
 */
public abstract class AbstractResource implements com.bradmcevoy.http.Resource, DigestResource, PropFindableResource
{
    private String          name;
    private Resource        uri;
    private Date            created;
    private Date            modified;
    private Context         context;
    private SecurityManager security;

    public AbstractResource( String name, Context context, SecurityManager security )
    {
        this( name, null, null, null, context, security );
    }

    public AbstractResource( String name, Resource uri, Context context, SecurityManager security )
    {
        this( name, uri, null, null, context, security );
    }

    public AbstractResource( String name, Resource uri, Date created, Context context, SecurityManager security )
    {
        this( name, uri, created, created, context, security );
    }

    public AbstractResource( String name, Resource uri, Date created, Date modified, Context context, SecurityManager security )
    {
        this.name = name;
        this.uri = uri;
        this.created = created;
        this.created = modified;
        this.context = context;
        this.security = security;
    }

    // ----------------------------------------------------------------------
    // Setters and Getters
    // ----------------------------------------------------------------------

    /**
     * @return the context
     */
    public Context getContext()
    {
        return context;
    }

    /**
     * @return the uri
     */
    public Resource getUri()
    {
        return uri;
    }

    /**
     * @param uri
     *            the uri to set
     */
    public void setUri( Resource uri )
    {
        this.uri = uri;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName( String name )
    {
        this.name = name;
    }

    /**
     * @return the security
     */
    public SecurityManager getSecurity()
    {
        return security;
    }

    // ----------------------------------------------------------------------
    // Resource
    // ----------------------------------------------------------------------
    @Override
    public String getRealm()
    {
        return security.getRealm();
    }

    @Override
    public Object authenticate( String user, String password )
    {
        return security.authenticate( user, password );
    }

    @Override
    public boolean authorise( Request request, Method method, Auth auth )
    {
        return security.authorise( request, method, auth, this );
    }

    @Override
    public String checkRedirect( Request request )
    {
        return null;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getUniqueId()
    {
        if ( uri != null ) {
            return uri.getString();
        }
        return null;
    }

    @Override
    public Date getModifiedDate()
    {
        return modified;
    }

    // ----------------------------------------------------------------------
    // DigestResource
    // ----------------------------------------------------------------------
    public Object authenticate( DigestResponse digestRequest )
    {
        return security.authenticate( digestRequest );
    }

    // ----------------------------------------------------------------------
    // PropFindableResource
    // ----------------------------------------------------------------------

    @Override
    public Date getCreateDate()
    {
        return created;
    }
}
