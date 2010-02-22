/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.ImagePyramidBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PreviewImageBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PreviewVideoBeanUtil;

/**
 * Retrieve a specific dataset.
 * 
 * @author Luigi Marini
 * 
 */
public class GetDatasetHandler implements ActionHandler<GetDataset, GetDatasetResult>
{

    /** Commons logging **/
    private static Log log = LogFactory.getLog( GetDatasetHandler.class );

    @Override
    public GetDatasetResult execute( GetDataset action, ExecutionContext arg1 ) throws ActionException
    {

        BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

        DatasetBeanUtil dbu = new DatasetBeanUtil( beanSession );

        try {
            DatasetBean datasetBean = dbu.get( action.getUri() );

            datasetBean = dbu.update( datasetBean );

            Collection<PreviewBean> previews = new HashSet<PreviewBean>();
            

            // image previews
            previews.addAll( new PreviewImageBeanUtil( beanSession ).getAssociationsFor( action.getUri() ) );

            // video previews
            previews.addAll( new PreviewVideoBeanUtil( beanSession ).getAssociationsFor( action.getUri() ) );

            // FIXME the next query is probably unnecessary, if we can get to the underlying BeanThing
            // representing the dataset which will have this triple in it, or not
            Set<Triple> pyramids = TupeloStore.getInstance().getContext().match( Resource.uriRef( datasetBean.getUri() ), ImagePyramidBeanUtil.HAS_PYRAMID, null );
            String pyramid = null;
            if ( pyramids.size() > 0 ) {
                pyramid = pyramids.iterator().next().getObject().getString();
            }
            return new GetDatasetResult( datasetBean, previews, pyramid );
        } catch ( Exception e ) {
            log.error( "Error retrieving dataset " + action.getUri(), e );
            throw new ActionException( e );
        }

    }

    @Override
    public Class<GetDataset> getActionType()
    {
        return GetDataset.class;
    }

    @Override
    public void rollback( GetDataset arg0, GetDatasetResult arg1, ExecutionContext arg2 ) throws ActionException
    {
        // TODO Auto-generated method stub

    }

}
