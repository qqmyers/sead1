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
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.ListUserMetadataFields;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListUserMetadataFieldsResult;
import edu.illinois.ncsa.mmdb.web.server.Memoized;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

public class ListUserMetadataFieldsHandler implements ActionHandler<ListUserMetadataFields, ListUserMetadataFieldsResult> {
    private static Log                           log = LogFactory.getLog(ListUserMetadataFieldsHandler.class);

    private static Memoized<Map<String, String>> cache;                                                       // key = uri, value = label

    public static Map<String, String> listUserMetadataFields() {
        if (cache == null) {
            cache = new Memoized<Map<String, String>>() {
                public Map<String, String> computeValue() {
                    // query for all the user metadata fields and their labels
                    Unifier u = new Unifier();
                    u.setColumnNames("field", "label");
                    u.addPattern("field", Rdf.TYPE, Cet.cet("userMetadataField"));
                    u.addPattern("field", Rdfs.LABEL, "label");
                    Map<String, String> result = new HashMap<String, String>();
                    try {
                        for (Tuple<Resource> row : TupeloStore.getInstance()
                                .unifyExcludeDeleted(u, "field") ) {
                            result.put(row.get(0).getString(), row.get(1).getString());
                        }
                    } catch (OperatorException e) {
                        log.error("query to fetch user metadata fields failed");
                        return null;
                    }
                    return result;
                }
            };
            cache.setTtl(60 * 1000); // 1min
        }
        return cache.getValue();
    }

    @Override
    public ListUserMetadataFieldsResult execute(ListUserMetadataFields arg0, ExecutionContext arg1) throws ActionException {
        Map<String, String> umf = listUserMetadataFields();
        if (umf == null) {
            throw new ActionException("query to fetch user metadata fields failed");
        } else {
            ListUserMetadataFieldsResult value = new ListUserMetadataFieldsResult();
            value.setFieldLabels(umf);
            return value;
        }
    }

    @Override
    public Class<ListUserMetadataFields> getActionType() {
        return ListUserMetadataFields.class;
    }

    @Override
    public void rollback(ListUserMetadataFields arg0, ListUserMetadataFieldsResult arg1, ExecutionContext arg2) throws ActionException {
    }

}
