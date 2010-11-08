package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.tupeloproject.rdf.terms.Rdfs;

import edu.illinois.ncsa.mmdb.web.client.dispatch.ListNamedThingsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListRelationshipTypes;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;

public class ListRelationshipTypesHandler extends ListNamedThingsHandler implements ActionHandler<ListRelationshipTypes, ListNamedThingsResult> {

    @Override
    public ListNamedThingsResult execute(ListRelationshipTypes arg0, ExecutionContext arg1) throws ActionException {
        ListNamedThingsResult result = listNamedThings(MMDB.USER_RELATIONSHIP, Rdfs.LABEL);
        if (result == null) {
            throw new ActionException("error listing relationship types");
        } else {
            return result;
        }
    }

    @Override
    public Class<ListRelationshipTypes> getActionType() {
        return ListRelationshipTypes.class;
    }

    @Override
    public void rollback(ListRelationshipTypes arg0, ListNamedThingsResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
