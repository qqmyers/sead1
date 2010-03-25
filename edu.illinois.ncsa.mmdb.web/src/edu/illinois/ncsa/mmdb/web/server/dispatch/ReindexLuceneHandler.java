package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ReindexLucene;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ReindexLuceneResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

public class ReindexLuceneHandler implements ActionHandler<ReindexLucene, ReindexLuceneResult> {

	@Override
	public ReindexLuceneResult execute(ReindexLucene arg0, ExecutionContext arg1)
			throws ActionException {
		return new ReindexLuceneResult(TupeloStore.getInstance().indexFullTextAll());
	}

	@Override
	public Class<ReindexLucene> getActionType() {
		return ReindexLucene.class;
	}

	@Override
	public void rollback(ReindexLucene arg0, ReindexLuceneResult arg1,
			ExecutionContext arg2) throws ActionException {
		// TODO Auto-generated method stub
		
	}

}
