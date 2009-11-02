package edu.illinois.ncsa.bard.ui.query.creator;

import java.util.HashMap;
import java.util.Map;

import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.Foaf;
import org.tupeloproject.util.Table;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.bard.ui.BardFrame;
import edu.illinois.ncsa.bard.ui.VirtualBardGroup;
import edu.illinois.ncsa.bard.ui.query.FrameQuery;

public class GroupByCreatorQuery extends FrameQuery
{
    public GroupByCreatorQuery( BardFrame frame )
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
            String name = resource.getString();
            VirtualBardGroup g = groups.get( name );
            if ( g == null ) {
                g = new VirtualBardGroup();                
                groups.put( name, g );
            }
        }

        updateFrameData( groups.values() );
    }

    protected Unifier createUnifier()
    {
        Unifier u = new Unifier();
        u.addPattern( "beanSubject", Dc.CREATOR , "creator" ); 
        u.addPattern( "creator", Foaf.NAME, "name" ); 
        u.addColumnName( "name" );

        return u;
    }
}
