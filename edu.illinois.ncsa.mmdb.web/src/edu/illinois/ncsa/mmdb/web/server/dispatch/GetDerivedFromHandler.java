package edu.illinois.ncsa.mmdb.web.server.dispatch;

import static org.tupeloproject.rdf.terms.Cet.cet;

import java.util.LinkedList;
import java.util.List;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.util.Tables;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDerivedFrom;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDerivedFromResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

public class GetDerivedFromHandler implements ActionHandler<GetDerivedFrom, GetDerivedFromResult> {

	@Override
	public GetDerivedFromResult execute(GetDerivedFrom arg0,
			ExecutionContext arg1) throws ActionException {
		// run the query
		try {
			Resource subject = Resource.uriRef(arg0.getUri());
			Unifier u = new Unifier();
			u.setColumnNames("input");
			u.addPattern("o3","o3s",subject); // sq
			u.addPattern("o2",cet("workflow/datalist/hasData"),"o3");
			u.addPattern("o1","o1s","o2"); // seq
			u.addPattern("step",cet("workflow/step/hasOutput"),"o1");
			u.addPattern("step",cet("workflow/step/hasInput"),"i1");
			u.addPattern("i1","i1s","i2");
			u.addPattern("i2",cet("workflow/datalist/hasData"),"i3");
			u.addPattern("i3","i3s","input");
			//
			TupeloStore.getInstance().getContext().perform(u);
			List<DatasetBean> df = new LinkedList<DatasetBean>();
			for(Resource d : Tables.getColumn(u.getResult(),0)) {
				df.add(TupeloStore.fetchDataset(d)); // dbu's only take strings
			}
			return new GetDerivedFromResult(df);
		} catch(Exception x) {
			throw new ActionException("unable to find datasets "+arg0.getUri()+" was derived from");
		}
	}

	@Override
	public Class<GetDerivedFrom> getActionType() {
		// TODO Auto-generated method stub
		return GetDerivedFrom.class;
	}

	@Override
	public void rollback(GetDerivedFrom arg0, GetDerivedFromResult arg1,
			ExecutionContext arg2) throws ActionException {
		// TODO Auto-generated method stub
		
	}

}
