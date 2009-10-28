package ncsa.mmdb.ui.utils;

import java.util.Map;

import ncsa.mmdb.ui.ImageHolder;
import ncsa.mmdb.ui.osgi.Activator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.nebula.widgets.gallery.DefaultGalleryItemRenderer;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.nebula.widgets.gallery.RendererHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

public class MMDBItemRenderer extends DefaultGalleryItemRenderer
{
    private Image defaultImage;
    private Map<ImageHolder, Image> images;

    boolean dropShadows = false;
    int dropShadowsSize = 0;
    int dropShadowsAlphaStep = 20;
    Color selectionForegroundColor;
    Color selectionBackgroundColor;
    Color foregroundColor, backgroundColor;
    boolean showLabels = true;
    boolean showRoundedSelectionCorners = true;
    int selectionRadius = 15;

    public MMDBItemRenderer( Map<ImageHolder, Image> images )
    {
        super();
        this.images = images;

        ImageDescriptor imageDescriptor = Activator.getImageDescriptor( "icons/loading.png" );
        Activator.getDefault().getImageRegistry().put( "loading", imageDescriptor );
        defaultImage = Activator.getDefault().getImageRegistry().get( "loading" );

        foregroundColor = Display.getDefault().getSystemColor( SWT.COLOR_LIST_FOREGROUND );
        backgroundColor = Display.getDefault().getSystemColor( SWT.COLOR_LIST_BACKGROUND );

        selectionForegroundColor = Display.getDefault().getSystemColor( SWT.COLOR_LIST_SELECTION_TEXT );
        selectionBackgroundColor = Display.getDefault().getSystemColor( SWT.COLOR_LIST_SELECTION );
    }

    public void draw( GC gc, GalleryItem item, int index, int x, int y, int width, int height )
    {
        if ( images == null )
            return;

        Image itemImage = null;

        if ( item.getData( "holder" ) instanceof ImageHolder ) {
            ImageHolder h = (ImageHolder) item.getData( "holder" );
            itemImage = images.get( h );
            // This seems like a bad place to do this as its a heavyweight operation called in a painting thread
            // But somewhere we need to manage this...
//            h.updateOverlays( item );
        }

        if ( itemImage == null )
            itemImage = defaultImage;

        Color itemBackgroundColor = item.getBackground();
        Color itemForegroundColor = item.getForeground();

        // Set up the GC
        gc.setFont( getFont( item ) );

        // Create some room for the label.
        int useableHeight = height;
        int fontHeight = 0;
        if ( item.getText() != null && !EMPTY_STRING.equals( item.getText() ) && this.showLabels ) {
            fontHeight = gc.getFontMetrics().getHeight();
            useableHeight -= fontHeight + 2;
        }

        int imageWidth = 0;
        int imageHeight = 0;
        int xShift = 0;
        int yShift = 0;
        Point size = null;

        if ( itemImage != null ) {
            Rectangle itemImageBounds = itemImage.getBounds();
            imageWidth = itemImageBounds.width;
            imageHeight = itemImageBounds.height;

            size = RendererHelper.getBestSize( imageWidth, imageHeight, width - 8 - 2 * this.dropShadowsSize, useableHeight - 8 - 2 * this.dropShadowsSize );

            xShift = RendererHelper.getShift( width, size.x );
            yShift = RendererHelper.getShift( useableHeight, size.y );

            if ( dropShadows ) {
                Color c = null;
                for ( int i = this.dropShadowsSize - 1; i >= 0; i-- ) {
                    c = (Color) dropShadowsColors.get( i );
                    gc.setForeground( c );

                    gc.drawLine( x + width + i - xShift - 1, y + dropShadowsSize + yShift, x + width + i - xShift - 1, y + useableHeight + i - yShift );
                    gc.drawLine( x + xShift + dropShadowsSize, y + useableHeight + i - yShift - 1, x + width + i - xShift, y - 1 + useableHeight + i - yShift );
                }
            }
        }

        // Draw background (rounded rectangles)
        if ( selected ) {

            // Set colors
            if ( selected ) {
                gc.setBackground( selectionBackgroundColor );
                gc.setForeground( selectionBackgroundColor );
            } else if ( itemBackgroundColor != null ) {
                gc.setBackground( itemBackgroundColor );
            }

            // Draw
            if ( showRoundedSelectionCorners ) {
                gc.fillRoundRectangle( x, y, width, useableHeight, selectionRadius, selectionRadius );
            } else {
                gc.fillRectangle( x, y, width, height );
            }

            if ( item.getText() != null && !EMPTY_STRING.equals( item.getText() ) && showLabels ) {
                gc.fillRoundRectangle( x, y + height - fontHeight, width, fontHeight, selectionRadius, selectionRadius );
            }
        }

        // Draw image
        if ( itemImage != null && size != null ) {
            if ( size.x > 0 && size.y > 0 ) {
                gc.drawImage( itemImage, 0, 0, imageWidth, imageHeight, x + xShift, y + yShift, size.x, size.y );
                drawAllOverlays( gc, item, x, y, size, xShift, yShift );
            }

        }

        // Draw label
        if ( item.getText() != null && !EMPTY_STRING.equals( item.getText() ) && showLabels ) {
            // Set colors
            if ( selected ) {
                // Selected : use selection colors.
                gc.setForeground( selectionForegroundColor );
                gc.setBackground( selectionBackgroundColor );
            } else {
                // Not selected, use item values or defaults.

                // Background
                if ( itemBackgroundColor != null ) {
                    gc.setBackground( itemBackgroundColor );
                } else {
                    gc.setBackground( backgroundColor );
                }

                // Foreground
                if ( itemForegroundColor != null ) {
                    gc.setForeground( itemForegroundColor );
                } else {
                    gc.setForeground( foregroundColor );
                }
            }

            // Create label
            String text = RendererHelper.createLabel( item.getText(), gc, width - 10 );

            // Center text
            int textWidth = gc.textExtent( text ).x;
            int textxShift = RendererHelper.getShift( width, textWidth );

            // Draw
            gc.drawText( text, x + textxShift, y + height - fontHeight, true );
        }
    }
}
