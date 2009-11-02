package edu.illinois.ncsa.bard.ui.tree;

import java.util.HashMap;
import java.util.Map;

import edu.illinois.ncsa.bard.ui.BardFrame;
import edu.illinois.ncsa.bard.ui.VirtualBardGroup;

public class RootBardTreeNode extends VirtualBardTreeNode
{
    protected final BardFrame frame;

    protected Map<VirtualBardGroup, VirtualBardTreeNode> nodeMap = new HashMap<VirtualBardGroup, VirtualBardTreeNode>();
    
    public RootBardTreeNode( BardFrame frame )
    {
        this.frame = frame;        
    }
    
    public Object getChild( int index )
    {
        VirtualBardGroup g = frame.getData().get( index );
        VirtualBardTreeNode n = nodeMap.get( g );
        
        if ( n == null ) {
            n = new VirtualBardTreeNode( frame, g );
            nodeMap.put( g, n );
        }
        
        return n;
    }
    
    public int getChildCount()
    {
        return frame.getData().size();
    }
}
