package ncsa.mmdb.ui.views;

import ncsa.bard.ui.Refreshable;
import ncsa.bard.ui.services.IContextService;
import ncsa.mmdb.ui.dnd.CollectionsDropAdapter;
import ncsa.mmdb.ui.providers.MimeTypeImageProvider;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.tupeloproject.kernel.BeanSession;

import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;

public class CollectionsView extends ViewPart implements Refreshable
{
    private BeanSession session;
    private DatasetBeanUtil util;
    private TreeViewer viewer;

    public CollectionsView()
    {
    }
    
    public void refresh()
    {
        viewer.refresh();
    }

    public void createPartControl( Composite parent )
    {
        IContextService imageService = (IContextService) PlatformUI.getWorkbench().getService( IContextService.class );
        session = imageService.getDefaultBeanSession();
        util = new DatasetBeanUtil( session );
        
        viewer = new TreeViewer( parent, SWT.VIRTUAL );
        viewer.setContentProvider( new MyContentProvider() );
        viewer.setLabelProvider( new MyLabelProvider() );
        viewer.setInput( "Test" );
        
        getSite().setSelectionProvider( viewer );
        
        hookContextMenu();
        hookDragAndDrop();
    }

    private void hookContextMenu()
    {
        MenuManager menuMgr = new MenuManager( "#PopupMenu" ); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown( true );
        menuMgr.addMenuListener( new IMenuListener() {
            public void menuAboutToShow( IMenuManager manager )
            {
            }
        } );

        GroupMarker marker = new GroupMarker( IWorkbenchActionConstants.MB_ADDITIONS );
        menuMgr.add( marker );

        Menu menu = menuMgr.createContextMenu( viewer.getControl() );

        viewer.getControl().setMenu( menu );
        getSite().registerContextMenu( menuMgr, viewer );
    }

    private void hookDragAndDrop( )
    {
        int ops = DND.DROP_COPY | DND.DROP_MOVE;
        Transfer[] transfers = new Transfer[] { FileTransfer.getInstance() };
        viewer.addDropSupport( ops, transfers, new CollectionsDropAdapter( viewer ) );
    }

    public void setFocus()
    {
    }

    private class MyContentProvider implements ITreeContentProvider
    {
        public Object[] getChildren( Object parentElement )
        {
            return null;
        }

        public Object getParent( Object element )
        {
            return null;
        }

        public boolean hasChildren( Object element )
        {
            return false;
        }

        public void dispose()
        {
        }

        public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
        {
        }

        public Object[] getElements( Object inputElement )
        {
            try {
                return util.getAll().toArray();
            } catch ( Exception e ) {
                e.printStackTrace();
                return new Object[] { "Error" };
            }
        }
    }
    
    private class MyLabelProvider extends LabelProvider
    {
        private MimeTypeImageProvider p = MimeTypeImageProvider.getInstance();
        
        public Image getImage( Object element )
        {
            return p.getImage( element );
        }

        public String getText( Object element )
        {
            if ( element instanceof DatasetBean ) {
                DatasetBean b = (DatasetBean) element;
                return b.getTitle();
            }
            
            return null;
        }
    }
}
