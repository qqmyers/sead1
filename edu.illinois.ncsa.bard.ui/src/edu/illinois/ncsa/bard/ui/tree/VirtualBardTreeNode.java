package edu.illinois.ncsa.bard.ui.tree;

import edu.illinois.ncsa.bard.ui.VirtualBardGroup;

public class VirtualBardTreeNode
{
    protected VirtualBardGroup group;

    public VirtualBardTreeNode()
    {
    }

    public VirtualBardTreeNode( VirtualBardGroup group )
    {
        this.group = group;
    }

    public VirtualBardGroup getGroup()
    {
        return group;
    }

    public void setGroup( VirtualBardGroup group )
    {
        this.group = group;
    }

    public Object getChild( int index )
    {
        Object child = group.getChild( index );
        if ( child instanceof VirtualBardGroup ) {
            VirtualBardGroup g = (VirtualBardGroup) child;
            return new VirtualBardTreeNode( g );
        }
        
        return child;
    }
}
