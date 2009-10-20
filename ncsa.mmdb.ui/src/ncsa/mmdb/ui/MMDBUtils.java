package ncsa.mmdb.ui;

import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.impl.MemoryContext;

public class MMDBUtils
{
    public static Context getLocalContext()
    {
        return new MemoryContext();
    }
    
    public static Context getRemoteContext()
    {
        return null;
    }
    
    public static Context getDefaultContext()
    {
        return getLocalContext();
    }
}
