package ncsa.mmdb.ui.views;

import ncsa.mmdb.ui.dnd.CollectionsDropAdapter;
import ncsa.mmdb.ui.providers.MimeTypeImageProvider;
import ncsa.mmdb.ui.utils.MMDBUtils;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.tupeloproject.kernel.BeanSession;

import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;

public class CollectionsView extends ViewPart
{
    private BeanSession session = MMDBUtils.getDefaultBeanSession();
    private DatasetBeanUtil util = new DatasetBeanUtil( session );

    public CollectionsView()
    {
    }

    public void createPartControl( Composite parent )
    {
        TreeViewer viewer = new TreeViewer( parent );
        viewer.setContentProvider( new MyContentProvider() );
        viewer.setLabelProvider( new MyLabelProvider() );
        viewer.setInput( "Test" );
        
        hookDragAndDrop( viewer );
    }

    private void hookDragAndDrop( TreeViewer viewer )
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
