package ncsa.mmdb.ui.dnd;

import java.util.Arrays;
import java.util.Calendar;

import ncsa.mmdb.ui.MMDBUtils;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.rdf.Resource;

import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;

public class CollectionsDropAdapter extends ViewerDropAdapter
{
    private BeanSession session = MMDBUtils.getDefaultBeanSession();

    public CollectionsDropAdapter( Viewer viewer )
    {
        super( viewer );
    }

    public boolean performDrop( Object data )
    {
        String[] fileNames = (String[]) data;
        System.err.println( "Data: " + Arrays.asList( fileNames ) );
        
        DatasetBeanUtil util = new DatasetBeanUtil( session );
        
        for ( String fileName : fileNames ) {
            DatasetBean bean = new DatasetBean();
            bean.setCreator( MMDBUtils.getCurrentUser() );
            bean.setDate( Calendar.getInstance().getTime() );
            bean.setTitle( MMDBUtils.getName( fileName ) );
            bean.setMimeType( MMDBUtils.getMimeType( fileName ) );
            try {
                Resource subject = session.registerAndSave( bean );
                util.setData( bean, fileName );
            } catch ( Throwable t ) {
                t.printStackTrace();
            }            
        }
        
        getViewer().refresh();        
        return true;
    }

    public boolean validateDrop( Object target, int operation, TransferData transferType )
    {
        return FileTransfer.getInstance().isSupportedType( transferType );
    }
}
