package edu.illinois.ncsa.mmdb.web.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.terms.Dc;

import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBAC;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;

// static utilities for making access control decisions
public class AccessControl {
    static Log log = LogFactory.getLog(AccessControl.class);

    /** Is this person a dc:creator of this dataset? */
    public static boolean isCreator(String personUri, DatasetBean dataset) {
        if (personUri == null || dataset == null) {
            log.warn("isCreator called with null value");
            return false;
        }
        return personUri.equals(dataset.getCreator().getUri());
    }

    /** Is this person a dc:creator of this resource? */
    public static boolean isCreator(String personUri, String resourceUri) {
        if (personUri == null || resourceUri == null) {
            log.warn("isCreator called with null value");
            return false;
        }
        try {
            for (Triple t : TupeloStore.getInstance().getContext().match(Resource.uriRef(resourceUri), Dc.CREATOR, null) ) {
                if (t.getObject().getString().equals(personUri)) {
                    return true;
                }
            }
            return false;
        } catch (OperatorException x) {
            log.error("unable to determine ownership", x);
            return false;
        }
    }

    public static boolean isAdmin(String personUri) {
        if (personUri == null) {
            log.warn("isAdmin called with null value");
            return false;
        }
        RBAC rbac = new RBAC(TupeloStore.getInstance().getContext());
        try {
            return rbac.checkPermission(Resource.uriRef(personUri), MMDB.VIEW_ADMIN_PAGES);
        } catch (RBACException e) {
            log.error("unable to check if user is admin", e);
            return false;
        }
    }
}
