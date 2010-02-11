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
import org.tupeloproject.rdf.terms.Foaf;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Tuple;

import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;

/**
 * Wrapper around the CollectionBean. This will show a folder called
 * 'people' with in there all people, or a folder with the name of the
 * person with in there all the datasets created by this user.
 * 
 * @author Rob Kooper
 * 
 */
public class PersonBeanResource extends AbstractCollectionResource
{
    private static Log    log    = LogFactory.getLog( PersonBeanResource.class );

    private static String PEOPLE = "people";

    public PersonBeanResource( BeanSession beanSession, SecurityManager security )
    {
        super( PEOPLE, Cet.cet( "people" ).getString(), beanSession, security ); //$NON-NLS-1$
    }

    public PersonBeanResource( String name, String id, BeanSession beanSession, SecurityManager security )
    {
        super( name, id, beanSession, security ); //$NON-NLS-1$
    }

    // ----------------------------------------------------------------------
    // AbstractCollectionResource
    // ----------------------------------------------------------------------

    @Override
    public Map<String, AbstractResource> getResourceList()
    {
        Map<String, AbstractResource> result = new HashMap<String, AbstractResource>();

        if ( name.equals( PEOPLE ) ) {
            Unifier uf = new Unifier();
            uf.addPattern( "person", Rdf.TYPE, Foaf.PERSON ); //$NON-NLS-1$
            uf.addPattern( "person", Foaf.NAME, "name" ); //$NON-NLS-1$ //$NON-NLS-2$
            uf.setColumnNames( "person", "name" ); //$NON-NLS-1$ //$NON-NLS-2$
            try {
                beanSession.getContext().perform( uf );
            } catch ( OperatorException e ) {
                log.warn( "Could not get list of names.", e );
            }
            for ( Tuple<org.tupeloproject.rdf.Resource> row : uf.getResult() ) {
                AbstractResource r = new PersonBeanResource( row.get( 1 ).getString(), row.get( 0 ).getString(), beanSession, security );
                result.put( row.get( 0 ).getString(), r );
            }

        } else {
            Unifier uf = new Unifier();
            uf.addPattern( "data", Rdf.TYPE, Cet.DATASET ); //$NON-NLS-1$
            uf.addPattern( "data", Dc.CREATOR, org.tupeloproject.rdf.Resource.uriRef( id ) ); //$NON-NLS-1$
            uf.addPattern( "data", Dc.IS_REPLACED_BY, "replaced", true ); //$NON-NLS-1$ //$NON-NLS-2$
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
