package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import edu.illinois.ncsa.mmdb.web.client.dispatch.Mint;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MintResult;
import edu.illinois.ncsa.mmdb.web.rest.RestUriMinter;

public class MintHandler implements ActionHandler<Mint, MintResult> {

    @Override
    public MintResult execute(Mint arg0, ExecutionContext arg1) throws ActionException {
        String uriRef = RestUriMinter.getInstance().mintUri();
        return new MintResult(uriRef);
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
