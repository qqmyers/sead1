package edu.illinois.ncsa.mmdb.web.server.webdav;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Tags;
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
public class TagRootResource extends AbstractCollectionResource
{
    private static Log log = LogFactory.getLog( TagRootResource.class );

    public TagRootResource( Context context, SecurityManager security )
    {
        super( "tags", context, security );
    }

    // ----------------------------------------------------------------------
    // AbstractCollectionResource
    // ----------------------------------------------------------------------

    @Override
    public Map<String, AbstractResource> getResourceList()
    {
        Map<String, AbstractResource> result = new HashMap<String, AbstractResource>();

        Unifier uf = new Unifier();
        uf.addPattern( "tevent", Rdf.TYPE, Tags.TAGGING_EVENT ); //$NON-NLS-1$
        uf.addPattern( "tevent", Tags.HAS_TAG_OBJECT, "tag" ); //$NON-NLS-1$ //$NON-NLS-2$
        uf.addPattern( "tag", Tags.HAS_TAG_TITLE, "title" ); //$NON-NLS-1$ //$NON-NLS-2$
        uf.addColumnName( "title" ); //$NON-NLS-1$
        try {
            getContext().perform( uf );
        } catch ( OperatorException e ) {
            log.warn( "Could not get list of tags.", e );
        }
        for ( Tuple<Resource> row : uf.getResult() ) {
            AbstractResource r = new TagBeanResource( row.get( 0 ).getString(), getContext(), getSecurity() );
            result.put( row.get( 0 ).getString(), r );
        }

        return result;
    }
}
