package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.HashMap;
import java.util.Map;

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

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetRelationship;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetRelationshipResult;
import edu.illinois.ncsa.mmdb.web.client.ui.Relationship;
import edu.illinois.ncsa.mmdb.web.server.SEADRbac;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

public class GetRelationshipHandlerNew implements ActionHandler<GetRelationship, GetRelationshipResult> {

    public static final Resource VIEW_RELATIONSHIP = Resource.uriRef("http://sead-data.net/terms/acr/Viewable_Relationship");

    static Log                   log               = LogFactory.getLog(GetRelationshipHandlerNew.class);

    @Override
    public GetRelationshipResult execute(GetRelationship action, ExecutionContext arg1) throws ActionException {
        try {
            Resource subject = Resource.uriRef(action.getDatasetURI());

            Unifier u = new Unifier();
            u.setColumnNames("label", "dataset", "type");
            u.addPattern("type", Rdf.TYPE, VIEW_RELATIONSHIP); // only fetch current/past user relationship triples
            u.addPattern(subject, "type", "dataset"); // determine the target dataset uri from the relationship triple
            u.addPattern("type", Rdfs.LABEL, "label");
            // don't fetch the reified stuff (the date and creator of the relationship) because these are not returned by this dispatch

            Map<String, Relationship> dataset = new HashMap<String, Relationship>();

            SEADRbac rbac = new SEADRbac(TupeloStore.getInstance().getContext());
            for (Tuple<Resource> row : TupeloStore.getInstance().unifyExcludeDeleted(u, "dataset") ) {
                if (!rbac.checkAccessLevel(Resource.uriRef(action.getUser()), row.get(1))) {
                    continue;
                }
                DatasetBean db = TupeloStore.fetchDataset(row.get(1)); // dbu's only take strings
                String label = row.get(0).getString();
                String type = row.get(2).getString();
                //update datasets in the specific type
                if (dataset.containsKey(type)) {
                    Relationship rel = dataset.get(type);
                    dataset.get(type).datasets.add(db);
                    dataset.get(type).uris.add(db.getUri());
                    dataset.put(type, rel);

                } else {
                    Relationship result = new Relationship();
                    result.datasets.add(db);
                    result.uris.add(db.getUri());
                    result.typeLabel = label;
                    dataset.put(type, result);
                }
            }

            return new GetRelationshipResult(dataset);
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
