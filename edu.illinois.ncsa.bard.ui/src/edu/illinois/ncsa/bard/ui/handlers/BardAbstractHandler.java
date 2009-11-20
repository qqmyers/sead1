package edu.illinois.ncsa.bard.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

public abstract class BardAbstractHandler extends AbstractHandler
{
    protected IWorkbenchPart activePart;

    protected IStructuredSelection handleSelection( ExecutionEvent event )
    {
        activePart = HandlerUtil.getActivePart( event );

        ISelection currentSelection = HandlerUtil.getActiveMenuSelection( event );
        if ( currentSelection == null )
            currentSelection = HandlerUtil.getCurrentSelection( event );

        if ( currentSelection == null || currentSelection.isEmpty() ) {
            System.out.println( "Current selection empty" );
            return null;
        }

        IStructuredSelection selection = (IStructuredSelection) currentSelection;
        return selection;
    }

    protected IWorkbenchSiteProgressService getProgressService( IViewPart part )
    {
        IWorkbenchSiteProgressService service = null;
        Object siteService = part.getSite().getAdapter( IWorkbenchSiteProgressService.class );
        if ( siteService != null ) {
            service = (IWorkbenchSiteProgressService) siteService;
        }
        return service;
    }
}
