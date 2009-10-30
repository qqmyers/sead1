package edu.illinois.ncsa.bard.ui.services.bardframe;

import java.util.HashMap;
import java.util.Map;

import edu.illinois.ncsa.bard.ui.BardFrame;

public class BardFrameService implements IBardFrameService
{
    public final static String DEFAULT_KEY = "default";
    
    protected Map<String,BardFrame> frames = new HashMap<String, BardFrame>();
    
    public BardFrameService()
    {
    }

    @Override
    public void createFrame( String key, BardFrame frame )
    {
        frames.put( key, frame );
    }

    @Override
    public BardFrame getDefaultFrame()
    {
        return frames.get( DEFAULT_KEY );
    }

    @Override
    public BardFrame getFrame( String key )
    {
        return frames.get( key );
    }
}
