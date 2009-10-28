package ncsa.mmdb.ui.views;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ncsa.bard.ui.Refreshable;
import ncsa.bard.ui.services.IContextService;
import ncsa.mmdb.ui.DatasetImageHolder;
import ncsa.mmdb.ui.ImageHolder;
import ncsa.mmdb.ui.MMDBFrame;
import ncsa.mmdb.ui.utils.MMDBItemRenderer;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.animation.ScrollingSmoother;
import org.eclipse.nebula.animation.movement.ExpoOut;
import org.eclipse.nebula.widgets.gallery.Gallery;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.nebula.widgets.gallery.NoGroupRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.tupeloproject.kernel.BeanSession;

import edu.uiuc.ncsa.cet.bean.DatasetBean;

public class DatasetGalleryView extends ViewPart implements ISelectionProvider, Refreshable
{
    private Gallery gallery;
    private int itemWidth = 64;
    private int itemHeight = 64;

    private int count = 5;

    private MMDBFrame frame = MMDBFrame.getInstance();
    private BeanSession session;
    private ISelectionChangedListener scl = new MySelectionChangedListener();

    //    private List<ImageHolder> holders = Collections.synchronizedList( new ArrayList<ImageHolder>() );
    private Map<DatasetBean, ImageHolder> datasetMap = new HashMap<DatasetBean, ImageHolder>();
    private Map<ImageHolder, Image> images = frame.getImageCache();
    private Set<ImageHolder> loadingThreads = Collections.synchronizedSet( new HashSet<ImageHolder>() );
    private Map<ISelectionChangedListener, WrappedSelectionAdapter> listeners = new HashMap<ISelectionChangedListener, WrappedSelectionAdapter>();
    private ExecutorService e;

    public DatasetGalleryView()
    {
        IContextService contextService = (IContextService) PlatformUI.getWorkbench().getService( IContextService.class );
        session = contextService.getDefaultBeanSession();

//        LinkedBlockingDeque<Runnable> lbq = new LinkedBlockingDeque<Runnable>() {
//            public boolean add( Runnable e )
//            {
//                addFirst( e );
//                return true;
//            }
//        };
//        
//        e = new ThreadPoolExecutor( 3, 3, 60, TimeUnit.SECONDS, lbq );
        e = Executors.newFixedThreadPool( 3 );
    }

    public void refresh()
    {
        gallery.layout();
    }

