/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010, NCSA.  All rights reserved.
 *
 * Developed by:
 * Cyberenvironments and Technologies (CET)
 * http://cet.ncsa.illinois.edu/
 *
 * National Center for Supercomputing Applications (NCSA)
 * http://www.ncsa.illinois.edu/
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the names of CET, University of Illinois/NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *******************************************************************************/
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

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
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
