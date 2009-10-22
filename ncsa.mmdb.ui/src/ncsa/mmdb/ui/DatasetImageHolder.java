package ncsa.mmdb.ui;

import ncsa.mmdb.ui.utils.ImageUtils;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.tupeloproject.kernel.BeanSession;

import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;

public class DatasetImageHolder extends ImageHolder
{
    private final BeanSession session;
    private final DatasetBean bean;
    private final DatasetBeanUtil util;

    public DatasetImageHolder( BeanSession session, DatasetBean bean )
    {
        this.session = session;
        this.bean = bean;
        this.util = new DatasetBeanUtil( session );
    }

    protected Image realizeOriginal()
    {
        try {
            Image i = new Image( Display.getDefault(), util.getData( bean ) );
            return i;
        } catch ( Throwable e ) {
            e.printStackTrace();
            return null;
        }
    }

    protected Image realizeThumbnail()
    {
        Image orig = realizeOriginal();
        Image i = ImageUtils.resizeBestSize( orig, tWidth, tHeight );
        orig.dispose();
        return i;
    }
}
