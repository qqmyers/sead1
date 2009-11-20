package edu.illinois.ncsa.bard.ui.services.bardframe;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.beans.reflect.BeanMappingCache;
import org.tupeloproject.kernel.impl.MemoryContext;

import edu.illinois.ncsa.bard.ui.BardFrame;
import edu.uiuc.ncsa.cet.bean.AnnotationBean;
import edu.uiuc.ncsa.cet.bean.CETBean;
import edu.uiuc.ncsa.cet.bean.ContextBean;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.TagEventBean;
import edu.uiuc.ncsa.cet.bean.tupelo.AnnotationBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.CETBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.ContextBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.ContextListner;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.TagBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.TagEventBeanUtil;

public class SimpleBardFrameServiceFactory extends AbstractServiceFactory
{
    private BardFrameService service;

    public SimpleBardFrameServiceFactory()
    {
    }

    @SuppressWarnings("unchecked")
    public Object create( Class serviceInterface, IServiceLocator parentLocator, IServiceLocator locator )
    {
        initializeBeanStore();

        service = new BardFrameService();
        ContextBeanUtil.addContextListner( new MyContextListener() );

        BardFrame frame = new BardFrame();

        Context defaultContext = getDefaultContext();
        BeanSession defaultBeanSession = getDefaultBeanSession( defaultContext );
        createDefaultUtils( frame, defaultBeanSession );

        frame.setBeanSesion( defaultBeanSession );
        frame.setContext( defaultContext );

        service.createFrame( BardFrameService.DEFAULT_KEY, frame );

        return service;
    }

    protected void initializeBeanStore()
    {
        // TODO: This should use a preference
        File dataDir = new File( System.getProperty( "user.home" ), "NCSA/MMDB" );
        if ( !dataDir.exists() )
            dataDir.mkdirs();

        try {
            ContextBeanUtil.setContextBeanStore( new File( dataDir, "contexts.xml" ) );
        } catch ( Exception e ) {
            // XXX: Exception handling
            e.printStackTrace();
        }
    }

    protected Context getDefaultContext()
    {
        Context defaultContext = null;
        try {
            defaultContext = ContextBeanUtil.getContextBeanStore().getDefaultContext();
        } catch ( Exception e ) {
            // XXX: Exception handling

            if ( e instanceof NullPointerException ) {
                // no context defined
                MessageDialog.openInformation( Display.getDefault().getActiveShell(), "No Default Context", "No default context defined.  Using memory context." );
            } else {
                e.printStackTrace();
            }
            defaultContext = new MemoryContext();
        }

        return defaultContext;
    }

    protected BeanSession getDefaultBeanSession( Context context )
    {
        BeanSession s = new BeanSession( context );
        populateBeanSession( s );
        return s;
    }

    protected void createDefaultUtils( BardFrame frame, BeanSession session )
    {
        frame.registerUtil( DatasetBean.class, new DatasetBeanUtil( session ) );
        frame.registerUtil( CETBean.class, new CETBeanUtil( session ) );
        frame.registerUtil( PersonBean.class, new PersonBeanUtil( session ) );
        frame.registerUtil( AnnotationBean.class, new AnnotationBeanUtil( session ) );
        frame.registerUtil( TagEventBean.class, new TagEventBeanUtil( session ) );
        frame.registerUtil( ContextBean.class, new ContextBeanUtil( session ) );
    }

    protected void populateBeanSession( BeanSession s )
    {
        BeanMappingCache cache = null;
        try {
            cache = s.getBeanMappingCache();
        } catch ( OperatorException e ) {
            // XXX: Exception handling
            e.printStackTrace();
            return;
        }

        cache.put( DatasetBeanUtil.getMapping() );
        cache.put( CETBeanUtil.getMapping() );
        cache.put( PersonBeanUtil.getMapping() );
        cache.put( AnnotationBeanUtil.getMapping() );
        cache.put( TagBeanUtil.getMapping() );
        cache.put( TagEventBeanUtil.getMapping() );
        cache.put( ContextBeanUtil.getMapping() );
    }

    protected class MyContextListener implements ContextListner
    {
        @Override
        public void defaultContext( Context context )
        {
            if ( service == null )
                return;

            BardFrame defaultFrame = service.getDefaultFrame();

            BeanSession defaultBeanSession = getDefaultBeanSession( context );
            createDefaultUtils( defaultFrame, defaultBeanSession );

            defaultFrame.setBeanSesion( defaultBeanSession );
            defaultFrame.setContext( context );
        }

        @Override
        public void initializeContext( Context context )
        {
            // Currently does not handle muliple frames...
            BardFrame defaultFrame = service.getDefaultFrame();
            defaultFrame.fireContextChanged();
        }

        @Override
        public void reloadContext( Context context )
        {
            // Currently does not handle muliple frames...
            BardFrame defaultFrame = service.getDefaultFrame();
            defaultFrame.fireContextChanged();
        }
    }
}
