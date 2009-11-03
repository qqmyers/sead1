package edu.illinois.ncsa.bard.ui;

import java.util.List;

import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.bard.ISubjectSource;
import edu.illinois.ncsa.bard.ui.query.MemberQuery;

public class VirtualBardGroup implements ISubjectSource, Refreshable
{
    protected Resource subject;
    protected String label;
    protected MemberQuery query;
    protected List<Resource> members;

    @Override
    public Resource getSubject()
    {
        return subject;
    }

    @Override
    public void refresh()
    {
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel( String label )
    {
        this.label = label;
    }

    public void setSubject( Resource subject )
    {
        this.subject = subject;
    }
    
    /**
     * Can either be another VirtualBardGroup or a Resource (leaf)
     * @param index
     * @return
     */
    public Resource getMember( int index )
    {
        if ( members == null ) {
            try {
                query.execute();
            } catch ( OperatorException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return members.get( index );
    }

    public int getMemberCount()
    {
        if ( members == null && query != null ) {
            try {
                query.execute();
            } catch ( OperatorException e ) {
                // XXX: Exception handling
                e.printStackTrace();
            }
        } 
        
        return members.size();
    }

    public void setMembers( List<Resource> members )
    {
        this.members = members;
    }

    public MemberQuery getQuery()
    {
        return query;
    }

    public void setQuery( MemberQuery query )
    {
        this.query = query;
    }
}
