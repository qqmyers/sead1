package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.rdf.Namespaces;
import org.tupeloproject.rdf.Reification;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.SetRelationship;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetRelationshipResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

public class SetRelationshipHandlerNew implements ActionHandler<SetRelationship, SetRelationshipResult> {

    @Override
    public SetRelationshipResult execute(SetRelationship action, ExecutionContext exc) throws ActionException {
        // Here we interpret the "type" field of the action as the URI of the predicate to set
        try {
            Context c = TupeloStore.getInstance().getContext();
            //
            // we're calling new URI below, to validate that these are all URI's.
            Resource source = Resource.uriRef(new URI(action.getUri1()));
            Resource target = Resource.uriRef(new URI(action.getUri2()));
            Resource predicate = Resource.uriRef(new URI(action.getType()));
            Resource creator = Resource.uriRef(new URI(action.getCreator()));
            //
            // construct a relationship triple, and its inverse
            Resource inverse = null;
            for (Triple t : c.match(predicate, Resource.uriRef(Namespaces.owl("inverseOf")), null) ) {
                inverse = t.getObject();
            }
            if (inverse == null) {
                throw new ActionException("no inverse predicate found for relationship " + predicate);
            }
            TripleWriter tw = new TripleWriter();
            for (Triple relationshipTriple : new Tuple<Triple>(new Triple(source, predicate, target), new Triple(target, inverse, source)) ) {
                tw.add(relationshipTriple);
                Resource reified = Resource.uriRef();
                tw.addAll(Reification.reify(reified, relationshipTriple));
                tw.add(reified, Dc.CREATOR, creator);
                tw.add(reified, Dc.DATE, new Date());
            }

            c.perform(tw);
        } catch (OperatorException x) {
            throw new ActionException("set relationship failed", x);
        } catch (URISyntaxException e) {
            throw new ActionException("set relationship failed", e);
        }
        return new SetRelationshipResult();
    }

    @Override
    public Class<SetRelationship> getActionType() {
        return SetRelationship.class;
    }

    @Override
    public void rollback(SetRelationship arg0, SetRelationshipResult arg1, ExecutionContext arg2) throws ActionException {
    }
}
