package edu.illinois.ncsa.bard.ui.services.context;

import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.Context;

public interface IContextService
{
    Context getDefaultContext();
    BeanSession getDefaultBeanSession();
}
