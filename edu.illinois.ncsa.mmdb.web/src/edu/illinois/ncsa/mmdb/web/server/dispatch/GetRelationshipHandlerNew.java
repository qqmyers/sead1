package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.LinkedList;
import java.util.List;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetRelationship;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetRelationshipResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;

public class GetRelationshipHandlerNew implements ActionHandler<GetRelationship, GetRelationshipResult> {

    @Override
    public GetRelationshipResult execute(GetRelationship action, ExecutionContext arg1) throws ActionException {
        try {
            Resource subject = Resource.uriRef(action.getDatasetURI());

            Unifier u = new Unifier();
            u.setColumnNames("label", "dataset");
            u.addPattern("type", Rdf.TYPE, MMDB.USER_RELATIONSHIP); // only fetch user relationship triples
            u.addPattern(subject, "type", "dataset"); // determine the target dataset uri from the relationship triple
            u.addPattern("type", Rdfs.LABEL, "label");
            // don't fetch the reified stuff (the date and creator of the relationship) because these are not returned by this dispatch

            TupeloStore.getInstance().getContext().perform(u);

            List<DatasetBean> rt = new LinkedList<DatasetBean>();
            List<String> types = new LinkedList<String>();

            for (Tuple<Resource> row : u.getResult() ) {
                DatasetBean db = TupeloStore.fetchDataset(row.get(1)); // dbu's only take strings
                String type = row.get(0).getString();
                rt.add(db);
                types.add(type);
            }

            return new GetRelationshipResult(rt, types);
        } catch (OperatorException x) {
            throw new ActionException("get relationships failed", x);
        }
    }

    @Override
    public Class<GetRelationship> getActionType() {
        return GetRelationship.class;
    }

    @Override
    public void rollback(GetRelationship arg0, GetRelationshipResult arg1, ExecutionContext arg2) throws ActionException {
    }
}
