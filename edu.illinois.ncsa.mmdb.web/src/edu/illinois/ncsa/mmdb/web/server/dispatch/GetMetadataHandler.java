/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetMetadata;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetMetadataResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;

/**
 * Retrieve generic metadata about a resource.
 * 
 * @author Luigi Marini
 * @author Rob Kooper
 * 
 */
public class GetMetadataHandler implements
		ActionHandler<GetMetadata, GetMetadataResult> {

	/** Commons logging **/
	private static Log log = LogFactory.getLog(GetMetadataHandler.class);

	@Override
	public GetMetadataResult execute(GetMetadata action, ExecutionContext arg1)
			throws ActionException {

		Resource uri = Resource.resource(action.getUri());

		GetMetadataResult result = new GetMetadataResult();

		Unifier uf = new Unifier();
		uf.addPattern(uri, "predicate", "value"); //$NON-NLS-1$ //$NON-NLS-2$
		uf.addPattern("predicate", Rdf.TYPE, MMDB.METADATA_TYPE); //$NON-NLS-1$
		uf.addPattern("predicate", MMDB.METADATA_CATEGORY, "category"); //$NON-NLS-1$ //$NON-NLS-2$
		uf.addPattern("predicate", Rdfs.LABEL, "label"); //$NON-NLS-1$ //$NON-NLS-2$
		uf.setColumnNames("label", "value", "category"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		try {
			TupeloStore.getInstance().getContext().perform(uf);

			for (Tuple<Resource> row : uf.getResult()) {
				if (row.get(0) != null) {
					result.add(row.get(2).getString(), row.get(0).getString(),
							row.get(1).getString());
				}
			}
		} catch (OperatorException e1) {
			log.error("Error getting metadata for " + action.getUri(), e1);
			e1.printStackTrace();
		}

		return result;
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
