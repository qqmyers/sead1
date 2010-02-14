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
