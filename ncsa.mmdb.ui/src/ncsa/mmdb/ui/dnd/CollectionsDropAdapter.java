package ncsa.mmdb.ui.dnd;

import java.util.Arrays;
import java.util.LinkedList;

import ncsa.bard.ui.services.IContextService;
import ncsa.mmdb.ui.MMDBFrame;
import ncsa.mmdb.ui.utils.MMDBUtils;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.PlatformUI;
import org.tupeloproject.kernel.BeanSession;

import edu.uiuc.ncsa.cet.bean.DatasetBean;

public class CollectionsDropAdapter extends ViewerDropAdapter
{
    private BeanSession session;

    public CollectionsDropAdapter( Viewer viewer )
    {
        super( viewer );

        IContextService contextService = (IContextService) PlatformUI.getWorkbench().getService( IContextService.class );

        session = contextService.getDefaultBeanSession();
    }

    public boolean performDrop( Object data )
    {
        String[] fileNames = (String[]) data;
        System.err.println( "Data: " + Arrays.asList( fileNames ) );

        for ( String fileName : fileNames ) {
            MMDBUtils.importDatasetFromFile( session, fileName );
        }

        getViewer().refresh();
        MMDBFrame.getInstance().setCurrentData( new LinkedList<DatasetBean>() );

        return true;
    }

    public boolean validateDrop( Object target, int operation, TransferData transferType )
    {
        return FileTransfer.getInstance().isSupportedType( transferType );
    }
}
