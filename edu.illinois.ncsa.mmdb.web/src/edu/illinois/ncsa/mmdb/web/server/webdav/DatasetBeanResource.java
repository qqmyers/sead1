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
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import org.tupeloproject.kernel.BlobFetcher;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.rdf.Resource;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.SecurityManager;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

/**
 * Wrapper around the DatasetBean. This will show information about the dataset
 * and allow to download it.
 * 
 * @author Rob Kooper
 * 
 */
public class DatasetBeanResource extends AbstractResource implements GetableResource
{
    private long     size;
    private Resource uri;
    private String   mimetype;

    public DatasetBeanResource( String name, Resource uri, long size, Date date, String mimetype, Context context, SecurityManager security )
    {
        super( name, uri, date, context, security );
        this.uri = uri;
        this.size = size;
        this.mimetype = mimetype;
    }

    // ----------------------------------------------------------------------
    // Resource
    // ----------------------------------------------------------------------

    @Override
    public String getName()
    {
        // FIXME add extention if wrong one.
        String name = super.getName();
        if ( (mimetype.equals( "image/jpeg" ) || mimetype.equals( "image/jpg" )) && !name.endsWith( ".jpg" ) ) {
            name = name + ".jpg";
        }
        return name;
    }

    // ----------------------------------------------------------------------
    // GetableResource
    // ----------------------------------------------------------------------

    @Override
    public Long getContentLength()
    {
        if ( size <= 0 ) {
            return null;
        }
        return size;
    }

    @Override
    public String getContentType( String accepts )
    {
        // FIXME should really use accepts
        return mimetype;
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
        bf.setSubject( uri );
        try {
            getContext().perform( bf );
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
