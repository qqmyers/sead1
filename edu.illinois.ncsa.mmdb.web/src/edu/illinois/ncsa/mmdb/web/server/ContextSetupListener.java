/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010, NCSA.  All rights reserved.
 *
 * Developed by:
 * Cyberenvironments and Technologies (CET)
 * http://cet.ncsa.illinois.edu/
 *
 * National Center for Supercomputing Applications (NCSA)
 * http://www.ncsa.illinois.edu/
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the names of CET, University of Illinois/NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *******************************************************************************/
package edu.illinois.ncsa.mmdb.web.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.tupeloproject.kernel.ContentStoreContext;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.kernel.impl.HashFileContext;
import org.tupeloproject.kernel.impl.MemoryContext;
import org.tupeloproject.mysql.MysqlContext;
import org.tupeloproject.rdf.Namespaces;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.rdf.xml.RdfXml;

import edu.illinois.ncsa.cet.search.impl.LuceneTextIndex;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.server.search.SearchableThingIdGetter;
import edu.illinois.ncsa.mmdb.web.server.search.SearchableThingTextExtractor;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.rbac.medici.DefaultRole;
import edu.uiuc.ncsa.cet.bean.rbac.medici.Permission;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.context.ContextConvert;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.AuthenticationException;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.ContextAuthentication;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBAC;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.medici.MediciRbac;
import edu.uiuc.ncsa.cet.bean.tupelo.util.MimeMap;

/**
 * Create users and roles specified in the server.properties file.
 * 
 * FIXME: add roles to admin and guest account and not specific permissions
 * 
 * @author Luigi Marini, Rob Kooper
 * 
 */
public class ContextSetupListener implements ServletContextListener {
    private static Log  log   = LogFactory.getLog(ContextSetupListener.class);

    /** Timer to schedule re-occurring jobs */
    private final Timer timer = new Timer(true);

