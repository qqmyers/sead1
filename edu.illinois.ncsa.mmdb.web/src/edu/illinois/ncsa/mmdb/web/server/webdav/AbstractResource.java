package edu.illinois.ncsa.mmdb.web.server.webdav;

import java.util.Date;

import org.tupeloproject.kernel.BeanSession;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.http11.auth.DigestResponse;

/**
 * Helper class to easily create a resource (file/folder). This will take care of
 * handling the name, id, date and security.
 * 
 * @author Rob Kooper
 * 
 */
public abstract class AbstractResource implements Resource, DigestResource, PropFindableResource
{
    protected String          name;
    protected String          id;
    protected Date            created;
    protected BeanSession     beanSession;
    protected SecurityManager security;

    public AbstractResource( String name, String id, BeanSession beanSession, SecurityManager security )
    {
        this( name, id, null, beanSession, security );
    }

    public AbstractResource( String name, String id, Date created, BeanSession beanSession, SecurityManager security )
    {
        this.name = name;
        this.id = id;
        this.created = created;
        this.beanSession = beanSession;
        this.security = security;
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
        return security.authorise( request, method, auth );
    }

    @Override
    public String checkRedirect( Request request )
    {
        return null;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    @Override
    public String getName()
    {
        return name;
    }

    public void setUniqueId( String id )
    {
        this.id = id;
    }

    @Override
    public String getUniqueId()
    {
        return id;
    }

    @Override
    public Date getModifiedDate()
    {
        return created;
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
