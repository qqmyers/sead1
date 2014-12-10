package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.HashSet;
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
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;

import edu.illinois.ncsa.mmdb.web.client.dispatch.AddMetadata;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AddMetadataResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;

public class AddMetadataHandler implements ActionHandler<AddMetadata, AddMetadataResult> {
    private static Log log = LogFactory.getLog(AddMetadataHandler.class);

    @Override
    public AddMetadataResult execute(AddMetadata action, ExecutionContext arg1) throws ActionException {
        Resource uri = Resource.resource(action.getUri());
        String label = action.getLabel();
        String description = action.getDescription();
        AddMetadataResult result = new AddMetadataResult();
        TripleWriter tw = new TripleWriter();

        Set<Resource> blacklistedPredicates = new HashSet<Resource>();
        blacklistedPredicates.add(Resource.uriRef("http://purl.org/dc/terms/license"));
        blacklistedPredicates.add(Resource.uriRef("http://purl.org/dc/terms/rightsHolder"));
        blacklistedPredicates.add(Resource.uriRef("http://purl.org/dc/terms/rights"));
        blacklistedPredicates.add(Dc.TITLE);
        blacklistedPredicates.add(Dc.CREATOR);
        blacklistedPredicates.add(Dc.IDENTIFIER);
        blacklistedPredicates.add(Dc.CONTRIBUTOR); // should whitelist once we have multi-valued user properties
        blacklistedPredicates.add(Rdfs.LABEL);
        // there's an even longer list, but these are some of the ones I expect we'd have the most problems with

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
    public void rollback(AddMetadata arg0, AddMetadataResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
