package edu.illinois.ncsa.mmdb.web.server.resteasy;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Table;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

public class ValidItem {

    private boolean    valid = false;
    private Response   r;

    /** Commons logging **/
    private static Log log   = LogFactory.getLog(ValidItem.class);

    public ValidItem(UriRef itemId, Resource itemType, UriRef userId) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        Unifier uf = new Unifier();
        uf.addPattern(itemId, Dc.IDENTIFIER, "id");
        uf.addPattern(itemId, Rdf.TYPE, itemType);
        uf.addPattern(itemId, Resource.uriRef("http://purl.org/dc/terms/isReplacedBy"), "_ued", true);
        uf.setColumnNames("id", "_ued");
        try {
            TupeloStore.getInstance().getContext().perform(uf);
            Table<Resource> uResult = uf.getResult();
            boolean idMatch = false;
            boolean deleted = true;
            Iterator<Tuple<Resource>> it = uResult.iterator();
            if (it.hasNext()) {
                log.debug("Found item");
                Tuple<Resource> tu = it.next();
                String id = tu.get(0).toString();
                if (id.equals(itemId.toString())) {
                    log.debug("ID matches");
                    idMatch = true;
                } else {
                    log.debug("Found: " + id + ", expected: " + itemId.toString());
                }
                if (tu.get(1) == null) {
                    deleted = false;
                }
            }
            if (idMatch && !deleted) {
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
