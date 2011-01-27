package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetSubclasses;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetSubclassesResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.NamedThing;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

public class GetSubclassesHandler implements ActionHandler<GetSubclasses, GetSubclassesResult> {

    @Override
    public GetSubclassesResult execute(GetSubclasses action, ExecutionContext arg1) throws ActionException {
        GetSubclassesResult result = new GetSubclassesResult();
        Resource clazz = Resource.uriRef(action.getUri());
        Unifier u = new Unifier();
        u.setColumnNames("sc", "label");
        u.addPattern("sc", Rdfs.SUB_CLASS_OF, clazz);
        u.addPattern("sc", Rdfs.LABEL, "label");
        List<NamedThing> subs = new ArrayList<NamedThing>();
        try {
            TupeloStore.getInstance().getOntologyContext().perform(u);
            for (Tuple<Resource> row : u.getResult() ) {
                subs.add(new NamedThing(row.get(0).getString(), row.get(1).getString()));
            }
        } catch (OperatorException e) {
            throw new ActionException(e);
        }
        result.setSubclasses(subs);
        return result;
    }

    @Override
    public Class<GetSubclasses> getActionType() {
        return GetSubclasses.class;
    }

    @Override
    public void rollback(GetSubclasses arg0, GetSubclassesResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub
    }
}
