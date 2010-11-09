package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Transformer;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.rdf.Namespaces;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.xml.RdfXml;

import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteRelationship;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetRelationshipResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;

public class DeleteRelationshipHandler implements ActionHandler<DeleteRelationship, SetRelationshipResult> {

    @Override
    public SetRelationshipResult execute(DeleteRelationship action, ExecutionContext arg1) throws ActionException {
        // Here we interpret the "type" field of the action as the URI of the predicate to set
        try {
            Context c = TupeloStore.getInstance().getContext();
            //
            // we're calling new URI below, to validate that these are all URI's.
            Resource source = Resource.uriRef(new URI(action.getUri1()));
            Resource target = Resource.uriRef(new URI(action.getUri2()));
            Resource predicate = Resource.uriRef(new URI(action.getType()));
            Resource INVERSE_OF = Resource.uriRef(Namespaces.owl("inverseOf"));
            //
            // find all relationship triples of this sort
            Transformer t = new Transformer();
            t.addInPattern(predicate, Rdf.TYPE, MMDB.USER_RELATIONSHIP); // make sure this isn't some random other predicate
            t.addInPattern(predicate, INVERSE_OF, "inverse");
            t.addInPattern(source, predicate, target);
            t.addInPattern(target, "inverse", source);
            t.addInPattern("S1", Rdf.PREDICATE, predicate);
            t.addInPattern("S1", Rdf.TYPE, Rdf.STATEMENT);
            t.addInPattern("S1", Rdf.SUBJECT, source);
            t.addInPattern("S1", Rdf.OBJECT, target);
            t.addInPattern("S1", Dc.DATE, "date");
            t.addInPattern("S1", Dc.CREATOR, "creator");
            t.addInPattern("S2", Rdf.PREDICATE, "inverse");
            t.addInPattern("S2", Rdf.TYPE, Rdf.STATEMENT);
            t.addInPattern("S2", Rdf.SUBJECT, target);
            t.addInPattern("S2", Rdf.OBJECT, source);
            t.addInPattern("S2", Dc.DATE, "date2");
            t.addInPattern("S2", Dc.CREATOR, "creator2");
            //
            t.addOutPattern(target, "inverse", source);
            t.addOutPattern("S1", Rdf.PREDICATE, predicate);
            t.addOutPattern("S1", Rdf.TYPE, Rdf.STATEMENT);
            t.addOutPattern("S1", Rdf.SUBJECT, source);
            t.addOutPattern("S1", Rdf.OBJECT, target);
            t.addOutPattern("S1", Dc.DATE, "date");
            t.addOutPattern("S1", Dc.CREATOR, "creator");
            t.addOutPattern("S2", Rdf.SUBJECT, target);
            t.addOutPattern("S2", Rdf.PREDICATE, "inverse");
            t.addOutPattern("S2", Rdf.TYPE, Rdf.STATEMENT);
            t.addOutPattern("S2", Rdf.OBJECT, source);
            t.addOutPattern("S2", Dc.DATE, "date2");
            t.addOutPattern("S2", Dc.CREATOR, "creator2");
            //
            c.perform(t);
            Set<Triple> toast = t.getResult();
            try {
                RdfXml.write(toast, System.out);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } // FIXME debug;
            TripleWriter tw = new TripleWriter();
            tw.removeAll(toast);
            c.perform(tw);
        } catch (OperatorException x) {
            throw new ActionException("delete relationship failed", x);
        } catch (URISyntaxException e) {
            throw new ActionException("delete relationship failed", e);
        }
        return new SetRelationshipResult();
    }

    @Override
    public Class<DeleteRelationship> getActionType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void rollback(DeleteRelationship arg0, SetRelationshipResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
