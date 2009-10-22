package ncsa.mmdb.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ncsa.mmdb.ui.utils.MMDBUtils;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

public class MMDBFrame
{
    private static MMDBFrame instance;
    
    private List<DatasetBean> current = new ArrayList<DatasetBean>();
    
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
        if ( current.isEmpty() )
            testData();
        
        return current;
    }

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
