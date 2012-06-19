package edu.illinois.ncsa.mmdb.web.server.resteasy;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

/**
 * Trigger a reindex of the lucene index.
 * 
 * @author Rob Kooper
 * 
 */
public class UpdateLuceneRestService {
    @GET
    @Path("/search/reindex")
    public int reindex() {
        return TupeloStore.getInstance().indexFullTextAll();
    }
}
