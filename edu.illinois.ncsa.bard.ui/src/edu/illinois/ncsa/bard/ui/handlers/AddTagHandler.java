package edu.illinois.ncsa.bard.ui.handlers;

import java.util.Calendar;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;

import edu.illinois.ncsa.bard.ui.BardFrame;
import edu.uiuc.ncsa.cet.bean.TagBean;
import edu.uiuc.ncsa.cet.bean.TagEventBean;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.TagEventBeanUtil;

public class AddTagHandler extends SubjectSourceHandler
{
    @Override
    protected Object wrappedExecute() throws ExecutionException
    {
        try {
            tagSubject( activePart.getSite().getShell(), subject, frame );
        } catch ( OperatorException e ) {
            // XXX: Exception handling
            e.printStackTrace();
        }

        return null;
    }

    public static void tagSubject( Shell shell, Resource subject, BardFrame frame ) throws OperatorException
    {
        InputDialog d = new InputDialog( shell, "Add Tag", "Tag", "", null );
        if ( d.open() == Window.CANCEL )
            return;
        
        String value = d.getValue();
        
        TagBean tag = new TagBean();
        tag.setTagString( value );
        
        // XXX: ID management
        tag.setUri( UriRef.uriRef().getString() );
        
        TagEventBean bean = new TagEventBean();
        bean.addTag( tag );
        bean.setTagEventDate( Calendar.getInstance().getTime() );
        
        // XXX: User management
        bean.setTagCreator( PersonBeanUtil.getAnonymous() );
        
        // XXX: ID management
        bean.setUri( UriRef.uriRef().getString() );
                
        TagEventBeanUtil util = (TagEventBeanUtil) frame.getUtil( TagEventBean.class );
        util.addAssociationTo( subject, bean );
        frame.getBeanSesion().registerAndSave( bean );
    }
}
