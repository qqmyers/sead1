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

/**
 * TODO Add comments
 * 
 * @author Joe Futrelle
 * 
 */
public class RunSparqlQueryHandler implements
		ActionHandler<RunSparqlQuery, RunSparqlQueryResult> {

	/** Commons logging **/
	private static Log log = LogFactory.getLog(RunSparqlQueryHandler.class);

	@Override
	public RunSparqlQueryResult execute(RunSparqlQuery arg0,
			ExecutionContext arg1) throws ActionException {
		try {
			List<List<String>> resultTable = new LinkedList<List<String>>();
			Operator o = SparqlOperatorFactory.newOperator(arg0.getQuery());
			TupeloStore.getInstance().getContext().perform(o);
			Iterable<? extends Tuple<Resource>> rowSource = null;
			if (o instanceof TableProvider) {
				rowSource = ((TableProvider) o).getResult();
			} else if (o instanceof TripleSetProvider) {
				rowSource = ((TripleSetProvider) o).getResult();
			}
			if (rowSource != null) {
				for (Tuple<Resource> row : rowSource) {
					List<String> resultRow = new LinkedList<String>();
					for (Resource cell : row) {
						resultRow.add(cell != null ? cell.getString() : null);
					}
					resultTable.add(resultRow);
				}
				RunSparqlQueryResult result = new RunSparqlQueryResult();
				result.setResult(resultTable);
				return result;
			} else {
				log.debug("query ran, but didn't grok result");
				throw new ActionException("query ran, but didn't grok result");
			}
		} catch (Exception x) {
			log.error("query failed", x);
			throw new ActionException("query failed", x);
		}
	}

	@Override
	public Class<RunSparqlQuery> getActionType() {
		return RunSparqlQuery.class;
	}

	@Override
	public void rollback(RunSparqlQuery arg0, RunSparqlQueryResult arg1,
			ExecutionContext arg2) throws ActionException {
		// TODO Auto-generated method stub

	}

}
