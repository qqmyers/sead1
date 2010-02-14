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
import org.tupeloproject.rdf.terms.Files;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.rdf.terms.Tags;
import org.tupeloproject.util.Iso8601;
import org.tupeloproject.util.Tuple;

import com.bradmcevoy.http.SecurityManager;

/**
 * Wrapper around tags. This will show a folder called 'tags' with in there all
 * tags, or a folder with the name of the tag with in there all the datasets
 * tagged with this specific tag.
 * 
 * @author Rob Kooper
 * 
 */
public class TagBeanResource extends AbstractCollectionResource
{
    private static Log log = LogFactory.getLog( TagBeanResource.class );

    public TagBeanResource( String tag, Context context, SecurityManager security )
    {
        super( tag, context, security );
    }

    // ----------------------------------------------------------------------
    // AbstractCollectionResource
    // ----------------------------------------------------------------------

    @Override
    public Map<String, AbstractResource> getResourceList()
    {
        Map<String, AbstractResource> result = new HashMap<String, AbstractResource>();

        Unifier uf = new Unifier();
        uf.addPattern( "data", Rdf.TYPE, Cet.DATASET ); //$NON-NLS-1$
        uf.addColumnName( "data" ); //$NON-NLS-1$
        uf.addPattern( "data", Tags.HAS_TAGGING_EVENT, "tevent" ); //$NON-NLS-1$ //$NON-NLS-2$
        uf.addPattern( "tevent", Rdf.TYPE, Tags.TAGGING_EVENT ); //$NON-NLS-1$
        uf.addPattern( "tevent", Tags.HAS_TAG_OBJECT, "tag" ); //$NON-NLS-1$ //$NON-NLS-2$
        uf.addPattern( "tag", Tags.HAS_TAG_TITLE, Resource.literal( getName() ) ); //$NON-NLS-1$
        uf.addPattern( "data", Dc.IS_REPLACED_BY, "replaced", true ); //$NON-NLS-1$ //$NON-NLS-2$
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
                AbstractResource r = new DatasetBeanResource( label, row.get( 0 ), size, date, format, getContext(), getSecurity() );
                result.put( row.get( 0 ).getString(), r );
            }
        }

        return result;
    }
}
