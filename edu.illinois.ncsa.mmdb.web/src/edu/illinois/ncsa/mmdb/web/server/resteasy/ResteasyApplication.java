/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.resteasy;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

/**
 * @author Luigi Marini <lmarini@ncsa.illinois.edu>
 * 
 */
public class ResteasyApplication extends Application {

    private final Set<Object> singletons = new HashSet<Object>();

    public ResteasyApplication() {
        singletons.add(new TagsRestService());
        singletons.add(new CollectionsRestService());
        singletons.add(new DatasetsRestService());
        singletons.add(new SparqlRestService());
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

}
