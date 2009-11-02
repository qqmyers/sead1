package edu.illinois.ncsa.bard.ui.query.tag;

import java.util.HashMap;
import java.util.Map;

import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Tags;
import org.tupeloproject.util.Table;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.bard.ui.BardFrame;
import edu.illinois.ncsa.bard.ui.VirtualBardGroup;
import edu.illinois.ncsa.bard.ui.query.FrameQuery;

public class GroupByTagQuery extends FrameQuery
{
    public GroupByTagQuery( BardFrame frame )
    {
        super( frame );
    }

    public void execute() throws OperatorException
    {
        Unifier unifier = createUnifier();
        frame.getContext().perform( unifier );

        Map<String,VirtualBardGroup> groups = new HashMap<String,VirtualBardGroup>();

        Table<Resource> result = unifier.getResult();
        for ( Tuple<Resource> tuple : result ) {
            Resource resource = tuple.get( 0 );
            String tag = resource.getString();
            VirtualBardGroup g = groups.get( tag );
            if ( g == null ) {
                g = new VirtualBardGroup();
                
                TagMemberQuery query = new TagMemberQuery( frame, g );
                query.setTag( tag );
                
                g.setQuery( query );
                g.setLabel( tag );                
                groups.put( tag, g );
            }
        }

        updateFrameData( groups.values() );
    }

    protected Unifier createUnifier()
    {
        Unifier u = new Unifier();
        u.addPattern( "subject", Tags.HAS_TAGGING_EVENT, "tevent" ); 
        u.addPattern( "tevent", Tags.HAS_TAG_OBJECT, "tag" ); 
        u.addPattern( "tag", Tags.HAS_TAG_TITLE, "title" ); 
        u.addColumnName( "title" );

        return u;
    }
}
