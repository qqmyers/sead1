package edu.illinois.ncsa.mmdb.web.server.rest;

import java.util.logging.Level;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.RoleAuthorizer;

import edu.uiuc.ncsa.cet.bean.rbac.medici.Permission;

public class MediciServerApplication extends Application {
    /**
     * When launched as a standalone application.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Component component = new Component();
        component.getClients().add(Protocol.FILE);
        component.getServers().add(Protocol.HTTP, 8080);
        component.getDefaultHost().attach(new MediciServerApplication());
        component.start();
    }

    @Override
    public Restlet createInboundRoot() {
        getLogger().setLevel(Level.WARNING);
        Router router = new Router(getContext());
        router.setDefaultMatchingMode(Template.MODE_STARTS_WITH);

        // Create a secure router, or guard
        Router secured = new Router(getContext());
        ChallengeAuthenticator guard = new ChallengeAuthenticator(getContext(), ChallengeScheme.HTTP_BASIC, "medici"); //$NON-NLS-1$
        guard.setVerifier(new MediciAuthentication());
        guard.setEnroler(new MediciAuthorizer());
        router.attach("/secure", guard); //$NON-NLS-1$
        guard.setNext(secured);

        // ----------------------------------------------------------------------
        // SECURE ROUTES
        // Any routes that are attached to secured will be checked to see if they
        // have a valid username/password or cookie.
        // ----------------------------------------------------------------------
        secured.attach("/pyramid/{dataset}/xml", ImagePyramidResource.class); //$NON-NLS-1$
        secured.attach("/pyramid/{dataset}/xml_files/{level}/{col}_{row}.{ext}", ImagePyramidTileResource.class); //$NON-NLS-1$

        // rerun lucene idex
        RoleAuthorizer ra = new RoleAuthorizer();
        ra.getAuthorizedRoles().add(MediciAuthorizer.getRole(Permission.REINDEX_FULLTEXT));
        ra.setNext(UpdateLuceneResource.class);
        secured.attach("/search/reindex", ra); //$NON-NLS-1$

        // ----------------------------------------------------------------------
        // UNSECURE ROUTES
        // Any routes that are atteched to router will be accepted and not
        // checked for valid username/password or cookie.
        // ----------------------------------------------------------------------

        // ----------------------------------------------------------------------
        // DONE
        // ----------------------------------------------------------------------
        return router;
    }
}
