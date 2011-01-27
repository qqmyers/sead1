package edu.illinois.ncsa.mmdb.web.server.rest;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

/**
 * Trigger a reindex of the lucene index.
 * 
 * @author Rob Kooper
 * 
 */
public class UpdateLuceneResource extends ServerResource {
    @Get
    public int reindex() {
        return TupeloStore.getInstance().indexFullTextAll();
    }
}
