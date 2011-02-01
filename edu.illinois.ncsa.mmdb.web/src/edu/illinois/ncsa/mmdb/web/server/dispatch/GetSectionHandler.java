package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetSection;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetSectionResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.Section;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;

public class GetSectionHandler implements ActionHandler<GetSection, GetSectionResult> {

    @Override
    public GetSectionResult execute(GetSection action, ExecutionContext arg1) throws ActionException {
        Resource datasetUri = Resource.uriRef(action.getUri());
        GetSectionResult r = new GetSectionResult();
        if (action.getMarker() == null) {
            Section s = new Section();
            s.setDatasetUri(datasetUri.getString());
            s.setUri(datasetUri.getString());
            s.setName(null);
            r.addSection(s);
        } else {
            Unifier u = new Unifier();
            u.addPattern(datasetUri, MMDB.METADATA_HASSECTION, "section");
            u.addPattern("section", MMDB.SECTION_MARKER, Resource.literal(action.getMarker()));
            u.setColumnNames("section");
            try {
                for (Tuple<Resource> row : TupeloStore.getInstance().unifyExcludeDeleted(u, "section") ) {
                    Resource sectionUri = row.get(0);
                    Section s = new Section();
                    s.setDatasetUri(datasetUri.getString());
                    s.setUri(sectionUri.getString());
                    s.setName(action.getMarker());
                    r.addSection(s);
                }
                if (r.getSections().size() == 0) {
                    throw new ActionException("unrecognized section marker " + action.getMarker());
                }
            } catch (OperatorException e) {
                e.printStackTrace();
            }
        }
        return r;
    }

    @Override
    public Class<GetSection> getActionType() {
        return GetSection.class;
    }

    @Override
    public void rollback(GetSection arg0, GetSectionResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
