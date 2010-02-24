/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ExtractionService;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ExtractionServiceResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

/**
 * Retrieve a specific dataset.
 * 
 * @author Luigi Marini
 * 
 */
public class ExtractionServiceHandler implements ActionHandler<ExtractionService, ExtractionServiceResult>
{
    @Override
    public ExtractionServiceResult execute( ExtractionService action, ExecutionContext arg1 ) throws ActionException
    {
        return new ExtractionServiceResult( TupeloStore.getInstance().extractPreviews( action.getUri(), true ) );
    }

    @Override
    public Class<ExtractionService> getActionType()
    {
        return ExtractionService.class;
    }

    @Override
    public void rollback( ExtractionService arg0, ExtractionServiceResult arg1, ExecutionContext arg2 ) throws ActionException
    {
        // TODO Auto-generated method stub

    }

}
