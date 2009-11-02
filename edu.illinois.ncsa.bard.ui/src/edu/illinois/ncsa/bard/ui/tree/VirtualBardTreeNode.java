package edu.illinois.ncsa.bard.ui.tree;

import java.util.HashMap;
import java.util.Map;

import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.bard.ui.BardFrame;
import edu.illinois.ncsa.bard.ui.VirtualBardGroup;
import edu.uiuc.ncsa.cet.bean.CETBean;

public class VirtualBardTreeNode
{
    protected VirtualBardGroup group;
    protected BardFrame frame;
    protected Map<Integer,CETBean> beans = new HashMap<Integer,CETBean>();

    public VirtualBardTreeNode()
    {
    }

    public VirtualBardTreeNode( BardFrame frame, VirtualBardGroup group )
    {
        this.frame = frame;
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

    public int getChildCount()
    {
        return group.getMemberCount();
    }

    public Object getChild( int index )
    {
        if ( beans.get( index ) != null )
            return beans.get( index );
        
        Object child = group.getMember( index );

        if ( child instanceof VirtualBardGroup ) {
            VirtualBardGroup g = (VirtualBardGroup) child;
            return new VirtualBardTreeNode( frame, g );
        } else if ( child instanceof Resource ) {
            Resource subject = (Resource) child;
            
            try {
                CETBean bean = (CETBean) frame.getBeanSesion().fetchBean( subject );
                beans.put( index, bean );

                return bean;
            } catch ( Throwable t ) {
                // TODO Auto-generated catch block
                t.printStackTrace();
            }
        }
        
        return child;
    }
}
