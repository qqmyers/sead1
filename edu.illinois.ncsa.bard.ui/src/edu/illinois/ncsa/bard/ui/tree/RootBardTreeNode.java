package edu.illinois.ncsa.bard.ui.tree;

import edu.illinois.ncsa.bard.ui.BardFrame;
import edu.illinois.ncsa.bard.ui.VirtualBardGroup;

public class RootBardTreeNode extends VirtualBardTreeNode
{
    private final BardFrame frame;

    public RootBardTreeNode( BardFrame frame )
    {
        this.frame = frame;        
    }
    
    public Object getChild( int index )
    {
        VirtualBardGroup g = new VirtualBardGroup();
        g.setLabel( "Label" );
        
        VirtualBardTreeNode n = new VirtualBardTreeNode( g );
        return n;
    }
}
