package edu.illinois.ncsa.bard.ui.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.bard.ISubjectSource;
import edu.uiuc.ncsa.cet.bean.TagBean;
import edu.uiuc.ncsa.cet.bean.TagEventBean;
import edu.uiuc.ncsa.cet.bean.tupelo.TagEventBeanUtil;

public class TagView extends BardFrameView
{
    // STATE
    protected Collection<TagEventBean> annotations = new HashSet<TagEventBean>();
    protected ISelectionListener listener = new MySelectionChangedListener();
    protected Resource subject;

    // CONTROLS
    protected Composite contents;
    private ListViewer viewer;

    public void refresh()
    {
        viewer.refresh();
    }

    protected void wrappedCreatePartControl( Composite parent )
    {
        viewer = new ListViewer( parent );
        viewer.setContentProvider( new MyContentProvider() );
        viewer.setLabelProvider( new MyLabelProvider() );
        viewer.setInput( getSite() );

        getSite().setSelectionProvider( viewer );
        
        hookContextMenu( viewer );
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

    public void setSubject( Resource subject )
    {
        this.subject = subject;
        refresh();
    }

    private class MySelectionChangedListener implements ISelectionListener
    {
        public void selectionChanged( IWorkbenchPart part, ISelection selection )
        {
            if ( selection.isEmpty() || part.equals( TagView.this ) )
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

    public Collection<TagBean> getBeans( Resource subject )
    {
        if ( subject == null )
            return new ArrayList<TagBean>( 0 );

        TagEventBeanUtil util = (TagEventBeanUtil) frame.getUtil( TagEventBean.class );
        Collection<TagBean> tags = new HashSet<TagBean>();
        
        try {
            // XXX: Performance
            Collection<TagEventBean> a = util.getAssociationsFor( subject );

            for ( TagEventBean t : a ) {
                Set<TagBean> beans = t.getTags();
                for ( TagBean tagBean : beans ) {
                    tags.add( tagBean );
                }
            }
            
            return tags;
        } catch ( Exception e ) {
            // XXX: Exception handling
            e.printStackTrace();
            return new ArrayList<TagBean>( 0 );
        }
    }

    private class MyContentProvider implements IStructuredContentProvider
    {
        @Override
        public Object[] getElements( Object inputElement )
        {
            return getBeans( subject ).toArray();
        }

        @Override
        public void dispose()
        {
        }

        @Override
        public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
        {
        }
    }
    
    private class MyLabelProvider extends LabelProvider
    {
        @Override
        public String getText( Object element )
        {
            if ( element instanceof TagBean ) {
                TagBean t = (TagBean) element;
                return t.getTagString();
            }
            return super.getText( element );
        }
    }
}
