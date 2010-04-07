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

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.util.Iso8601;
import org.tupeloproject.util.Tuple;

import com.bradmcevoy.http.SecurityManager;

import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;

/**
 * Wrapper around the CollectionBean. This will show a folder called
 * 'collections' with in there all collections, or a folder with the name of the
 * collection with in there all the datasets.
 * 
 * @author Rob Kooper
 * 
 */
public class CollectionRootResource extends AbstractCollectionResource
{
    private static Log log = LogFactory.getLog( CollectionRootResource.class );

    public CollectionRootResource( Context context, SecurityManager security )
    {
        super( "collections", context, security );
    }

    // ----------------------------------------------------------------------
    // AbstractCollectionResource
    // ----------------------------------------------------------------------

    @Override
    public Map<String, AbstractResource> getResourceList()
    {
        Map<String, AbstractResource> result = new HashMap<String, AbstractResource>();

        Unifier uf = new Unifier();
        uf.addPattern( "collection", Rdf.TYPE, CollectionBeanUtil.COLLECTION_TYPE );
        uf.addColumnName( "collection" ); //$NON-NLS-1$
        uf.addPattern( "collection", Dc.TITLE, "title", true ); //$NON-NLS-1$ //$NON-NLS-2$
        uf.addColumnName( "title" ); //$NON-NLS-1$
        uf.addPattern( "collection", Rdfs.LABEL, "label", true ); //$NON-NLS-1$ //$NON-NLS-2$
        uf.addColumnName( "label" ); //$NON-NLS-1$
        uf.addPattern( "collection", DcTerms.DATE_CREATED, "created", true ); //$NON-NLS-1$ //$NON-NLS-2$
        uf.addColumnName( "created" ); //$NON-NLS-1$
        uf.addPattern( "collection", DcTerms.DATE_MODIFIED, "modified", true ); //$NON-NLS-1$ //$NON-NLS-2$
        uf.addColumnName( "modified" ); //$NON-NLS-1$

        try {
            getContext().perform( uf );
        } catch ( OperatorException e ) {
            log.warn( "Could not get list of collections.", e );
        }

        for ( Tuple<Resource> row : uf.getResult() ) {
            String label;
            if ( row.get( 2 ) != null ) {
                label = row.get( 2 ).toString();
            } else {
                label = row.get( 1 ).toString();
            }
            Date created = null;
            if ( row.get( 3 ) != null ) {
                try {
                    created = Iso8601.string2Date( row.get( 3 ).getString() ).getTime();
                } catch ( ParseException e ) {
                    log.info( "Could not parse date.", e );
                    created = null;
                }
            }
            Date modified = null;
            if ( row.get( 4 ) != null ) {
                try {
                    modified = Iso8601.string2Date( row.get( 4 ).getString() ).getTime();
                } catch ( ParseException e ) {
                    log.info( "Could not parse date.", e );
                    modified = null;
                }
            }
            AbstractResource r = new CollectionBeanResource( label, row.get( 0 ), created, modified, getContext(), getSecurity() );
            result.put( row.get( 0 ).getString(), r );
        }

        return result;
    }
}
