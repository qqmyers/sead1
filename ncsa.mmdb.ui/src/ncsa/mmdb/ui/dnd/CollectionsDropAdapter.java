package ncsa.mmdb.ui.dnd;

import java.util.Arrays;

import ncsa.mmdb.ui.utils.MMDBUtils;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.tupeloproject.kernel.BeanSession;

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
                
        for ( String fileName : fileNames ) {
            MMDBUtils.importDatasetFromFile( session, fileName );            
        }
        
        getViewer().refresh();        
        return true;
    }

    public boolean validateDrop( Object target, int operation, TransferData transferType )
    {
        return FileTransfer.getInstance().isSupportedType( transferType );
    }
}
