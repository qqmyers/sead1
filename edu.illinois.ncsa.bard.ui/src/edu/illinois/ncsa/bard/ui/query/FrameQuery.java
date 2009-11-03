package edu.illinois.ncsa.bard.ui.query;

import java.util.Collection;

import edu.illinois.ncsa.bard.ui.BardFrame;
import edu.illinois.ncsa.bard.ui.VirtualBardGroup;

public abstract class FrameQuery extends BardQuery
{
    protected BardFrame frame;

    public FrameQuery()
    {
    }
    
    public FrameQuery( BardFrame frame )
    {
        this.frame = frame;
    }
    
    public BardFrame getFrame()
    {
        return frame;
    }

    public void setFrame( BardFrame frame )
    {
        this.frame = frame;
    }

    protected void updateFrameData( Collection<VirtualBardGroup> data )
    {
        frame.getData().clear();
        frame.getData().addAll( data );
    }
}
