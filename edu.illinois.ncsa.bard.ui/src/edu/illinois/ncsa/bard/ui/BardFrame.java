package edu.illinois.ncsa.bard.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.util.SafeRunnable;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;

import edu.illinois.ncsa.bard.ui.query.FrameQuery;
import edu.illinois.ncsa.bard.ui.query.creator.GroupByCreatorQuery;
import edu.uiuc.ncsa.cet.bean.CETBean;
import edu.uiuc.ncsa.cet.bean.tupelo.TupeloBeanUtil;

/**
 * A BardFrame is short for Frame of Reference.  It defines a single location for managing common features of Bard
 * applications.
 * 
 * -- context management
 * -- bean sessions
 * -- bean utilities
 * 
 * later:
 * -- data
 * -- selections
 * @author shawn
 *
 */
public class BardFrame
{
    protected Context context;
    protected BeanSession beanSesion;
    protected Map<Class<?>, TupeloBeanUtil<? extends CETBean>> utilMap = new HashMap<Class<?>, TupeloBeanUtil<? extends CETBean>>();
    protected List<VirtualBardGroup> data = new ArrayList<VirtualBardGroup>();

    protected FrameQuery query;
    
    protected Set<IFrameListener> frameListeners = new HashSet<IFrameListener>();

    public BardFrame()
    {
//        query = new GroupByTagQuery( this );
        query = new GroupByCreatorQuery( this );
    }

    public void addFrameListener( IFrameListener listener )
    {
        frameListeners.add( listener );
    }

    public void removeFrameListener( IFrameListener listener )
    {
        frameListeners.remove( listener );
    }

    public FrameQuery getQuery()
    {
        return query;
    }

    public void setQuery( FrameQuery query )
    {
        this.query = query;
        executeQuery( query );
    }

    public Context getContext()
    {
        return context;
    }

    public void setContext( Context context )
    {
        System.err.println( "Frame context now: " + context );

        this.context = context;
        executeQuery( query );
    }

    public BeanSession getBeanSesion()
    {
        return beanSesion;
    }

    public void setBeanSesion( BeanSession beanSesion )
    {
        this.beanSesion = beanSesion;
    }

    public TupeloBeanUtil<?> getUtil( Class<?> beanClass )
    {
        return utilMap.get( beanClass );
    }

    public void registerUtil( Class<?> beanClass, TupeloBeanUtil<? extends CETBean> util )
    {
        utilMap.put( beanClass, util );
    }

    public List<VirtualBardGroup> getData()
    {
        return data;
    }

    // AUXILIARY METHOD

    public void fireContextChanged()
    {
        for ( final IFrameListener l : frameListeners ) {
            SafeRunnable.run( new SafeRunnable() {
                public void run()
                {
                    l.contextChanged();
                }
            } );
        }
    }

    public void fireDataChanged()
    {
        for ( final IFrameListener l : frameListeners ) {
            SafeRunnable.run( new SafeRunnable() {
                public void run()
                {
                    l.dataChanged();
                }
            } );
        }
    }

    private void executeQuery( FrameQuery query2 )
    {
        try {
            query.execute();
            fireDataChanged();
        } catch ( OperatorException e ) {
            // XXX: Exception handling
            e.printStackTrace();
        }
    }
}
