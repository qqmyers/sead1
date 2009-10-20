package ncsa.mmdb.ui;

import ncsa.mmdb.ui.views.CollectionsView;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory
{
    public void createInitialLayout( IPageLayout layout )
    {
        layout.setEditorAreaVisible( false );
        layout.addView( CollectionsView.class.getName(), IPageLayout.LEFT, 0.33f, layout.getEditorArea() );
    }
}
