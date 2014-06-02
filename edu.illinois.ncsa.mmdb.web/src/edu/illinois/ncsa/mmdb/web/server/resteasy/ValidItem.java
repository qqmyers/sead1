package edu.illinois.ncsa.mmdb.web.server.resteasy;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Table;

import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

public class ValidItem {

    private boolean  valid = false;
    private Response r;

    public ValidItem(UriRef itemId, Resource itemType, UriRef userId) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        Unifier uf = new Unifier();

        uf.addPattern("item", Rdf.TYPE, itemType);
        uf.addPattern("item", Dc.IDENTIFIER, itemId);
        try {
            Table<Resource> uResult = TupeloStore.getInstance().unifyExcludeDeleted(uf, "item");

            if (uResult.iterator().hasNext()) {
                if (ItemServicesImpl.isAccessible(userId, itemId)) {
                    valid = true;
                } else {
                    result.put("Error", itemId.toString() + " not accessible");
                    r = Response.status(403).entity(result).build();
                }

            } else {
                result.put("Error", itemId.toString() + " not found.");
                r = Response.status(404).entity(result).build();
            }
        } catch (OperatorException e) {
            result.put("Error", "Server error");
            r = Response.status(500).entity(result).build();
        }
    }

    public boolean isValid() {
        return valid;
    }

    public Response getErrorResponse() {
        return r;
    }
}
