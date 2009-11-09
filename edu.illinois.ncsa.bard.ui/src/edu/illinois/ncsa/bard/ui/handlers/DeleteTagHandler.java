package edu.illinois.ncsa.bard.ui.handlers;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.bard.ui.ISubjectProvider;
import edu.uiuc.ncsa.cet.bean.TagBean;
import edu.uiuc.ncsa.cet.bean.TagEventBean;
import edu.uiuc.ncsa.cet.bean.tupelo.TagEventBeanUtil;

public class DeleteTagHandler extends SubjectSourceHandler
{
    @Override
    protected Object wrappedExecute() throws ExecutionException
    {
        IWorkbenchPart part = HandlerUtil.getActivePart( event );
        if ( !( part instanceof ISubjectProvider) ) {
            return null;
        }
        ISubjectProvider p = (ISubjectProvider) part;
        Resource subject = p.getSubject();

        List<String> tags = new LinkedList<String>();
        
        IStructuredSelection selection = handleSelection( event );
        Iterator<?> iterator = selection.iterator();
        while ( iterator.hasNext() ) {
            TagBean tag = (TagBean) iterator.next();
            tags.add( tag.getTagString() );
        }
        
        boolean b = MessageDialog.openConfirm( Display.getDefault().getActiveShell(), "Remove Tags", "Do you wish to remove the following tags: " + tags + "?");
        if ( ! b )
            return null;
            
        TagEventBeanUtil util = (TagEventBeanUtil) frame.getUtil( TagEventBean.class );
        try {
            // XXX: Performance
            util.removeTags( subject, tags );
        } catch ( OperatorException e ) {
            // XXX: Exception handling
            e.printStackTrace();
        }

        return null;
    }
}
