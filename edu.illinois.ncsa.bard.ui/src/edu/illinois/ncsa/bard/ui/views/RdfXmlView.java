package edu.illinois.ncsa.bard.ui.views;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleFetcher;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.xml.RdfXml;

import edu.illinois.ncsa.bard.ISubjectSource;

public class RdfXmlView extends BardFrameView
{
    private ISelectionListener listener = new MySelectionChangedListener();
    private Text textArea;

    public RdfXmlView()
    {
    }

    public void refresh()
    {
        // currently no-op
        handle( frame.getContext() );
    }

    protected void wrappedCreatePartControl( Composite parent )
    {
        textArea = new Text( parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );

        hookSelection();

        hookDragAndDrop();
    }

    private void hookDragAndDrop()
    {
    }

    private void hookSelection()
    {
        ISelectionService selectionService = getSite().getWorkbenchWindow().getSelectionService();
        selectionService.addSelectionListener( listener );
    }

    protected void handle( Object object )
    {
        Set<Triple> triples = new HashSet<Triple>();

        if ( object instanceof Context ) {
            Context context = (Context) object;
            try {
                triples = context.getTriples();
            } catch ( OperatorException e ) {
                e.printStackTrace();
            }
        } else {
            ISubjectSource n = null;

            if ( object instanceof ISubjectSource ) {
                n = (ISubjectSource) object;
            } else {
                n = (ISubjectSource) Platform.getAdapterManager().loadAdapter( object, ISubjectSource.class.getName() );
            }

            if ( n == null )
                return;

            Resource subject = n.getSubject();

            TripleFetcher tf = new TripleFetcher();
            tf.setSubject( subject );
            try {
                frame.getContext().perform( tf );
                triples = tf.getResult();
            } catch ( OperatorException e ) {
                e.printStackTrace();
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            RdfXml.write( triples, baos );
            textArea.setText( new String( baos.toByteArray(), "UTF-8" ) );
        } catch ( Throwable e ) {
            e.printStackTrace();
            for ( Triple triple : triples ) {
                System.err.println( triple );
            }
        }

    }

    public void dispose()
    {
        ISelectionService selectionService = getSite().getWorkbenchWindow().getSelectionService();
        selectionService.removeSelectionListener( listener );

        super.dispose();
    }

    public void setFocus()
    {
    }

    private class MySelectionChangedListener implements ISelectionListener
    {
        public void selectionChanged( IWorkbenchPart part, ISelection selection )
        {
            if ( selection.isEmpty() )
                return;

            IStructuredSelection s = (IStructuredSelection) selection;
            Object object = s.getFirstElement();

            handle( object );
        }
    }
}
