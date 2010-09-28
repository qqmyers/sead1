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
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.client.HttpTupeloClient;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;

import edu.illinois.ncsa.cet.search.impl.LuceneTextIndex;
import edu.illinois.ncsa.mmdb.web.server.search.SearchableThingIdGetter;
import edu.illinois.ncsa.mmdb.web.server.search.SearchableThingTextExtractor;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.AuthenticationException;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.ContextAuthentication;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBAC;

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

        // some global variables
        if (props.containsKey("extractor.url")) { //$NON-NLS-1$
            TupeloStore.getInstance().setExtractionServiceURL(props.getProperty("extractor.url")); //$NON-NLS-1$
        }

        //
        if (props.containsKey("extractor.contextUrl")) {
            String contextUrl = props.getProperty("extractor.contextUrl");
            String contextUser = props.getProperty("extractor.contextUser", "admin");
            String contextPassword = props.getProperty("extractor.contextPassword", "admin");
            HttpTupeloClient cc = new HttpTupeloClient();
            cc.setTupeloUrl(contextUrl);
            cc.setUsername(contextUser);
            cc.setPassword(contextPassword);
            try {
                TupeloStore.getInstance().setExtractorContext(cc);
            } catch (Exception e) {
                log.error("Could not set context for extraction service.", e);
            }
        }

        // mail properties
        Properties mail = new Properties();
        for (String key : props.stringPropertyNames() ) {
            if (key.startsWith("mail.")) {
                mail.put(key, props.getProperty(key));
            }
        }
        Mail.setProperties(mail);

        // set up full-text search
        String indexFile = props.getProperty("search.index", null);
        setUpSearch(indexFile);

        // initialize system
        try {
            createAccounts(props);
        } catch (Exception e) {
            log.warn("Could not add accounts.", e);
        }
        createUserFields(props);

        // start the timers
        startTimers();
    }

    private void startTimers() {
        // count datasets every hour
        // FIXME hack to force read of context every hour to solve MMDB-491
        timer.schedule(new TimerTask() {
            @Override
            public void run()
                {
                    TupeloStore.getInstance().countDatasets(null, true);
                }

        }, 0, 60 * 60 * 1000);

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

    private void setUpSearch(String indexFile) {
        File folder = null;
        if (indexFile != null) {
            folder = new File(indexFile);
            if (!folder.exists()) {
                if (!folder.mkdirs()) {
                    folder = null;
                }
            } else if (!folder.isDirectory()) {
                folder = null;
            }
        }

        if (folder == null) {
            folder = new File(System.getProperty("java.io.tmpdir"), "mmdb.lucene");
            folder.mkdirs();
        }

        log.info("Lucene search index directory = " + folder.getAbsolutePath());
        LuceneTextIndex<String> search = new LuceneTextIndex<String>(folder);
        search.setTextExtractor(new SearchableThingTextExtractor());
        search.setIdGetter(new SearchableThingIdGetter());
        TupeloStore.getInstance().setSearch(search);
    }

    private void createUserFields(Properties props) {
        Context context = TupeloStore.getInstance().getContext();
        TripleWriter tw = new TripleWriter();

        // FIXME remove the old code
        tw.remove(Resource.uriRef("urn:strangeness"), Rdf.TYPE, Cet.cet("userMetadataField")); //$NON-NLS-1$ //$NON-NLS-2$
        tw.remove(Resource.uriRef("urn:charm"), Rdf.TYPE, Cet.cet("userMetadataField")); //$NON-NLS-1$ //$NON-NLS-2$

        // add all the userfields
        for (String key : props.stringPropertyNames() ) {
            if (key.startsWith("userfield.") && key.endsWith(".predicate")) { //$NON-NLS-1$ //$NON-NLS-2$
                String pre = key.substring(0, key.lastIndexOf(".")); //$NON-NLS-1$
                if (props.containsKey(pre + ".label")) { //$NON-NLS-1$
                    Resource r = Resource.uriRef(props.getProperty(key));
                    tw.add(r, Rdf.TYPE, Cet.cet("userMetadataField")); //$NON-NLS-1$
                    tw.add(r, Rdfs.LABEL, props.getProperty(pre + ".label")); //$NON-NLS-1$
                }
            }
        }

        try {
            context.perform(tw);
        } catch (OperatorException exc) {
            log.warn("Could not add userfields.", exc);
        }
    }

    private void createAccounts(Properties props) throws OperatorException, AuthenticationException {
        Context context = TupeloStore.getInstance().getContext();
        ContextAuthentication auth = new ContextAuthentication(context);
        RBAC rbac = new RBAC(context);

        // add permissions
        rbac.addPermission(MMDB.ADMIN_ROLE, MMDB.VIEW_ADMIN_PAGES);
        rbac.addPermission(MMDB.ADMIN_ROLE, MMDB.VIEW_MEMBER_PAGES);
        rbac.addPermission(MMDB.REGULAR_MEMBER_ROLE, MMDB.VIEW_MEMBER_PAGES);

        // create accounts
        Set<String> keys = new HashSet<String>();
        for (String key : props.stringPropertyNames() ) {
            if (key.startsWith("user.")) { //$NON-NLS-1$
                String pre = key.substring(0, key.lastIndexOf(".")); //$NON-NLS-1$
                if (!keys.contains(key)) {
                    keys.add(key);
                    String username = props.getProperty(pre + ".username");
                    String fullname = props.getProperty(pre + ".fullname");
                    String email = props.getProperty(pre + ".email");
                    String password = props.getProperty(pre + ".password");

                    // create the user
                    Resource userid = auth.addUser(username, email, fullname, password);

                    // add roles
                    for (String role : props.getProperty(pre + ".roles", "").split(",") ) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        if ("admin".equalsIgnoreCase(role)) { //$NON-NLS-1$
                            rbac.addRole(userid, MMDB.ADMIN_ROLE);
                        }
                        if ("member".equalsIgnoreCase(role)) { //$NON-NLS-1$
                            rbac.addRole(userid, MMDB.REGULAR_MEMBER_ROLE);
                        }
                    }
                }
            }
        }
    }
}
