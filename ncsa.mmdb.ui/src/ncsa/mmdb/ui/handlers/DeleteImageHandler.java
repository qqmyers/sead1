package ncsa.mmdb.ui.handlers;

import java.util.Iterator;

import ncsa.bard.ui.Refreshable;
import ncsa.bard.ui.services.IContextService;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.rdf.Resource;

import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;

public class DeleteImageHandler extends AbstractHandler
{
    public DeleteImageHandler()
    {
    }

    @SuppressWarnings("unchecked")
    public Object execute( ExecutionEvent event ) throws ExecutionException
    {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked( event );
        ISelection selection = HandlerUtil.getCurrentSelection( event );
        if ( selection.isEmpty() ) return null;
        
        IContextService service = (IContextService) PlatformUI.getWorkbench().getService( IContextService.class );
        BeanSession session = service.getDefaultBeanSession();
        DatasetBeanUtil util = new DatasetBeanUtil( session );
        
        IStructuredSelection s = (IStructuredSelection) selection;
        Iterator<DatasetBean> i = s.iterator();
        while ( i.hasNext() ) {
            DatasetBean b = i.next();
            try {
                Resource r = Resource.resource( b.getUri() );
                session.deregister( r );
                session.removeBlob( r );
                session.remove( r );
            } catch ( OperatorException e ) {
                e.printStackTrace();
            }
            
        }

        try {
            session.save();
        } catch ( OperatorException e ) {
            e.printStackTrace();
        }

        if ( window instanceof Refreshable ) {
            Refreshable r = (Refreshable) window;
            r.refresh();
        }
        
        return null;
    }
}
