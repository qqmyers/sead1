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
import org.tupeloproject.util.Tuple;

import com.bradmcevoy.http.SecurityManager;

import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.CollectionBean;
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
    private static Log     log         = LogFactory.getLog( CollectionBeanResource.class );

    private static String  COLLECTIONS = "collections";

    private CollectionBean bean;

    public CollectionBeanResource( BeanSession beanSession, SecurityManager security )
    {
        super( COLLECTIONS, Cet.cet( COLLECTIONS ).getString(), beanSession, security );
        this.bean = null;
    }

    public CollectionBeanResource( CollectionBean bean, BeanSession beanSession, SecurityManager security )
    {
        super( bean.getLabel(), bean.getUri(), beanSession, security );
        this.bean = bean;
        if ( bean.getLabel() == null ) {
            name = bean.getTitle();
        }
    }

    // ----------------------------------------------------------------------
    // AbstractCollectionResource
    // ----------------------------------------------------------------------

    @Override
    public Map<String, AbstractResource> getResourceList()
    {
        Map<String, AbstractResource> result = new HashMap<String, AbstractResource>();
        CollectionBeanUtil cbu = new CollectionBeanUtil( TupeloStore.getInstance().getBeanSession() );

        if ( bean == null ) {
            try {
                for ( CollectionBean cb : cbu.getAll() ) {
                    AbstractResource r = new CollectionBeanResource( cb, beanSession, security );
                    result.put( cb.getUri(), r );
                }
            } catch ( Exception e ) {
                log.warn( "Could not get list of collections.", e );
            }

        } else {
            Unifier uf = new Unifier();
            uf.addPattern( "data", Rdf.TYPE, Cet.DATASET ); //$NON-NLS-1$
            uf.addPattern( "data", Dc.IS_REPLACED_BY, "replaced", true ); //$NON-NLS-1$ //$NON-NLS-2$
            uf.addPattern( org.tupeloproject.rdf.Resource.uriRef( bean.getUri() ), CollectionBeanUtil.DCTERMS_HAS_PART, "data" ); //$NON-NLS-1$
            uf.setColumnNames( "data", "replaced" ); //$NON-NLS-1$ //$NON-NLS-2$
            try {
                TupeloStore.getInstance().getContext().perform( uf );
            } catch ( OperatorException e ) {
                log.warn( "Could not get list of datasets.", e );
            }

            for ( Tuple<org.tupeloproject.rdf.Resource> row : uf.getResult() ) {
                if ( !Rdf.NIL.equals( row.get( 1 ) ) ) {
                    try {
                        AbstractResource r = new DatasetResource( TupeloStore.fetchDataset( row.get( 0 ) ), beanSession, security );
                        result.put( row.get( 0 ).getString(), r );
                    } catch ( OperatorException e ) {
                        log.warn( "Could not fetch bean.", e );
                    }
                }
            }
        }

        return result;
    }
}
