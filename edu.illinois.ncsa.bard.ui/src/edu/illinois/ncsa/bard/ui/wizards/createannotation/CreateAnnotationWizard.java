package edu.illinois.ncsa.bard.ui.wizards.createannotation;

import org.eclipse.jface.wizard.Wizard;

import edu.uiuc.ncsa.cet.bean.AnnotationBean;

public class CreateAnnotationWizard extends Wizard
{
    private AnnotationPage addAnnotationPage;
    private AnnotationBean annotation;

    public CreateAnnotationWizard()
    {
    }

    public void addPages()
    {
        addAnnotationPage = new AnnotationPage( "Create Annotation" );
        addPage( addAnnotationPage );
    }

    public boolean performFinish()
    {
        annotation = addAnnotationPage.getAnnotation();
        return true;
    }

    public AnnotationBean getAnnotation()
    {
        return annotation;
    }
}
