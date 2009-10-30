package edu.illinois.ncsa.bard.ui.views;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import edu.illinois.ncsa.bard.ui.BardFrame;
import edu.illinois.ncsa.bard.ui.FrameListenerAdapter;
import edu.illinois.ncsa.bard.ui.IFrameListener;
import edu.illinois.ncsa.bard.ui.Refreshable;
import edu.illinois.ncsa.bard.ui.services.bardframe.IBardFrameService;

public abstract class BardFrameView extends ViewPart implements Refreshable
{
    protected BardFrame frame;
    protected IFrameListener frameListener = new MyFrameListener();

    @Override
    public void createPartControl( Composite parent )
    {
        // TODO: This make take some time, we should thread this in some way.
        IBardFrameService frameService = (IBardFrameService) PlatformUI.getWorkbench().getService( IBardFrameService.class );
        frame = frameService.getDefaultFrame();
        frameService.getDefaultFrame().addFrameListener( frameListener );
        
        wrappedCreatePartControl( parent );
    }

    protected abstract void wrappedCreatePartControl( Composite parent );

    @Override
    public void dispose()
    {
        frame.removeFrameListener( frameListener );
        super.dispose();
    }

    @Override
    public void setFocus()
    {
    }

    protected void hookContextMenu( Viewer viewer )
    {
        MenuManager menuMgr = new MenuManager( "#PopupMenu" ); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown( true );
        menuMgr.addMenuListener( new IMenuListener() {
            public void menuAboutToShow( IMenuManager manager )
            {
            }
        } );

        GroupMarker marker = new GroupMarker( IWorkbenchActionConstants.MB_ADDITIONS );
        menuMgr.add( marker );

        Menu menu = menuMgr.createContextMenu( viewer.getControl() );

        viewer.getControl().setMenu( menu );
        getSite().registerContextMenu( menuMgr, viewer );
    }

    protected class MyFrameListener extends FrameListenerAdapter
    {
        @Override
        public void contextChanged()
        {
            refresh();
        }
    }
}
