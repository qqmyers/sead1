package edu.illinois.ncsa.bard.ui.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.bard.ISubjectSource;
import edu.illinois.ncsa.bard.ui.BardFrame;
import edu.illinois.ncsa.bard.ui.services.bardframe.IBardFrameService;
import edu.illinois.ncsa.bard.ui.wizards.createannotation.CreateAnnotationWizard;
import edu.uiuc.ncsa.cet.bean.AnnotationBean;
import edu.uiuc.ncsa.cet.bean.tupelo.AnnotationBeanUtil;

public class AddAnnotationHandler extends BardAbstractHandler
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
            
            annotateSubject( activePart.getSite().getShell(), subject, frame );
        } catch ( Throwable e ) {
            e.printStackTrace();
        }

        return null;
    }

    public static void annotateSubject( Shell shell, Resource subject, BardFrame frame ) throws OperatorException
    {
        CreateAnnotationWizard wizard = new CreateAnnotationWizard();
        WizardDialog d = new WizardDialog( shell, wizard );
        if ( d.open() == Window.CANCEL )
            return;

        AnnotationBean annotation = wizard.getAnnotation();
        
        AnnotationBeanUtil util = (AnnotationBeanUtil) frame.getUtil( AnnotationBean.class );
        util.addAssociationTo( subject, annotation );
        frame.getBeanSesion().registerAndSave( annotation );
    }
}
