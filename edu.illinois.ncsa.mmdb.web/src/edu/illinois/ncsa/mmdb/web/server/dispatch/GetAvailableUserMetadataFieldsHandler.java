package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetAvailableUserMetadataFields;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetAvailableUserMetadataFieldsResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

public class GetAvailableUserMetadataFieldsHandler implements ActionHandler<GetAvailableUserMetadataFields, GetAvailableUserMetadataFieldsResult> {

    @Override
    public GetAvailableUserMetadataFieldsResult execute(GetAvailableUserMetadataFields arg0, ExecutionContext arg1) throws ActionException {
        Unifier u = new Unifier();
        u.setColumnNames("label", "field");
        u.addPattern("field", Rdf.TYPE, Cet.cet("userMetadataField"));
        u.addPattern("field", Rdfs.LABEL, "label");
        GetAvailableUserMetadataFieldsResult r = new GetAvailableUserMetadataFieldsResult();
        try {
            for (Tuple<Resource> row : TupeloStore.getInstance().unifyExcludeDeleted(u, "field") ) {
                r.addField(row.get(0).getString(), row.get(1).getString());
            }
        } catch (OperatorException e) {
            e.printStackTrace();
        }
        return r;
    }

    @Override
    public Class<GetAvailableUserMetadataFields> getActionType() {
        // TODO Auto-generated method stub
        return GetAvailableUserMetadataFields.class;
    }

    @Override
    public void rollback(GetAvailableUserMetadataFields arg0, GetAvailableUserMetadataFieldsResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
