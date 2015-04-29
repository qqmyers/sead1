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
import org.tupeloproject.rdf.terms.Rdfs;

import edu.illinois.ncsa.mmdb.web.client.dispatch.MetadataTermResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.UpdateMetadata;
import edu.illinois.ncsa.mmdb.web.server.BlacklistedPredicates;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.illinois.ncsa.mmdb.web.server.resteasy.ItemServicesImpl;

public class UpdateMetadataHandler implements ActionHandler<UpdateMetadata, MetadataTermResult> {
    private static Log log = LogFactory.getLog(AddMetadataHandler.class);

    @Override
    public MetadataTermResult execute(UpdateMetadata action, ExecutionContext arg1) throws ActionException {
        Resource uri = Resource.resource(action.getUri());
        MetadataTermResult result = new MetadataTermResult();
        Set<Resource> blacklistedPredicates = BlacklistedPredicates.GetResources();
        if (blacklistedPredicates.contains(uri)) {
            throw new ActionException("Cannot update a blacklisted Predicate.");
        }
        if (ItemServicesImpl.getReservedLabels().contains(action.getLabel())) {
            throw new ActionException("Cannot update to use a reserved label.");
        }

        TripleWriter tw = new TripleWriter();
        String label = action.getLabel();
        String description = action.getDescription();
        try {
            Context context = TupeloStore.getInstance().getContext();

            // remove existing label
            context.removeTriples(context.match(uri, Rdfs.LABEL, null));
            tw.add(uri, Rdfs.LABEL, label);

            // remove existing definition
            context.removeTriples(context.match(uri, Rdfs.COMMENT, null));
            tw.add(uri, Rdfs.COMMENT, description);

            context.perform(tw);
            ListUserMetadataFieldsHandler.resetCache();
        } catch (OperatorException exc) {
            log.warn("Could not update metadata userfields.", exc);
        }
        return result;

    }

    @Override
    public Class<UpdateMetadata> getActionType() {
        return UpdateMetadata.class;
    }

    @Override
    public void rollback(UpdateMetadata arg0, MetadataTermResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
