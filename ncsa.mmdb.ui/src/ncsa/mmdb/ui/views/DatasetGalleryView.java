package ncsa.mmdb.ui.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ncsa.mmdb.ui.ImageHolder;
import ncsa.mmdb.ui.TestImageHolder;
import ncsa.mmdb.ui.utils.MMDBItemRenderer;

import org.eclipse.nebula.animation.ScrollingSmoother;
import org.eclipse.nebula.animation.movement.ExpoOut;
import org.eclipse.nebula.widgets.gallery.Gallery;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.nebula.widgets.gallery.NoGroupRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.part.ViewPart;

public class DatasetGalleryView extends ViewPart
{
    private Gallery gallery;
    private int itemWidth = 64;
    private int itemHeight = 64;

    private int count = 2000;

    private List<ImageHolder> holders = Collections.synchronizedList( new ArrayList<ImageHolder>() );
    private Map<ImageHolder, Image> images = Collections.synchronizedMap( new HashMap<ImageHolder, Image>() );
    private Set<ImageHolder> loadingThreads = Collections.synchronizedSet( new HashSet<ImageHolder>() );
    private ThreadPoolExecutor e;

    public DatasetGalleryView()
    {
        for ( int i = 0; i < count; i++ ) {
            holders.add( new TestImageHolder() );
        }

        LinkedBlockingDeque<Runnable> lbq = new LinkedBlockingDeque<Runnable>() {
            public boolean add( Runnable e )
            {
                addFirst( e );
                return true;
            }
        };
        e = new ThreadPoolExecutor( 3, 30, 60, TimeUnit.SECONDS, lbq );
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

        gallery.getVerticalBar().addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e )
            {
                System.err.println( "Selection: " + e );
                super.widgetSelected( e );
            }
        });
        
        gallery.setItemRenderer( new MMDBItemRenderer( images ) );

        NoGroupRenderer groupRenderer = new NoGroupRenderer();
        groupRenderer.setItemSize( (int) (itemWidth * (float) 15 / 11), itemHeight );
        groupRenderer.setAutoMargin( true );
        groupRenderer.setMinMargin( 2 );
        gallery.setGroupRenderer( groupRenderer );

        // 1 group
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
                    item.setText( "Some Group" );

                    item.setItemCount( count );
                } else {
                    // It's an item
                    int index = gallery.indexOf( item );
                    System.err.println( "Setting data for: " + index );

                    ImageHolder imageHolder = holders.get( index );
                    item.setData( "holder", imageHolder );
                }
            }
        } );
    }

    public void setFocus()
    {
    }

    public void dispose()
    {
        for ( Image i : images.values() ) {
            i.dispose();
        }
    }

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
                Image thumbnail = holder.getThumbnail();
                images.put( holder, thumbnail );
                updateItem( item );
                loadingThreads.remove( holder );
            }
        };
        
        e.execute( r );

        loadingThreads.add( holder );
    }
}
