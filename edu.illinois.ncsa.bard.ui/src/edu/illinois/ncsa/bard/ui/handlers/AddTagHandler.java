package edu.illinois.ncsa.bard.ui.handlers;

import java.util.Calendar;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;

import edu.illinois.ncsa.bard.ISubjectSource;
import edu.illinois.ncsa.bard.ui.BardFrame;
import edu.illinois.ncsa.bard.ui.services.bardframe.IBardFrameService;
import edu.uiuc.ncsa.cet.bean.TagBean;
import edu.uiuc.ncsa.cet.bean.TagEventBean;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.TagEventBeanUtil;

public class AddTagHandler extends BardAbstractHandler
{
    public Object execute( ExecutionEvent event ) throws ExecutionException
    {
        try {
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

            Resource subject = n.getSubject();
            IBardFrameService service = (IBardFrameService) PlatformUI.getWorkbench().getService( IBardFrameService.class );

            // XXX: Not multi-frame friendly
            BardFrame frame = service.getDefaultFrame();

            tagSubject( activePart.getSite().getShell(), subject, frame );
        } catch ( Throwable e ) {
            e.printStackTrace();
        }

        return null;
    }

    public static void tagSubject( Shell shell, Resource subject, BardFrame frame ) throws OperatorException
    {
        InputDialog d = new InputDialog( shell, "Add Tag", "Tag", "", null );
        if ( d.open() == Window.CANCEL )
            return;
        
        String value = d.getValue();
        
        TagBean tag = new TagBean();
        tag.setTagString( value );
        
        // XXX: ID management
        tag.setUri( UriRef.uriRef().getString() );
        
        TagEventBean bean = new TagEventBean();
        bean.addTag( tag );
        bean.setTagEventDate( Calendar.getInstance().getTime() );
        
        // XXX: User management
        bean.setTagCreator( PersonBeanUtil.getAnonymous() );
        
        // XXX: ID management
        bean.setUri( UriRef.uriRef().getString() );
                
        TagEventBeanUtil util = (TagEventBeanUtil) frame.getUtil( TagEventBean.class );
        util.addAssociationTo( subject, bean );
        frame.getBeanSesion().registerAndSave( bean );
    }
}
