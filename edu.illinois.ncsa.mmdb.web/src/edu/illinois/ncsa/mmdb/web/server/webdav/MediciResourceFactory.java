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
        SecurityManager security = new MediciSecurityManager( context, false );

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
        String servlet = MiltonServlet.request().getContextPath() + MiltonServlet.request().getServletPath() + "/?";
        path = path.replaceFirst( servlet, "" );

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
