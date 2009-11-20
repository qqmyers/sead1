package edu.illinois.ncsa.bard.ui.utils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class CompositeUtils
{
    private CompositeUtils()
    {
    }

    public static Composite createFormContainer( Composite parent, int numColumns, boolean fillVertical, int style )
    {
        Composite container = new Composite( parent, SWT.NONE );

        GridData data = new GridData( GridData.HORIZONTAL_ALIGN_FILL );
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;

        if ( fillVertical ) {
            data.verticalAlignment = SWT.CENTER;
            data.grabExcessVerticalSpace = true;
        }

        container.setLayoutData( data );

        GridLayout layout = new GridLayout();
        container.setLayout( layout );
        layout.numColumns = numColumns;
        layout.verticalSpacing = 9;

        return container;
    }

    public static Text createLabelledText( Composite container, String labelString, int colSpan )
    {
        Label label = new Label( container, SWT.NONE );
        label.setText( labelString );

        GridData data = new GridData( GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_HORIZONTAL );
        data.horizontalSpan = colSpan;

        Text text = new Text( container, SWT.BORDER );
        text.setLayoutData( data );

        return text;
    }

    public static Text createLabelledTextArea( Composite container, String labelString, int colSpan )
    {
        if ( labelString != null ) {
            Label label = new Label( container, SWT.NONE );
            label.setText( labelString );
        }

        GridData data = new GridData( GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL );
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        data.horizontalSpan = colSpan;

        Text text = new Text( container, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL );
        text.setLayoutData( data );

        return text;
    }

}
