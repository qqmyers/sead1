package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.kernel.impl.MemoryContext;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetSubclasses;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetSubclassesResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.NamedThing;

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
            //TupeloStore.getInstance().getContext().perform(u);
            getDemoContext().perform(u);
            for (Tuple<Resource> row : u.getResult() ) {
                subs.add(new NamedThing(row.get(0).getString(), row.get(1).getString()));
            }
        } catch (OperatorException e) {
            throw new ActionException(e);
        }
        result.setSubclasses(subs);
        return result;
    }

    private Resource ns(String suffix) {
        return Resource.uriRef("http://demo.org/ns#" + suffix);
    }

    private Context getDemoContext() throws OperatorException {
        MemoryContext c = new MemoryContext();
        c.addTriple(ns("Animal"), Rdfs.LABEL, "Animal");
        c.addTriple(ns("Mammal"), Rdfs.LABEL, "Mammal");
        c.addTriple(ns("Dog"), Rdfs.LABEL, "Dog");
        c.addTriple(ns("Cat"), Rdfs.LABEL, "Cat");
        c.addTriple(ns("Reptile"), Rdfs.LABEL, "Reptile");
        c.addTriple(ns("Lizard"), Rdfs.LABEL, "Lizard");
        c.addTriple(ns("Turtle"), Rdfs.LABEL, "Turtle");
        c.addTriple(ns("Mammal"), Rdfs.SUB_CLASS_OF, ns("Animal"));
        c.addTriple(ns("Dog"), Rdfs.SUB_CLASS_OF, ns("Mammal"));
        c.addTriple(ns("Cat"), Rdfs.SUB_CLASS_OF, ns("Mammal"));
        c.addTriple(ns("Reptile"), Rdfs.SUB_CLASS_OF, ns("Animal"));
        c.addTriple(ns("Lizard"), Rdfs.SUB_CLASS_OF, ns("Reptile"));
        c.addTriple(ns("Turtle"), Rdfs.SUB_CLASS_OF, ns("Reptile"));
        return c;
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
