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
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Files;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.util.Iso8601;
import org.tupeloproject.util.Tuple;

import com.bradmcevoy.http.DeletableResource;
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
public class CollectionBeanResource extends AbstractCollectionResource
{
    private static Log log = LogFactory.getLog( CollectionBeanResource.class );

    public CollectionBeanResource( String name, Resource uri, Context context, SecurityManager security )
    {
        super( name, uri, context, security );
    }

    public CollectionBeanResource( String name, Resource uri, Date created, Context context, SecurityManager security )
    {
        super( name, uri, created, context, security );
    }

    public CollectionBeanResource( String name, Resource uri, Date created, Date modified, Context context, SecurityManager security )
    {
        super( name, uri, created, modified, context, security );
    }

    // ----------------------------------------------------------------------
    // AbstractCollectionResource
    // ----------------------------------------------------------------------

    @Override
    public Map<String, AbstractResource> getResourceList()
    {
        Map<String, AbstractResource> result = new HashMap<String, AbstractResource>();

        Unifier uf = new Unifier();
        uf.addPattern( getUri(), CollectionBeanUtil.DCTERMS_HAS_PART, "data" ); //$NON-NLS-1$
        uf.addPattern( "data", Rdf.TYPE, Cet.DATASET ); //$NON-NLS-1$
        uf.addColumnName( "data" ); //$NON-NLS-1$
        uf.addPattern( "data", DcTerms.IS_REPLACED_BY, "replaced", true ); //$NON-NLS-1$ //$NON-NLS-2$
        uf.addColumnName( "replaced" ); //$NON-NLS-1$
        uf.addPattern( "data", Dc.DATE, "date" ); //$NON-NLS-1$ //$NON-NLS-2$
        uf.addColumnName( "date" ); //$NON-NLS-1$
        uf.addPattern( "data", Files.LENGTH, "size", true ); //$NON-NLS-1$ //$NON-NLS-2$
        uf.addColumnName( "size" ); //$NON-NLS-1$
        uf.addPattern( "data", Dc.TITLE, "title", true ); //$NON-NLS-1$ //$NON-NLS-2$
        uf.addColumnName( "title" ); //$NON-NLS-1$
        uf.addPattern( "data", Rdfs.LABEL, "label", true ); //$NON-NLS-1$ //$NON-NLS-2$
        uf.addColumnName( "label" ); //$NON-NLS-1$
        uf.addPattern( "data", Dc.FORMAT, "format" ); //$NON-NLS-1$ //$NON-NLS-2$
        uf.addColumnName( "format" ); //$NON-NLS-1$
        try {
            getContext().perform( uf );
        } catch ( OperatorException e ) {
            log.warn( "Could not get list of datasets.", e );
        }
        for ( Tuple<Resource> row : uf.getResult() ) {
            if ( !Rdf.NIL.equals( row.get( 1 ) ) ) {
                String label;
                if ( row.get( 5 ) != null ) {
                    label = row.get( 5 ).toString();
                } else {
                    label = row.get( 4 ).toString();
                }
                Date date;
                try {
                    date = Iso8601.string2Date( row.get( 2 ).getString() ).getTime();
                } catch ( ParseException e ) {
                    log.info( "Could not parse date.", e );
                    date = null;
                }
                long size = -1;
                if ( row.get( 3 ) != null ) {
                    size = Long.parseLong( row.get( 3 ).getString() );
                }
                String format = row.get( 6 ).getString();
                AbstractResource r = new DeletableDatasetBeanResource( label, row.get( 0 ), size, date, format, getContext(), getSecurity() );
                result.put( row.get( 0 ).getString(), r );
            }
        }

        return result;
    }

    class DeletableDatasetBeanResource extends DatasetBeanResource implements DeletableResource
    {
        public DeletableDatasetBeanResource( String name, Resource uri, long size, Date date, String mimetype, Context context, SecurityManager security )
        {
            super( name, uri, size, date, mimetype, context, security );
        }

        // ----------------------------------------------------------------------
        // DeletableResource
        // ----------------------------------------------------------------------

        public void delete()
        {
            try {
                getContext().removeTriple( CollectionBeanResource.this.getUri(), DcTerms.HAS_PART, getUri() );
            } catch ( OperatorException e ) {
                log.warn( "Could not remove tag.", e );
            }
        }
    }

}
