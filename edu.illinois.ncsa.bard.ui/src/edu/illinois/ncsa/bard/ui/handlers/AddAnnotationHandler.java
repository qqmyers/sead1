package edu.illinois.ncsa.bard.ui.handlers;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.bard.ui.BardFrame;
import edu.illinois.ncsa.bard.ui.wizards.createannotation.CreateAnnotationWizard;
import edu.uiuc.ncsa.cet.bean.AnnotationBean;
import edu.uiuc.ncsa.cet.bean.tupelo.AnnotationBeanUtil;

public class AddAnnotationHandler extends SubjectSourceHandler
{
    @Override
    protected Object wrappedExecute() throws ExecutionException
    {
        try {
            annotateSubject( activePart.getSite().getShell(), subject, frame );
        } catch ( OperatorException e ) {
            // XXX: Exception handling
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
