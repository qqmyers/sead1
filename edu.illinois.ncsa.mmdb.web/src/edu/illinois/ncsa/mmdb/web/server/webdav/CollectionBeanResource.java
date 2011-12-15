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
import java.io.InputStream;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Files;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.util.Iso8601;
import org.tupeloproject.util.Tuple;

import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.SecurityManager;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

import edu.uiuc.ncsa.cet.bean.tupelo.CETBeans;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;

/**
 * Wrapper around the CollectionBean. This will show a folder called
 * 'collections' with in there all collections, or a folder with the name of the
 * collection with in there all the datasets.
 * 
 * @author Rob Kooper
 * 
 */
public class CollectionBeanResource extends AbstractCollectionResource implements PutableResource
{
    private static Log log = LogFactory.getLog(CollectionBeanResource.class);

    public CollectionBeanResource(String name, Resource uri, Context context, SecurityManager security)
    {
        super(name, uri, context, security);
    }

    public CollectionBeanResource(String name, Resource uri, Date created, Context context, SecurityManager security)
    {
        super(name, uri, created, context, security);
    }

    public CollectionBeanResource(String name, Resource uri, Date created, Date modified, Context context, SecurityManager security)
    {
        super(name, uri, created, modified, context, security);
    }

    // ----------------------------------------------------------------------
    // AbstractCollectionResource
    // ----------------------------------------------------------------------

    @Override
    public Map<String, AbstractResource> getResourceList()
    {
        Map<String, AbstractResource> result = new HashMap<String, AbstractResource>();

        Unifier uf = new Unifier();
        uf.addPattern(getUri(), CollectionBeanUtil.DCTERMS_HAS_PART, "data"); //$NON-NLS-1$
        uf.addPattern("data", Rdf.TYPE, Cet.DATASET); //$NON-NLS-1$
        uf.addColumnName("data"); //$NON-NLS-1$
        uf.addPattern("data", DcTerms.IS_REPLACED_BY, "replaced", true); //$NON-NLS-1$ //$NON-NLS-2$
        uf.addColumnName("replaced"); //$NON-NLS-1$
        uf.addPattern("data", Dc.DATE, "date"); //$NON-NLS-1$ //$NON-NLS-2$
        uf.addColumnName("date"); //$NON-NLS-1$
        uf.addPattern("data", Files.LENGTH, "size", true); //$NON-NLS-1$ //$NON-NLS-2$
        uf.addColumnName("size"); //$NON-NLS-1$
        uf.addPattern("data", Dc.TITLE, "title", true); //$NON-NLS-1$ //$NON-NLS-2$
        uf.addColumnName("title"); //$NON-NLS-1$
        uf.addPattern("data", Rdfs.LABEL, "label", true); //$NON-NLS-1$ //$NON-NLS-2$
        uf.addColumnName("label"); //$NON-NLS-1$
        uf.addPattern("data", Dc.FORMAT, "format"); //$NON-NLS-1$ //$NON-NLS-2$
        uf.addColumnName("format"); //$NON-NLS-1$
        try {
            getContext().perform(uf);
        } catch (OperatorException e) {
            log.warn("Could not get list of datasets.", e);
        }
        for (Tuple<Resource> row : uf.getResult() ) {
            if (!Rdf.NIL.equals(row.get(1))) {
                String label;
                if (row.get(5) != null) {
                    label = row.get(5).toString();
                } else {
                    label = row.get(4).toString();
                }
                Date date;
                try {
                    date = Iso8601.string2Date(row.get(2).getString()).getTime();
                } catch (ParseException e) {
                    log.info("Could not parse date.", e);
                    date = null;
                }
                long size = -1;
                if (row.get(3) != null) {
                    size = Long.parseLong(row.get(3).getString());
                }
                String format = row.get(6).getString();
                AbstractResource r = new DeletableDatasetBeanResource(label, row.get(0), size, date, format, getContext(), getSecurity());
                result.put(row.get(0).getString(), r);
            }
        }

        return result;
    }

    // ----------------------------------------------------------------------
    // PutableResource
    // ----------------------------------------------------------------------
    @Override
    public com.bradmcevoy.http.Resource createNew(String newName, InputStream stream, Long length, String contentType) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
        com.bradmcevoy.http.Resource result = upload(newName, stream, length, contentType);
        if (result == null) {
            return null;
        }

        try {
            BeanSession bs = CETBeans.createBeanSession(getContext());
            Collection<Resource> subjects = new HashSet<Resource>();
            subjects.add(Resource.uriRef(result.getUniqueId()));

            CollectionBeanUtil cbu = new CollectionBeanUtil(bs);
            cbu.addToCollection(cbu.get(getUniqueId()), subjects);

            return result;
        } catch (OperatorException e) {
            throw (new BadRequestException(result));
        } catch (Exception e) {
            throw (new BadRequestException(result));
        }
    }

    class DeletableDatasetBeanResource extends DatasetBeanResource implements DeletableResource
    {
        public DeletableDatasetBeanResource(String name, Resource uri, long size, Date date, String mimetype, Context context, SecurityManager security)
        {
            super(name, uri, size, date, mimetype, context, security);
        }

        // ----------------------------------------------------------------------
        // DeletableResource
        // ----------------------------------------------------------------------

        public void delete()
        {
            try {
                getContext().removeTriple(CollectionBeanResource.this.getUri(), DcTerms.HAS_PART, getUri());
            } catch (OperatorException e) {
                log.warn("Could not remove tag.", e);
            }
        }
    }

}
