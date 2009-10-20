package ncsa.mmdb.ui;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.tupeloproject.kernel.Context;

import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;

public class CollectionsView extends ViewPart
{
    private Context context = MMDBUtils.getDefaultContext();
    private DatasetBeanUtil util = new DatasetBeanUtil( context.getBeanSession() );

    public CollectionsView()
    {
    }

    public void createPartControl( Composite parent )
    {
        TreeViewer viewer = new TreeViewer( parent );
        viewer.setContentProvider( new MyContentProvider() );
        viewer.setLabelProvider( new LabelProvider() );
        viewer.setInput( "Test" );
    }

    public void setFocus()
    {
    }

    private class MyContentProvider implements IStructuredContentProvider
    {
        public void dispose()
        {
        }

        public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
        {
        }

        public Object[] getElements( Object inputElement )
        {
            return null;
        }
    }
}
