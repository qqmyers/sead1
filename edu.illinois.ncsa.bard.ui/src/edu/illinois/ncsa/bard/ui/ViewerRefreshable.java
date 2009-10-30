package edu.illinois.ncsa.bard.ui;

import org.eclipse.jface.viewers.Viewer;

public class ViewerRefreshable implements Refreshable
{
    private Viewer viewer;
    
    public ViewerRefreshable( Viewer viewer )
    {
        this.viewer = viewer;
    }

    @Override
    public void refresh()
    {   
        viewer.refresh();
    }
}
