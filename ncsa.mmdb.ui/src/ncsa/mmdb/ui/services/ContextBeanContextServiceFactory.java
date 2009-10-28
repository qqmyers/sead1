package ncsa.mmdb.ui.services;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ncsa.bard.ui.services.SimpleContextService;

import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;
import org.tupeloproject.kernel.Context;

import edu.uiuc.ncsa.cet.bean.ContextBean;
import edu.uiuc.ncsa.cet.bean.tupelo.ContextBeanUtil;
import edu.uiuc.ncsa.cet.tupelo.contexts.H2ContextCreator;

public class ContextBeanContextServiceFactory extends AbstractServiceFactory
{
    public ContextBeanContextServiceFactory()
    {
    }

    @SuppressWarnings("unchecked")
    public Object create( Class serviceInterface, IServiceLocator parentLocator, IServiceLocator locator )
    {
        SimpleContextService s = new SimpleContextService();

        File dataDir = new File( System.getProperty( "user.home" ), "NCSA/MMDB" );
        if ( !dataDir.exists() )
            dataDir.mkdirs();

        try {
            ContextBeanUtil.setContextBeanStore( new File( dataDir, "contexts.xml" ) );
            ContextBeanUtil.addContextCreator( new H2ContextCreator() );
            Context context;
            
            // XXX: Hacks...needs to be smarter
            Collection<ContextBean> collection = ContextBeanUtil.getContextBeanStore().getAll();
            if ( collection.size() == 0 ) {
                ContextBean b = new ContextBean();
                b.addProperty( "type", "H2" );
                b.addProperty( H2ContextCreator.H2_FOLDER, new File( dataDir, "H2" ).getAbsolutePath() );
                b.addProperty( H2ContextCreator.H2_DATABASE, "tupelo" );

                H2ContextCreator c = new H2ContextCreator();
                context = c.createContext( b.getProperties() );
                context.initialize();

                ContextBeanUtil.getContextBeanStore().addContext( b, true );
            } else {
                List<ContextBean> l = new ArrayList<ContextBean>( collection );
                System.err.println( "Using existing context: " + l.get( 0 ) );
                context = ContextBeanUtil.getContextBeanStore().getContext( l.get( 0 ) );
            }
            
            s.setDefaultContext( context );
        } catch ( Throwable e ) {
        }

        return s;
    }
}
