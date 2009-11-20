package edu.illinois.ncsa.bard.ui.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandImageService;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.SubjectFacade;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.bard.ISubjectSource;
import edu.illinois.ncsa.bard.ui.handlers.AddAnnotationHandler;
import edu.illinois.ncsa.bard.ui.osgi.Activator;
import edu.uiuc.ncsa.cet.bean.AnnotationBean;
import edu.uiuc.ncsa.cet.bean.tupelo.AnnotationBeanUtil;

public class AnnotationView extends BardFrameView
{
    // STATE
    protected SubjectFacade session;
    protected FormToolkit toolkit;
    protected ScrolledForm form;
    protected Collection<AnnotationBean> annotations = new HashSet<AnnotationBean>();
    protected ISelectionListener listener = new MySelectionChangedListener();
    protected List<Section> sections = new LinkedList<Section>();

    // CONTROLS
    protected Composite contents;
    private Resource subject;

    public void refresh()
    {

    }

    protected void wrappedCreatePartControl( Composite parent )
    {
        toolkit = new FormToolkit( parent.getDisplay() );
        contents = toolkit.createComposite( parent );
        contents.setLayout( new FillLayout() );
        form = toolkit.createScrolledForm( contents );
        form.setText( "Annotations" );

        form.getBody().setLayout( new TableWrapLayout() );

        hookSelection();
    }

    private void hookSelection()
    {
        ISelectionService selectionService = getSite().getWorkbenchWindow().getSelectionService();
        selectionService.addSelectionListener( listener );
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

    private void createSection( Composite container, AnnotationBean bean, boolean root )
    {
        Color bgg = Display.getDefault().getSystemColor( SWT.COLOR_TITLE_BACKGROUND_GRADIENT );
        Color gbg = Display.getDefault().getSystemColor( SWT.COLOR_BLACK );
        Color fg = Display.getDefault().getSystemColor( SWT.COLOR_TITLE_FOREGROUND );

        Display.getDefault().getSystemColor( SWT.COLOR_TITLE_BACKGROUND_GRADIENT );
        Display.getDefault().getSystemColor( SWT.COLOR_TITLE_BACKGROUND );

        Section section = toolkit.createSection( container, Section.TWISTIE | Section.EXPANDED | SWT.BORDER );
        section.setBackground( bgg );
        section.setTitleBarGradientBackground( gbg );
        section.setTitleBarBorderColor( gbg );
        section.setForeground( fg );
        section.descriptionVerticalSpacing = 20;
        section.setText( bean.getTitle() );
        section.setData( FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER );

        toolkit.setBorderStyle( SWT.BORDER );
        toolkit.paintBordersFor( section );

        Composite body = toolkit.createComposite( section );
        body.setLayout( new TableWrapLayout() );

        FormText text = toolkit.createFormText( body, false );
        text.setText( bean.getDescription(), false, false );
        section.setClient( body );

        TableWrapData td = new TableWrapData();
        td.align = TableWrapData.FILL;
        td.grabHorizontal = true;
        td.colspan = 1;
        if ( !root )
            td.indent = 20;
        section.setLayoutData( td );

        section.addExpansionListener( new ExpansionAdapter() {
            public void expansionStateChanged( ExpansionEvent e )
            {
                form.reflow( true );
            }
        } );

        ToolBarManager toolbarManager = new ToolBarManager( SWT.FLAT | SWT.HORIZONTAL );

        ToolBar bar = toolbarManager.createControl( section );
        bar.setBackground( bgg );
        bar.setForeground( fg );
        section.setTextClient( bar );

        String key = "edu.illinois.ncsa.bard.ui.command.annotate";
        Image image = retrieveImage( key );
        ToolItem item = new ToolItem( bar, SWT.PUSH );
        item.setImage( image );
        item.setToolTipText( "Reply to this annotation." );
        item.addSelectionListener( new AnnotateListener( bean ) );

        key = "org.eclipse.ui.edit.delete";
        image = retrieveImage( key );
        item = new ToolItem( bar, SWT.PUSH );
        item.setImage( image );
        item.setToolTipText( "Delete this annotation." );
        item.addSelectionListener( new DeleteListener( bean ) );

        Collection<AnnotationBean> children = getBeans( Resource.resource( bean.getUri() ) );
        for ( AnnotationBean child : children ) {
            createSection( body, child, false );
        }

        sections.add( section );
    }

    private Image retrieveImage( String key )
    {
        ImageRegistry imageRegistry = Activator.getDefault().getImageRegistry();

        if ( imageRegistry.get( key ) == null ) {
            ICommandImageService imageService = (ICommandImageService) PlatformUI.getWorkbench().getService( ICommandImageService.class );
            ImageDescriptor id = imageService.getImageDescriptor( key );
            imageRegistry.put( key, id.createImage() );
        }
        
        return imageRegistry.get( key );
    }

    public void setSubject( Resource subject )
    {
        this.subject = subject;
        
        annotations.clear();

        for ( Control section : sections ) {
            section.dispose();
        }

        if ( subject == null ) {
            form.setText( "Annotations" );
        } else {
            form.setText( "Annotations for: " + subject.getString() );

            annotations = getBeans( subject );
            for ( AnnotationBean b : annotations ) {
                createSection( form.getBody(), b, true );
            }
        }

        form.layout();
        form.reflow( true );
    }

    private class MySelectionChangedListener implements ISelectionListener
    {
        public void selectionChanged( IWorkbenchPart part, ISelection selection )
        {
            if ( selection.isEmpty() )
                return;

            IStructuredSelection s = (IStructuredSelection) selection;
            Object object = s.getFirstElement();
            
            ISubjectSource n = null;
            if ( object instanceof ISubjectSource ) {
                n = (ISubjectSource) object;
            } else {
                n = (ISubjectSource) Platform.getAdapterManager().loadAdapter( object, ISubjectSource.class.getName() );
            }

            if ( n == null )
                setSubject( null );
            else
                setSubject( n.getSubject() );
        }
    }

    private class AnnotateListener extends SelectionAdapter
    {
        private AnnotationBean bean;

        public AnnotateListener( AnnotationBean bean )
        {
            this.bean = bean;
        }

        public void widgetSelected( SelectionEvent e )
        {
            try {
                AddAnnotationHandler.annotateSubject( getSite().getShell(), Resource.resource( bean.getUri() ), frame );
                setSubject( subject );
            } catch ( OperatorException e1 ) {
                e1.printStackTrace();
            }
        }
    }

    private class DeleteListener extends SelectionAdapter
    {
        private AnnotationBean bean;

        public DeleteListener( AnnotationBean bean )
        {
            this.bean = bean;
        }

        public void widgetSelected( SelectionEvent e )
        {
            MessageDialog.openInformation( getSite().getShell(), "Not Implemented", "Delete annotation not implemented."  );
        }
    }

    public Collection<AnnotationBean> getBeans( Resource subject )
    {
        AnnotationBeanUtil util = (AnnotationBeanUtil) frame.getUtil( AnnotationBean.class );
        try {
            // XXX: Performance
            Collection<AnnotationBean> a = util.getAssociationsFor( subject );
            return a;
        } catch ( Exception e ) {
            // XXX: Exception handling
            e.printStackTrace();
            return new ArrayList<AnnotationBean>( 0 );
        }
    }
}
