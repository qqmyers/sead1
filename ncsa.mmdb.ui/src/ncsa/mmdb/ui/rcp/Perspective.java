package ncsa.mmdb.ui.rcp;

import ncsa.mmdb.ui.views.CollectionsView;
import ncsa.mmdb.ui.views.DatasetGalleryView;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory
{
    public void createInitialLayout( IPageLayout layout )
    {
        layout.setEditorAreaVisible( false );
        layout.addView( CollectionsView.class.getName(), IPageLayout.LEFT, 0.33f, layout.getEditorArea() );
        layout.addView( DatasetGalleryView.class.getName(), IPageLayout.RIGHT, 0.67f, layout.getEditorArea() );
    }
}
