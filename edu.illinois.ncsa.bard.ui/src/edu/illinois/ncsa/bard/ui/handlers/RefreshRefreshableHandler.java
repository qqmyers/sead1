package edu.illinois.ncsa.bard.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.UIJob;

import edu.illinois.ncsa.bard.ui.Refreshable;

public class RefreshRefreshableHandler extends AbstractHandler implements IHandler
{
    public Object execute( ExecutionEvent event ) throws ExecutionException
    {
        final IWorkbenchPart part = HandlerUtil.getActivePart( event );
        
        UIJob j = new UIJob( "Refreshing..." ) {
            @Override
            public IStatus runInUIThread( IProgressMonitor monitor )
            {
                
                if ( part instanceof Refreshable ) {
                    Refreshable r = (Refreshable) part;
                    r.refresh();
                }
                return Status.OK_STATUS;
            }
        };
        j.schedule();
        
        return null;
    }
}
