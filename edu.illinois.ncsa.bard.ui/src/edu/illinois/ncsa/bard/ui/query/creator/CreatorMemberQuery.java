package edu.illinois.ncsa.bard.ui.query.creator;

import java.util.ArrayList;
import java.util.List;

import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.Foaf;
import org.tupeloproject.util.Table;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.bard.ui.BardFrame;
import edu.illinois.ncsa.bard.ui.VirtualBardGroup;
import edu.illinois.ncsa.bard.ui.query.MemberQuery;

public class CreatorMemberQuery extends MemberQuery
{
    protected String name;

    public CreatorMemberQuery( BardFrame frame, VirtualBardGroup group )
    {
        super( frame, group );
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public void execute() throws OperatorException
    {
        Unifier unifier = createUnifier();
        frame.getContext().perform( unifier );

        List<Resource> resources = new ArrayList<Resource>();
        
        Table<Resource> result = unifier.getResult();
        for ( Tuple<Resource> tuple : result ) {
            Resource resource = tuple.get( 0 );
            resources.add( resource );
        }

        updateGroupData( resources );
    }

    protected Unifier createUnifier()
    {
        Unifier u = new Unifier();
        u.addPattern( "beanSubject", Dc.CREATOR , "creator" ); 
        u.addPattern( "creator", Foaf.NAME, Resource.literal( name ) ); 
        u.addColumnName( "beanSubject" );

        return u;
    }
}
