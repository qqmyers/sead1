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
        singletons.add(new AuthenticationInterceptor());
        singletons.add(new UpdateLuceneRestService());
        singletons.add(new ImagePyramidRestService());
        singletons.add(new SysInfoRestService());
        singletons.add(new ItemServicesImpl());
        singletons.add(new URLRestService());
        singletons.add(new ResearchObjectsRestService());
        singletons.add(new PeopleRestService());
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

}
