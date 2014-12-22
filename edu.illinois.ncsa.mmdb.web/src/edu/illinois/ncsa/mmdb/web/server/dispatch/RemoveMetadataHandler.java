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

import edu.illinois.ncsa.mmdb.web.client.dispatch.MetadataTermResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.RemoveMetadata;
import edu.illinois.ncsa.mmdb.web.server.BlacklistedPredicates;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;

public class RemoveMetadataHandler implements ActionHandler<RemoveMetadata, MetadataTermResult> {
    private static Log log = LogFactory.getLog(RemoveMetadataHandler.class);

    @Override
    public MetadataTermResult execute(RemoveMetadata action, ExecutionContext arg1) throws ActionException {
        Resource uri = Resource.resource(action.getUri());
        MetadataTermResult result = new MetadataTermResult();
        Set<Resource> blacklistedPredicates = BlacklistedPredicates.GetResources();
        if (blacklistedPredicates.contains(uri)) {
            throw new ActionException("Cannot remove a blacklisted Predicate.");
        }

        TripleWriter tw = new TripleWriter();
        try {
            Context context = TupeloStore.getInstance().getContext();
            tw.remove(uri, Rdf.TYPE, MMDB.USER_METADATA_FIELD); //$NON-NLS-1$
            tw.add(uri, Rdf.TYPE, GetUserMetadataFieldsHandler.VIEW_METADATA); //$NON-NLS-1$

            context.perform(tw);
            ListUserMetadataFieldsHandler.resetCache();
        } catch (OperatorException exc) {
            log.warn("Could not update metadata userfields.", exc);
        }
        return result;

    }

    @Override
    public Class<RemoveMetadata> getActionType() {
        return RemoveMetadata.class;
    }

    @Override
    public void rollback(RemoveMetadata arg0, MetadataTermResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
