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
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetAllowedValues;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetAllowedValuesResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.NamedThing;

public class GetAllowedValuesHandler implements ActionHandler<GetAllowedValues, GetAllowedValuesResult> {

    @Override
    public GetAllowedValuesResult execute(GetAllowedValues action, ExecutionContext arg1) throws ActionException {
        Resource p = Resource.uriRef(action.getUri()); // this is the predicate, must be an owl:ObjectProperty
        Unifier u = new Unifier();
        u.setColumnNames("av", "l");
        u.addPattern(p, Rdfs.RANGE, "c");
        u.addPattern("av", Rdf.TYPE, "c");
        u.addPattern("av", Rdfs.LABEL, "l");
        try {
            getDemoContext().perform(u);
        } catch (OperatorException e) {
            throw new ActionException(e);
        }
        List<NamedThing> avs = new ArrayList<NamedThing>();
        for (Tuple<Resource> row : u.getResult() ) {
            avs.add(new NamedThing(row.get(0).getString(), row.get(1).getString()));
        }
        GetAllowedValuesResult result = new GetAllowedValuesResult();
        result.setAllowedValues(avs);
        return result;
    }

    private static Resource ns(String suffix) {
        return Resource.uriRef("http://foo.bar/baz#" + suffix);
    }

    private Context getDemoContext() throws OperatorException {
        MemoryContext c = new MemoryContext();
        c.addTriple(ns("contentValidity"), Rdfs.RANGE, ns("ContentValidity"));
        c.addTriple(ns("confirmed"), Rdf.TYPE, ns("ContentValidity"));
        c.addTriple(ns("probablyTrue"), Rdf.TYPE, ns("ContentValidity"));
        c.addTriple(ns("possiblyTrue"), Rdf.TYPE, ns("ContentValidity"));
        c.addTriple(ns("doubtfullyTrue"), Rdf.TYPE, ns("ContentValidity"));
        c.addTriple(ns("improbable"), Rdf.TYPE, ns("ContentValidity"));
        c.addTriple(ns("cannotBeAssessed"), Rdf.TYPE, ns("ContentValidity"));
        //
        c.addTriple(ns("contentValidity"), Rdfs.LABEL, "Content Validity");
        c.addTriple(ns("confirmed"), Rdfs.LABEL, "Confirmed");
        c.addTriple(ns("probablyTrue"), Rdfs.LABEL, "Probably True");
        c.addTriple(ns("possiblyTrue"), Rdfs.LABEL, "Possibly True");
        c.addTriple(ns("doubtfullyTrue"), Rdfs.LABEL, "Doubtfully True");
        c.addTriple(ns("improbable"), Rdfs.LABEL, "Improbable");
        c.addTriple(ns("cannotBeAssessed"), Rdfs.LABEL, "Cannot be Assessed");
        return c;
    }

    @Override
    public Class<GetAllowedValues> getActionType() {
        return GetAllowedValues.class;
    }

    @Override
    public void rollback(GetAllowedValues arg0, GetAllowedValuesResult arg1, ExecutionContext arg2) throws ActionException {
    }
}