    /* (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        timer.cancel();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        // property file location
        Properties props = new Properties();
        String path = "/server.properties"; //$NON-NLS-1$
        log.debug("Loading server property file: " + path);

        // load properties
        InputStream input = null;
        try {
            input = TupeloStore.findFile(path).openStream();
            props.load(input);
        } catch (IOException exc) {
            log.warn("Could not load server.properties.", exc);
        } finally {
            try {
                input.close();
            } catch (IOException exc) {
                log.warn("Could not close server.properties.", exc);
            }
        }

        // create the context
        Context md = null;
        if ("mysql".equals(props.getProperty("context.type"))) {
            MysqlContext mc = new MysqlContext();
            mc.setUser(props.getProperty("mysql.user"));
            mc.setPassword(props.getProperty("mysql.password"));
            mc.setSchema(props.getProperty("mysql.schema"));
            mc.setHost(props.getProperty("mysql.host"));
            try {
                mc.connect();
                md = mc;
            } catch (SQLException e) {
                log.error("Could not connect to database.", e);
            }
        }
        if (md == null) {
            log.error("Could not connect to persistent storage, creating memory database.");
            md = new MemoryContext();
        }

        if (props.containsKey("hfc.path") && new File(props.getProperty("hfc.path")).isDirectory()) {
            HashFileContext hfc = new HashFileContext();
            hfc.setDepth(3);
            hfc.setDirectory(new File(props.getProperty("hfc.path")));
            ContentStoreContext csc = new ContentStoreContext();
            csc.setMetadataContext(md);
            csc.setDataContext(hfc);
            TupeloStore.createInstance(csc);
        } else {
            TupeloStore.createInstance(md);
        }

        // initialize default configurations
        try {
            TupeloStore.getInstance().initializeConfiguration(props);
        } catch (OperatorException exc) {
            log.warn("Could not read configuration values from context.", exc);
        }

        // make sure context is up to latest version
        // TODO remove? always false?
        try {
            ContextConvert.updateContext(TupeloStore.getInstance().getContext());
        } catch (OperatorException e) {
            log.warn("Could not update context.", e);
        }

        // initialize mime map (i.e. make sure all know mime types are in context.)
        try {
            MimeMap.initializeContext(TupeloStore.getInstance().getContext());
        } catch (OperatorException e) {
            log.warn("Could not initialize mimemap.", e);
        }

        // set mongo properties
        if (props.containsKey("mongo.host")) {
            String host = props.getProperty("mongo.host");
            String db = props.getProperty("mongo.database");
            String username = props.getProperty("mongo.username");
            String password = props.getProperty("mongo.password");
            TupeloStore.getInstance().setMongoDBProperties(host, db, username, password);
        }

        // set up full-text search
        try {
            setUpSearch();
        } catch (IOException e) {
            log.error("Error setting up lucene index", e);
        }

        // create accounts
        try {
            createAccounts(props);
        } catch (Exception e) {
            log.warn("Could not add accounts.", e);
        }

        // load taxonomy
        createUserFields(props);
        createRelationships(props);
        createOntology();

        // start the timers
        startTimers();
    }

    private void createOntology() {
        MemoryContext oc = new MemoryContext();
        TupeloStore.getInstance().setOntologyContext(oc);

        try {
            String filename = TupeloStore.getInstance().getConfiguration(ConfigurationKey.TaxonomyFile);
            if (filename == null) {
                return;
            }
            URL url = TupeloStore.findFile(filename);
            if (url == null) {
                return;
            }
            InputStream is = url.openStream();
            Set<Triple> triples = RdfXml.parse(is);
            oc.addTriples(triples);
            log.debug("Read " + triples.size() + " ontology triple(s)");
        } catch (FileNotFoundException e) {
            log.warn("no ontology found");
        } catch (IOException e) {
            log.error("Error: could not read ontology", e);
        } catch (OperatorException e) {
            log.error("problem ingesting ontology", e);
        } catch (Exception e) {
            log.error("problem ingesting ontology", e);
        }
    }

    private void startTimers() {
        // count datasets every hour
        // FIXME hack to force read of context every hour to solve MMDB-491
        log.info("Starting dataset count timer");
        timer.schedule(new TimerTask() {
            @Override
            public void run()
            {
                TupeloStore.getInstance().countDatasets(null, true);
            }

        }, 0, 60 * 60 * 1000);

        log.info("Starting full-text indexing timers");
        // do a full-text index sweep every hour
        // FIXME do less often
        timer.schedule(new TimerTask() {
            public void run() {
                TupeloStore.getInstance().indexFullTextAll();
            }
        }, 30 * 60 * 1000, 60 * 60 * 1000);

        timer.schedule(new TimerTask() {
            public void run() {
                TupeloStore.getInstance().consumeFullTextIndexQueue();
                TupeloStore.getInstance().expireBeans();
            }
        }, 2 * 1000, 10 * 1000);
    }

    private void setUpSearch() throws IOException {
        File folder = new File(TupeloStore.getInstance().getConfiguration(ConfigurationKey.SearchPath));

        log.info("Lucene search index directory = " + folder.getAbsolutePath());
        FSDirectory dir = FSDirectory.getDirectory(folder);
        IndexWriter.unlock(dir);
        LuceneTextIndex<String> search = new LuceneTextIndex<String>(folder);
        search.setTextExtractor(new SearchableThingTextExtractor());
        search.setIdGetter(new SearchableThingIdGetter());
        TupeloStore.getInstance().setSearch(search);
    }

    private void createRelationships(Properties props) {
        Context context = TupeloStore.getInstance().getContext();
        try {
            TripleWriter tw = new TripleWriter();

            // add all the relationships
            for (String key : props.stringPropertyNames() ) {
                if (key.startsWith("relationship.") && key.endsWith(".predicate")) { //$NON-NLS-1$ //$NON-NLS-2$
                    String pre = key.substring(0, key.lastIndexOf(".")); //$NON-NLS-1$
                    if (props.containsKey(pre + ".label")) { //$NON-NLS-1$
                        Resource i = null;
                        if (props.containsKey(pre + ".inverse")) {
                            i = Resource.uriRef(props.getProperty(pre + ".inverse"));
                        }
                        Resource r = Resource.uriRef(props.getProperty(key));
                        String l = props.getProperty(pre + ".label");
                        tw.add(r, Rdf.TYPE, MMDB.USER_RELATIONSHIP); //$NON-NLS-1$
                        // remove existing label
                        context.removeTriples(context.match(r, Rdfs.LABEL, null));
                        tw.add(r, Rdfs.LABEL, l);
                        if (i != null) {
                            tw.add(r, Resource.uriRef(Namespaces.owl("inverseOf")), i);
                            tw.add(i, Resource.uriRef(Namespaces.owl("inverseOf")), r);
                        }
                        log.debug("Adding user relationship '" + l + "' (" + r + ")");
                    }
                }
            }

            context.perform(tw);
        } catch (OperatorException exc) {
            log.warn("Could not add user relationships.", exc);
        }
    }

    private void createUserFields(Properties props) {
        Context context = TupeloStore.getInstance().getContext();
        TripleWriter tw = new TripleWriter();

        Set<Resource> blacklistedPredicates = new HashSet<Resource>();
        blacklistedPredicates.add(Resource.uriRef("http://purl.org/dc/terms/license"));
        blacklistedPredicates.add(Resource.uriRef("http://purl.org/dc/terms/rightsHolder"));
        blacklistedPredicates.add(Resource.uriRef("http://purl.org/dc/terms/rights"));
        blacklistedPredicates.add(Dc.TITLE);
        blacklistedPredicates.add(Dc.CREATOR);
        blacklistedPredicates.add(Dc.IDENTIFIER);
        blacklistedPredicates.add(Dc.CONTRIBUTOR); // should whitelist once we have multi-valued user properties
        blacklistedPredicates.add(Rdfs.LABEL);
        // there's an even longer list, but these are some of the ones I expect we'd have the most problems with

        // add all the userfields
        for (String key : props.stringPropertyNames() ) {
            if (key.startsWith("userfield.") && key.endsWith(".predicate")) { //$NON-NLS-1$ //$NON-NLS-2$
                String pre = key.substring(0, key.lastIndexOf(".")); //$NON-NLS-1$
                if (props.containsKey(pre + ".label")) { //$NON-NLS-1$
                    Resource r = Resource.uriRef(props.getProperty(key));
                    String l = props.getProperty(pre + ".label");
                    tw.add(r, Rdf.TYPE, MMDB.USER_METADATA_FIELD); //$NON-NLS-1$
                    tw.add(r, Rdfs.LABEL, l);
                    log.debug("Adding user metadata field '" + l + "' (" + r + ")");
                }
            }
        }

        // remove blacklisted predicates
        for (Resource blacklisted : blacklistedPredicates ) {
            tw.remove(blacklisted, Rdf.TYPE, MMDB.USER_METADATA_FIELD);
        }

        try {
            context.perform(tw);
        } catch (OperatorException exc) {
            log.warn("Could not add userfields.", exc);
        }
    }

    private void createAccounts(Properties props) throws OperatorException, AuthenticationException, RBACException {
        Context context = TupeloStore.getInstance().getContext();
        ContextAuthentication auth = new ContextAuthentication(context);
        MediciRbac rbac = new MediciRbac(context);

        // ensure base RBAC ontology exists
        log.debug("Initializing Medici permission set...");
        rbac.createBaseOntology();

        // ensure Medici permissions exist
        rbac.intializePermissions();
        rbac.associatePermissionsWithRoles();

        //ensure default roles exist
        for (DefaultRole role : DefaultRole.values() ) {
            ensureRoleExists(role, rbac);
        }

        // ensure anonymous user exists
        PersonBean anon = PersonBeanUtil.getAnonymous();
        //        auth.addUser(anon.getEmail(), anon.getName(), "none");
        PersonBeanUtil pbu = new PersonBeanUtil(TupeloStore.getInstance().getBeanSession());
        try {
            pbu.update(anon);
        } catch (Exception e) {
            log.error("Error saving anonymous user", e);
        }
        TupeloStore.getInstance().getBeanSession().save();
        Resource anonymousRole = Resource.uriRef(DefaultRole.ANONYMOUS.getUri());
        Resource anonymousURI = Resource.uriRef(anon.getUri());
        rbac.addRole(anonymousURI, anonymousRole);
        log.debug("User " + anonymousURI + " was given role " + anonymousRole);

        // create accounts
        Set<String> keys = new HashSet<String>();
        for (String key : props.stringPropertyNames() ) {
            if (key.startsWith("user.")) { //$NON-NLS-1$
                String pre = key.substring(0, key.lastIndexOf(".")); //$NON-NLS-1$
                if (!keys.contains(pre)) {
                    keys.add(pre);
                    String username = props.getProperty(pre + ".username");
                    String fullname = props.getProperty(pre + ".fullname");
                    String email = props.getProperty(pre + ".email");
                    String password = props.getProperty(pre + ".password");

                    // create the user
                    Resource userid;
                    if ((username != null) && !username.equals(email)) {
                        log.warn("username should not be used anymore in server.properties.");
                        userid = auth.addUser(username, email, fullname, password);
                    } else {
                        userid = auth.addUser(email, fullname, password);
                    }

                    // add roles
                    for (String role : props.getProperty(pre + ".roles", "").split("[,\\s]+") ) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        if ("admin".equalsIgnoreCase(role)) { //$NON-NLS-1$
                            Resource adminRole = Resource.uriRef(DefaultRole.ADMINISTRATOR.getUri());
                            Resource viewMemberPages = Resource.uriRef(Permission.VIEW_MEMBER_PAGES.getUri());
                            Resource viewAdminPages = Resource.uriRef(Permission.VIEW_ADMIN_PAGES.getUri());
                            Resource editRoles = Resource.uriRef(Permission.EDIT_ROLES.getUri());
                            log.info("Adding " + userid + " to Administrator role");
                            rbac.addRole(userid, adminRole);
                            // this admin needs to have admin permissions. if they don't, create them
                            if (!rbac.checkPermission(userid, viewMemberPages) ||
                                    !rbac.checkPermission(userid, viewAdminPages) ||
                                    !rbac.checkPermission(userid, editRoles)) {
                                log.info("User " + userid + " cannot administer access control: adding permissions to Administrator role");
                                rbac.setPermissionValue(adminRole, viewMemberPages, RBAC.ALLOW);
                                rbac.setPermissionValue(adminRole, viewAdminPages, RBAC.ALLOW);
                                rbac.setPermissionValue(adminRole, editRoles, RBAC.ALLOW);
                            }
                        }
                        if ("member".equalsIgnoreCase(role)) { //$NON-NLS-1$
                            ensureRoleExists(DefaultRole.VIEWER, rbac);
                            rbac.addRole(userid, Resource.uriRef(DefaultRole.VIEWER.getUri()));
                        }
                    }
                }
            }
        }
    }

    void ensureRoleExists(DefaultRole role, MediciRbac rbac) throws OperatorException, RBACException {
        Resource roleUri = Resource.uriRef(role.getUri());
        if (!rbac.getRoles().contains(roleUri)) {
            log.warn("WARNING: " + role.getName() + " role does not exist, creating it");
            rbac.createRole(roleUri, role.getName(), role.getPermissions());
        }
    }
}
