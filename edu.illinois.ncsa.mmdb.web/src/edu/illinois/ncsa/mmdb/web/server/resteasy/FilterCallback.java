package edu.illinois.ncsa.mmdb.web.server.resteasy;

import org.tupeloproject.rdf.Resource;
import org.tupeloproject.util.Tuple;

public abstract class FilterCallback {

    public abstract boolean include(Tuple<Resource> t);

}
