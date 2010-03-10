/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.Collection;
import java.util.HashSet;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;

import edu.illinois.ncsa.cet.search.Hit;
import edu.illinois.ncsa.cet.search.TextExtractor;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.illinois.ncsa.mmdb.web.server.search.SearchableThingTextExtractor;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PreviewImageBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PreviewPyramidBeanUtil;
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

            // pyramid previews
            previews.addAll( new PreviewPyramidBeanUtil( beanSession ).getAssociationsFor( action.getUri() ) );

    		// FIXME debug
            // now extract text
            TupeloStore.getInstance().getSearch().reindex(action.getUri());
            //
            TextExtractor<String> ex = new SearchableThingTextExtractor();
            String query = ex.extractText(action.getUri());
            query = query.substring(query.indexOf(' ')+1);
            log.info("searching for "+query);
            try {
            	int i = 0;
            	for(Hit hit : TupeloStore.getInstance().getSearch().search(query)) {
            		i++;
            		log.info(i+" "+hit.getId()+" "+ex.extractText(hit.getId()));
            	}
            } catch(Exception x) {
            	log.debug("search failed: "+x.getMessage());
            }
            // end debug

            // return dataset and preview
            return new GetDatasetResult( datasetBean, previews );
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
