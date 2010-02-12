package edu.illinois.ncsa.mmdb.web.server.webdav;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Tags;
import org.tupeloproject.util.Tuple;

import com.bradmcevoy.http.SecurityManager;

import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;

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
    private static Log    log  = LogFactory.getLog( TagBeanResource.class );

    private static String TAGS = "tags";

    private String        tag;

    public TagBeanResource( BeanSession beanSession, SecurityManager security )
    {
        super( TAGS, Cet.cet( "tags" ).getString(), beanSession, security ); //$NON-NLS-1$
        this.tag = null;
    }

    public TagBeanResource( String tag, BeanSession beanSession, SecurityManager security )
    {
        super( tag, Cet.cet( "tags#" + tag ).getString(), beanSession, security ); //$NON-NLS-1$
        this.tag = tag;
    }

    // ----------------------------------------------------------------------
    // AbstractCollectionResource
    // ----------------------------------------------------------------------

    @Override
    public Map<String, AbstractResource> getResourceList()
    {
        Map<String, AbstractResource> result = new HashMap<String, AbstractResource>();

        if ( tag == null ) {
            Unifier uf = new Unifier();
            uf.addPattern( "tevent", Rdf.TYPE, Tags.TAGGING_EVENT ); //$NON-NLS-1$
            uf.addPattern( "tevent", Tags.HAS_TAG_OBJECT, "tag" ); //$NON-NLS-1$ //$NON-NLS-2$
            uf.addPattern( "tag", Tags.HAS_TAG_TITLE, "title" ); //$NON-NLS-1$ //$NON-NLS-2$
            uf.addColumnName( "title" ); //$NON-NLS-1$
            try {
                beanSession.getContext().perform( uf );
            } catch ( OperatorException e ) {
                log.warn( "Could not get list of tags.", e );
            }
            for ( Tuple<org.tupeloproject.rdf.Resource> row : uf.getResult() ) {
                AbstractResource r = new TagBeanResource( row.get( 0 ).getString(), beanSession, security );
                result.put( row.get( 0 ).getString(), r );
            }

        } else {
            Unifier uf = new Unifier();
            uf.addPattern( "data", Rdf.TYPE, Cet.DATASET ); //$NON-NLS-1$
            uf.addPattern( "data", Tags.HAS_TAGGING_EVENT, "tevent" ); //$NON-NLS-1$ //$NON-NLS-2$
            uf.addPattern( "data", Dc.IS_REPLACED_BY, "replaced", true ); //$NON-NLS-1$ //$NON-NLS-2$
            uf.addPattern( "tevent", Rdf.TYPE, Tags.TAGGING_EVENT ); //$NON-NLS-1$
            uf.addPattern( "tevent", Tags.HAS_TAG_OBJECT, "tag" ); //$NON-NLS-1$ //$NON-NLS-2$
            uf.addPattern( "tag", Tags.HAS_TAG_TITLE, org.tupeloproject.rdf.Resource.literal( tag ) ); //$NON-NLS-1$
            uf.setColumnNames( "data", "replaced" ); //$NON-NLS-1$ //$NON-NLS-2$
            try {
                beanSession.getContext().perform( uf );
            } catch ( OperatorException e ) {
                log.warn( "Could not get list of datasets.", e );
            }
            DatasetBeanUtil dbu = new DatasetBeanUtil( beanSession );
            for ( Tuple<org.tupeloproject.rdf.Resource> row : uf.getResult() ) {
                if ( !Rdf.NIL.equals( row.get( 1 ) ) ) {
                    try {
                        AbstractResource r = new DatasetResource( dbu.get( row.get( 0 ) ), beanSession, security );
                        result.put( row.get( 0 ).getString(), r );
                    } catch ( OperatorException e ) {
                        log.warn( "Could not fetch dataset.", e );
                    }
                }
            }
        }

        return result;
    }
}
