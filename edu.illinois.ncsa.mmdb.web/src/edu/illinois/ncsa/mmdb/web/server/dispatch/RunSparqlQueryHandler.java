package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.LinkedList;
import java.util.List;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.Operator;
import org.tupeloproject.kernel.TableProvider;
import org.tupeloproject.kernel.TripleSetProvider;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.query.sparql.SparqlOperatorFactory;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.RunSparqlQuery;
import edu.illinois.ncsa.mmdb.web.client.dispatch.RunSparqlQueryResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

public class RunSparqlQueryHandler implements ActionHandler<RunSparqlQuery, RunSparqlQueryResult> {
	Log log = LogFactory.getLog(RunSparqlQueryHandler.class);
	
	@Override
	public RunSparqlQueryResult execute(RunSparqlQuery arg0,
			ExecutionContext arg1) throws ActionException {
		try {
			List<List<String>> resultTable = new LinkedList<List<String>>();
			log.debug("executing "+arg0.getQuery());
			Operator o = SparqlOperatorFactory.newOperator(arg0.getQuery());
			TupeloStore.getInstance().getContext().perform(o);
			Iterable<? extends Tuple<Resource>> rowSource = null;
			if(o instanceof TableProvider) {
				rowSource = ((TableProvider)o).getResult();
			} else if(o instanceof TripleSetProvider) {
				rowSource = ((TripleSetProvider)o).getResult();
			}
			if(rowSource != null) {
				for(Tuple<Resource> row : rowSource) { 
					List<String> resultRow = new LinkedList<String>();
					for(Resource cell : row) {
						resultRow.add(cell != null ? cell.getString() : null);
					}
					resultTable.add(resultRow);
				}
				RunSparqlQueryResult result = new RunSparqlQueryResult();
				result.setResult(resultTable);
				for(List<String> row : resultTable) {
					log.debug("result row: "+row);
				}
				return result;
			} else {
				throw new ActionException("query ran, but didn't grok result");
			}
		} catch (Exception x) {
			x.printStackTrace();
			throw new ActionException("query failed",x);
		}
	}

	@Override
	public Class<RunSparqlQuery> getActionType() {
		// TODO Auto-generated method stub
		return RunSparqlQuery.class;
	}

	@Override
	public void rollback(RunSparqlQuery arg0, RunSparqlQueryResult arg1,
			ExecutionContext arg2) throws ActionException {
		// TODO Auto-generated method stub
		
	}

}
