/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010, NCSA.  All rights reserved.
 *
 * Developed by:
 * Cyberenvironments and Technologies (CET)
 * http://cet.ncsa.illinois.edu/
 *
 * National Center for Supercomputing Applications (NCSA)
 * http://www.ncsa.illinois.edu/
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the names of CET, University of Illinois/NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *******************************************************************************/
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
        if ( modified == null ) {
            return created;
        }
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
