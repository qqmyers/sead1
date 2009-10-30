package edu.illinois.ncsa.bard.ui.services.bardframe;

import edu.illinois.ncsa.bard.ui.BardFrame;

public interface IBardFrameService
{
    BardFrame getDefaultFrame();
    BardFrame getFrame( String key );
    void createFrame( String key, BardFrame frame );
}
