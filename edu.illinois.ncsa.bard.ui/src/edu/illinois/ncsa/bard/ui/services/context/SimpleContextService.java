package edu.illinois.ncsa.bard.ui.services.context;

import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;

import edu.uiuc.ncsa.cet.bean.tupelo.CETBeans;

public class SimpleContextService implements IContextService
{
    private Context context;
    private BeanSession beanSession;
    
    public SimpleContextService()
    {
    }

    public Context getDefaultContext()
    {
        return context;
    }
    
    public void setDefaultContext( Context context )
    {
        this.context = context;
        try {
            this.beanSession = CETBeans.createBeanSession( context );
        } catch ( OperatorException e ) {
            e.printStackTrace();
        } catch ( ClassNotFoundException e ) {
            e.printStackTrace();
        }
    }
    
    public BeanSession getDefaultBeanSession()
    {
        return beanSession;
    }
}
