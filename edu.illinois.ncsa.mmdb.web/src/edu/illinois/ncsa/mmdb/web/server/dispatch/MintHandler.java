package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.mmdb.web.client.dispatch.Mint;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MintResult;

public class MintHandler implements ActionHandler<Mint, MintResult> {

    @Override
    public MintResult execute(Mint arg0, ExecutionContext arg1) throws ActionException {
        Resource uriRef = Resource.uriRef();
        return new MintResult(uriRef.getString()); // FIXME generalize Medici-wide minter
    }

    @Override
    public Class<Mint> getActionType() {
        return Mint.class;
    }

    @Override
    public void rollback(Mint arg0, MintResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
