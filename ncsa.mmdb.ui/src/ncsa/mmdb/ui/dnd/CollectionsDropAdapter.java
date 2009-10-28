package ncsa.mmdb.ui.dnd;

import java.util.Arrays;
import java.util.LinkedList;

import ncsa.bard.ui.services.IContextService;
import ncsa.mmdb.ui.MMDBFrame;
import ncsa.mmdb.ui.utils.MMDBUtils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;
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

        ImportJob j = new ImportJob( fileNames );
        j.setUser( true );
        j.schedule();
        
        return true;
    }

    public boolean validateDrop( Object target, int operation, TransferData transferType )
    {
        return FileTransfer.getInstance().isSupportedType( transferType );
    }

    private class ImportJob extends Job
    {
        private final String[] fileNames;

        public ImportJob( String[] fileNames )
        {
            super( "Importing datasets..." );
            this.fileNames = fileNames;
        }

        protected IStatus run( IProgressMonitor monitor )
        {
            monitor.beginTask( "Importing datasets", fileNames.length );
            for ( String fileName : fileNames ) {
                monitor.setTaskName( fileName );
                MMDBUtils.importDatasetFromFile( session, fileName );
                monitor.worked( 1 );
                
                Display.getDefault().asyncExec( new Runnable() {
                    public void run()
                    {
                        getViewer().refresh();
                    }
                } );
            }

            Display.getDefault().asyncExec( new Runnable() {
                public void run()
                {
                    MMDBFrame.getInstance().setCurrentData( new LinkedList<DatasetBean>() );
                }
            } );

            monitor.done();
            
            return Status.OK_STATUS;
        }
    }
}
