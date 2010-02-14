package edu.illinois.ncsa.mmdb.web.server.webdav;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Foaf;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Tuple;

import com.bradmcevoy.http.SecurityManager;

/**
 * Wrapper around the CollectionBean. This will show a folder called 'people'
 * with in there all people, or a folder with the name of the person with in
 * there all the datasets created by this user.
 * 
 * @author Rob Kooper
 * 
 */
public class PersonRootResource extends AbstractCollectionResource
{
    private static Log log = LogFactory.getLog( PersonRootResource.class );

    public PersonRootResource( Context context, SecurityManager security )
    {
        super( "people", context, security );
    }

    // ----------------------------------------------------------------------
    // AbstractCollectionResource
    // ----------------------------------------------------------------------

    @Override
    public Map<String, AbstractResource> getResourceList()
    {
        Map<String, AbstractResource> result = new HashMap<String, AbstractResource>();

        Unifier uf = new Unifier();
        uf.addPattern( "person", Rdf.TYPE, Foaf.PERSON ); //$NON-NLS-1$
        uf.addPattern( "person", Foaf.NAME, "name" ); //$NON-NLS-1$ //$NON-NLS-2$
        uf.setColumnNames( "person", "name" ); //$NON-NLS-1$ //$NON-NLS-2$
        try {
            getContext().perform( uf );
        } catch ( OperatorException e ) {
            log.warn( "Could not get list of names.", e );
        }
        for ( Tuple<Resource> row : uf.getResult() ) {
            AbstractResource r = new PersonBeanResource( row.get( 1 ).getString(), row.get( 0 ), getContext(), getSecurity() );
            result.put( row.get( 0 ).getString(), r );
        }

        return result;
    }
}
