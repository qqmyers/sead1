/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.ArrayList;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetMetadata;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetMetadataResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;


/**
 * Retrieve generic metadata about a resource.
 * 
 * @author Luigi Marini
 *
 */
public class GetMetadataHandler implements ActionHandler<GetMetadata, GetMetadataResult>{

	private static final String HAS_METADATA = "http://cet.ncsa.uiuc.edu/2007/hasMetaData";

	@Override
	public GetMetadataResult execute(GetMetadata arg0, ExecutionContext arg1)
			throws ActionException {
		
		Resource uri = Resource.resource(arg0.getUri());
		
		ArrayList<ArrayList<String>> metadata = new ArrayList<ArrayList<String>>();
		
		Unifier uf = new Unifier();
		uf.addPattern(uri, HAS_METADATA, "metadata");
		uf.addPattern("metadata",Rdfs.LABEL, "label");
		uf.addPattern("metadata", Dc.DESCRIPTION, "value");
		uf.setColumnNames("label", "value");

		try {
			TupeloStore.getInstance().getContext().perform(uf);

			for (Tuple<Resource> row : uf.getResult()) {
				if (row.get(0) != null) {
					ArrayList<String> tuple = new ArrayList<String>();
					tuple.add(row.get(0).getString());
					tuple.add(row.get(1).getString());
					metadata.add(tuple);
				}
			}
		} catch (OperatorException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return new GetMetadataResult(metadata);
	}

	@Override
	public Class<GetMetadata> getActionType() {
		return GetMetadata.class;
	}

	@Override
	public void rollback(GetMetadata arg0, GetMetadataResult arg1,
			ExecutionContext arg2) throws ActionException {
		// TODO Auto-generated method stub
		
	}
	
}
