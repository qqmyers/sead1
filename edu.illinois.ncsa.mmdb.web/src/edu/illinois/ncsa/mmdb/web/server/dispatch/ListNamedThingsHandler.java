package edu.illinois.ncsa.mmdb.web.server.dispatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.mmdb.web.client.dispatch.ListNamedThingsResult;
import edu.illinois.ncsa.mmdb.web.server.Memoized;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

public abstract class ListNamedThingsHandler {
    private final Log                       log     = LogFactory.getLog(ListNamedThingsHandler.class);

    protected boolean                       memoize = true;                                           // should we memoize?
    protected long                          ttl     = 60 * 1000;                                      // what ttl in ms

    private Memoized<ListNamedThingsResult> cache;

    protected ListNamedThingsResult listNamedThings(final Resource typeUri, final Resource labelPredicate) {
        if (cache == null) {
            cache = new Memoized<ListNamedThingsResult>() {
                public ListNamedThingsResult computeValue() {
                    try {
                        ListNamedThingsResult r = new ListNamedThingsResult();
                        r.setThingNames(TupeloStore.getInstance().listThingsOfType(typeUri, labelPredicate));
                        return r;
                    } catch (OperatorException e) {
                        log.error("query to fetch user metadata fields failed");
                        return null;
                    }
                }
            };
            cache.setTtl(ttl); // 1min
        }
        return cache.getValue();
    }
}