    public void createPartControl( Composite parent )
    {
        gallery = new Gallery( parent, SWT.V_SCROLL | SWT.VIRTUAL | SWT.MULTI );
        gallery.setBackground( Display.getDefault().getSystemColor( SWT.COLOR_WHITE ) );
        gallery.setLowQualityOnUserAction( true );
        gallery.setHigherQualityDelay( 500 );
        gallery.setAntialias( SWT.OFF );
        gallery.setInterpolation( SWT.LOW );
        gallery.setVirtualGroups( true );
        gallery.setVirtualGroupDefaultItemCount( 100 );
        gallery.setVirtualGroupsCompatibilityMode( true );

        gallery.setItemRenderer( new MMDBItemRenderer( images ) );

        NoGroupRenderer groupRenderer = new NoGroupRenderer();
        groupRenderer.setItemSize( (int) (itemWidth * (float) 15 / 11), itemHeight );
        groupRenderer.setAutoMargin( true );
        groupRenderer.setMinMargin( 2 );
        gallery.setGroupRenderer( groupRenderer );

        // **** XXX TEST: 1 group
        gallery.setItemCount( 1 );

        ScrollingSmoother ss = new ScrollingSmoother( gallery, new ExpoOut() );
        ss.smoothControl( true );

        gallery.addListener( SWT.PaintItem, new Listener() {
            public void handleEvent( Event event )
            {
                switch ( event.type ) {
                    case SWT.PaintItem:
                        final GalleryItem item = (GalleryItem) event.item;
                        if ( item != null && item.getParentItem() != null ) {
                            ImageHolder holder = (ImageHolder) item.getData( "holder" );
                            Image i = images.get( holder );

                            if ( i == null ) {
                                loadItem( item );
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        } );
        gallery.addListener( SWT.SetData, new Listener() {
            public void handleEvent( Event event )
            {
                GalleryItem item = (GalleryItem) event.item;

                if ( item.getParentItem() == null ) {
                    // It's a group

                    // **** XXX: TEST
                    item.setItemCount( frame.getCurrentData().size() );
                } else {
                    // It's an item
                    int index = gallery.indexOf( item );
                    DatasetBean bean = frame.getCurrentData().get( index );
                    System.err.println( "Setting data for: " + index + " to " + bean );

                    ImageHolder imageHolder = datasetMap.get( bean );
                    if ( imageHolder == null ) {
                        imageHolder = new DatasetImageHolder( session, bean );
                        datasetMap.put( bean, imageHolder );
                    }
                    item.setData( "holder", imageHolder );
                }
            }
        } );

        hookContextMenu();

        getSite().setSelectionProvider( this );

        frame.addSelectionChangedListener( scl );
    }

    public void setFocus()
    {
    }

    public void dispose()
    {
        for ( Image i : images.values() ) {
            i.dispose();
        }
        frame.removeSelectionChangedListener( scl );
    }

    // SELECTION PROVIDER

    public void addSelectionChangedListener( ISelectionChangedListener listener )
    {
        if ( !listeners.containsKey( listener ) ) {
            WrappedSelectionAdapter l = new WrappedSelectionAdapter( listener );
            listeners.put( listener, l );
            gallery.addSelectionListener( l );
        }
    }

    public ISelection getSelection()
    {
        return new StructuredSelection( gallery.getSelection() );
    }

    public void removeSelectionChangedListener( ISelectionChangedListener listener )
    {
        if ( listeners.containsKey( listener ) ) {
            WrappedSelectionAdapter l = listeners.get( listener );
            gallery.removeSelectionListener( l );
            listeners.remove( listener );
        }
    }

    public void setSelection( ISelection selection )
    {
    }

    // AUXILIARY METHODS FOR IMAGE MANAGEMENT

    private void updateItem( final GalleryItem item )
    {
        Display.getDefault().syncExec( new Runnable() {
            public void run()
            {
                Rectangle bounds = item.getBounds();
                gallery.redraw( bounds.x, bounds.y, bounds.width, bounds.height, false );
            }
        } );
    }

    private synchronized void loadItem( final GalleryItem item )
    {
        final ImageHolder holder = (ImageHolder) item.getData( "holder" );
        final int index = gallery.indexOf( item );

        if ( loadingThreads.contains( holder ) )
            return;

        Runnable r = new Runnable() {
            public void run()
            {
                long first = System.currentTimeMillis();
                System.err.println( "Here at: " + first + " for " + index );
                Image thumbnail = holder.getThumbnail();
                images.put( holder, thumbnail );
                updateItem( item );
                loadingThreads.remove( holder );
                long second = System.currentTimeMillis();
                System.err.println( "Took: " + ( second - first ) + " for " + index );
            }
        };

        e.execute( r );

        loadingThreads.add( holder );
    }

    // AUXILIARY METHODS

    private void hookContextMenu()
    {
        MenuManager menuMgr = new MenuManager( "#PopupMenu" ); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown( true );
        menuMgr.addMenuListener( new IMenuListener() {
            public void menuAboutToShow( IMenuManager manager )
            {
            }
        } );

        GroupMarker marker = new GroupMarker( IWorkbenchActionConstants.MB_ADDITIONS );
        menuMgr.add( marker );

        Menu menu = menuMgr.createContextMenu( gallery );

        gallery.setMenu( menu );
        getSite().registerContextMenu( menuMgr, this );
    }

    // PRIVATE CLASSES

    private class WrappedSelectionAdapter implements SelectionListener
    {
        private ISelectionChangedListener l;

        public WrappedSelectionAdapter( ISelectionChangedListener l )
        {
            super();
            this.l = l;
        }

        public void widgetDefaultSelected( SelectionEvent e )
        {
            SelectionChangedEvent event = new SelectionChangedEvent( DatasetGalleryView.this, new StructuredSelection( gallery.getSelection() ) );
            l.selectionChanged( event );
        }

        public void widgetSelected( SelectionEvent e )
        {
            SelectionChangedEvent event = new SelectionChangedEvent( DatasetGalleryView.this, new StructuredSelection( gallery.getSelection() ) );
            l.selectionChanged( event );
        }
    }

    private class MySelectionChangedListener implements ISelectionChangedListener
    {
        public void selectionChanged( SelectionChangedEvent event )
        {
            gallery.clearAll();
        }
    }
}
