package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.Set;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;

import edu.illinois.ncsa.mmdb.web.client.dispatch.AddMetadata;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MetadataTermResult;
import edu.illinois.ncsa.mmdb.web.server.BlacklistedPredicates;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;

public class AddMetadataHandler implements ActionHandler<AddMetadata, MetadataTermResult> {
    private static Log log = LogFactory.getLog(AddMetadataHandler.class);

    @Override
    public MetadataTermResult execute(AddMetadata action, ExecutionContext arg1) throws ActionException {
        Resource uri = Resource.uriRef(action.getUri());
        String label = action.getLabel();
        String description = action.getDescription();
        MetadataTermResult result = new MetadataTermResult();
        TripleWriter tw = new TripleWriter();

        Set<Resource> blacklistedPredicates = BlacklistedPredicates.GetResources();

        try {
            Context context = TupeloStore.getInstance().getContext();

            tw.add(uri, Rdf.TYPE, MMDB.USER_METADATA_FIELD); //$NON-NLS-1$
            tw.add(uri, Rdf.TYPE, GetUserMetadataFieldsHandler.VIEW_METADATA); //$NON-NLS-1$

            // remove existing label
            context.removeTriples(context.match(uri, Rdfs.LABEL, null));
            tw.add(uri, Rdfs.LABEL, label);

            // remove existing definition
            context.removeTriples(context.match(uri, Rdfs.COMMENT, null));
            tw.add(uri, Rdfs.COMMENT, description);

            // remove blacklisted predicates
            for (Resource blacklisted : blacklistedPredicates ) {
                tw.remove(blacklisted, Rdf.TYPE, MMDB.USER_METADATA_FIELD);
                tw.remove(blacklisted, Rdf.TYPE, GetUserMetadataFieldsHandler.VIEW_METADATA);
            }
            context.perform(tw);
            ListUserMetadataFieldsHandler.resetCache();
        } catch (OperatorException exc) {
            log.warn("Could not add userfields.", exc);
        }
        return result;
    }

    @Override
    public Class<AddMetadata> getActionType() {
        return AddMetadata.class;
    }

    @Override
    public void rollback(AddMetadata arg0, MetadataTermResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
