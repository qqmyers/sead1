package ncsa.mmdb.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ncsa.mmdb.ui.utils.MMDBUtils;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.tupeloproject.kernel.BeanSession;

import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;

public class MMDBFrame implements ISelectionChangedListener, ISelectionProvider
{
    private static MMDBFrame instance;

    private List<DatasetBean> current = new ArrayList<DatasetBean>();
    private Set<ISelectionChangedListener> listeners = new HashSet<ISelectionChangedListener>();

    private MMDBFrame()
    {

    }

    public static MMDBFrame getInstance()
    {
        if ( instance == null )
            instance = new MMDBFrame();

        return instance;
    }

    public List<DatasetBean> getCurrentData()
    {
        return current;
    }

    public List<DatasetBean> getAllData()
    {
        BeanSession session = MMDBUtils.getDefaultBeanSession();
        DatasetBeanUtil util = new DatasetBeanUtil( session );

        try {
            return new ArrayList<DatasetBean>( util.getAll() );
        } catch ( Exception e ) {
            return new ArrayList<DatasetBean>( 0 );
        }
    }
    
    public void setCurrentData( List<DatasetBean> list )
    {
        this.current = list;
        for ( ISelectionChangedListener l : listeners ) {
            l.selectionChanged( new SelectionChangedEvent( this, getSelection() ) );
        }
    }

    // SELECTION CHANGED LISTENER
    
    public void addSelectionChangedListener( ISelectionChangedListener listener )
    {
        listeners.add( listener );
    }

    public void removeSelectionChangedListener( ISelectionChangedListener listener )
    {
        listeners.remove( listener );
    }

    public ISelection getSelection()
    {
        return new StructuredSelection( current.toArray() );
    }

    @SuppressWarnings("unchecked")
    public void setSelection( ISelection selection )
    {
        current.clear();
        
        IStructuredSelection s = (IStructuredSelection) selection;
        Iterator<DatasetBean> iterator = s.iterator();
        while ( iterator.hasNext() ) {
            DatasetBean b = iterator.next();
            current.add( b );
        }
    }

    // SELECTION CHANGED LISTENER
    
    public void selectionChanged( SelectionChangedEvent event )
    {
    }

    // AUXILIARY METHODS
    
    private void testData()
    {
        String home = System.getProperty( "user.home" );
        File testData = new File( home, "mmdb/pics" );

        System.err.println( "Creating test data from: " + testData.getAbsolutePath() );

        File[] list = testData.listFiles();
        for ( File f : list ) {
            current.add( MMDBUtils.importDatasetFromFile( MMDBUtils.getDefaultBeanSession(), f.getAbsolutePath() ) );
        }
    }
}
