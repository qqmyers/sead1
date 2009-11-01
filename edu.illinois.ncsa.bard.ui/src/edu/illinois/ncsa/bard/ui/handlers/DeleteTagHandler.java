package edu.illinois.ncsa.bard.ui.handlers;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

public class DeleteTagHandler extends SubjectSourceHandler
{
    @Override
    protected Object wrappedExecute() throws ExecutionException
    {
        MessageDialog.openInformation( Display.getDefault().getActiveShell(), "Not Implemented", "Delete tag not implemented." );
        return null;
    }
}
