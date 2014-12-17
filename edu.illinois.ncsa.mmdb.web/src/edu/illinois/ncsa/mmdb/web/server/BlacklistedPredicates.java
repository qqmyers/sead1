package edu.illinois.ncsa.mmdb.web.server;

import java.util.HashSet;
import java.util.Set;

import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.Rdfs;

public final class BlacklistedPredicates {
    private BlacklistedPredicates() {
    }

    private static Set<Resource> blacklistedPredicates = new HashSet<Resource>();

    public static Set<Resource> GetResources() {
        if (blacklistedPredicates.isEmpty()) {
            // there's an even longer list, but these are some of the ones I expect we'd have the most problems with
            blacklistedPredicates.add(Resource.uriRef("http://purl.org/dc/terms/license"));
            blacklistedPredicates.add(Resource.uriRef("http://purl.org/dc/terms/rightsHolder"));
            blacklistedPredicates.add(Resource.uriRef("http://purl.org/dc/terms/rights"));
            blacklistedPredicates.add(Dc.TITLE);
            blacklistedPredicates.add(Dc.CREATOR);
            blacklistedPredicates.add(Dc.IDENTIFIER);
            blacklistedPredicates.add(Dc.CONTRIBUTOR); // should whitelist once we have multi-valued user properties
            blacklistedPredicates.add(Rdfs.LABEL);

        }
        return blacklistedPredicates;
    }

}
