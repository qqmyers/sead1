package edu.illinois.ncsa.bard.ui.wizards.createannotation;

import java.util.Calendar;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;

import edu.illinois.ncsa.bard.ui.utils.CompositeUtils;
import edu.uiuc.ncsa.cet.bean.AnnotationBean;

/**
 * @author shawn@ncsa.uiuc.edu
 */
public class AnnotationPage extends WizardPage
{
    private Text titleField;
    private Text descriptionField;
    private Triple output;
    private Resource incomingSubject;

    public AnnotationPage( String pageName )
    {
        super( pageName );
        addBasicPageInfo();
    }

    private void addBasicPageInfo()
    {
        setTitle( "Create Annotation" );
        setDescription( "Enter a comment" );
    }

    protected void dialogModified()
    {

    }

    public void createControl( Composite parent )
    {
        Composite container = CompositeUtils.createFormContainer( parent, 2, true, SWT.NONE );
        titleField = CompositeUtils.createLabelledText( container, "Title", 1 );
        descriptionField = CompositeUtils.createLabelledTextArea( container, "Description", 1 );
        setControl( container );
    }

    public AnnotationBean getAnnotation()
    {
        AnnotationBean bean = new AnnotationBean();
        bean.setTitle( titleField.getText().trim() );
        bean.setDescription( descriptionField.getText().trim() );
        bean.setDate( Calendar.getInstance().getTime() );

        return bean;
    }

    public void setIncomingSubject( Resource incomingSubject )
    {
        this.incomingSubject = incomingSubject;
    }
}
