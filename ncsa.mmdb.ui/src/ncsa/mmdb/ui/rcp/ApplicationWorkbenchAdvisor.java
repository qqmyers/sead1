package ncsa.mmdb.ui.rcp;

import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	private static final String PERSPECTIVE_ID = "ncsa.mmdb.ui.perspective";

    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor( IWorkbenchWindowConfigurer configurer )
    {
        return new ApplicationWorkbenchWindowAdvisor( configurer );
    }

    public String getInitialWindowPerspectiveId()
    {
        return PERSPECTIVE_ID;
    }

    
    public void initialize( IWorkbenchConfigurer configurer )
    {
        super.initialize( configurer );
        PlatformUI.getPreferenceStore().setValue( IWorkbenchPreferenceConstants.SHOW_MEMORY_MONITOR, true );
        PlatformUI.getPreferenceStore().setValue( IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS, false );
    }

    protected IWorkbenchConfigurer getWorkbenchConfigurer()
    {
        IWorkbenchConfigurer configurer = super.getWorkbenchConfigurer();
        configurer.setSaveAndRestore( true );
        return configurer;
    }
}
