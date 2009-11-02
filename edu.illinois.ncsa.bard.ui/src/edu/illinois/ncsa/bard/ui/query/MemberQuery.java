package edu.illinois.ncsa.bard.ui.query;

import java.util.List;

import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.bard.ui.BardFrame;
import edu.illinois.ncsa.bard.ui.VirtualBardGroup;

public abstract class MemberQuery extends BardQuery
{
    protected final VirtualBardGroup group;
    protected final BardFrame frame;

    public MemberQuery( BardFrame frame, VirtualBardGroup group )
    {
        this.frame = frame;
        this.group = group;
    }

    protected void updateGroupData( List<Resource> data )
    {
        group.setMembers( data );
    }
}
