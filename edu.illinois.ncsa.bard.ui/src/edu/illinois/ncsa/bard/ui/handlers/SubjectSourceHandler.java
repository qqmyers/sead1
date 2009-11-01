package edu.illinois.ncsa.bard.ui.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.bard.ISubjectSource;
import edu.illinois.ncsa.bard.ui.BardFrame;
import edu.illinois.ncsa.bard.ui.services.bardframe.IBardFrameService;

public abstract class SubjectSourceHandler extends BardAbstractHandler
{
    protected ExecutionEvent event;
    protected Resource subject;
    protected BardFrame frame;

    protected abstract Object wrappedExecute() throws ExecutionException;

    public Object execute( ExecutionEvent event ) throws ExecutionException
    {
        this.event = event;
        IStructuredSelection selection = handleSelection( event );
        Object object = selection.getFirstElement();

        ISubjectSource n = null;
        if ( object instanceof ISubjectSource ) {
            n = (ISubjectSource) object;
        } else {
            n = (ISubjectSource) Platform.getAdapterManager().loadAdapter( object, ISubjectSource.class.getName() );
        }

        if ( n == null )
            return null;

        subject = n.getSubject();
        IBardFrameService service = (IBardFrameService) PlatformUI.getWorkbench().getService( IBardFrameService.class );

        frame = service.getDefaultFrame();

        return null;
    }
}
