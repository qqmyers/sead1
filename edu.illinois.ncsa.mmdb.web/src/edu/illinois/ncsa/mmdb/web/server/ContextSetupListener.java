/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.client.HttpTupeloClient;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleMatcher;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.bard.jaas.PasswordDigest;
import edu.illinois.ncsa.cet.search.IdGetter;
import edu.illinois.ncsa.cet.search.TextExtractor;
import edu.illinois.ncsa.cet.search.impl.LuceneTextIndex;
import edu.illinois.ncsa.mmdb.web.server.search.Search;
import edu.illinois.ncsa.mmdb.web.server.search.SearchableThingIdGetter;
import edu.illinois.ncsa.mmdb.web.server.search.SearchableThingTextExtractor;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PreviewBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PreviewPyramidBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBAC;

/**
 * Create users and roles specified in the server.properties file.
 * 
 * FIXME: add roles to admin and guest account and not specific permissions
 * 
 * @author Luigi Marini, Rob Kooper
 * 
 */
public class ContextSetupListener implements ServletContextListener
{
    private static Log log = LogFactory.getLog( ContextSetupListener.class );

    /* (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextDestroyed( ServletContextEvent arg0 )
    {
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextInitialized( ServletContextEvent arg0 )
    {
        // property file location
        Properties props = new Properties();
        String path = "/server.properties"; //$NON-NLS-1$
        log.debug( "Loading server property file: " + path );

        // load properties
        InputStream input = ContextSetupListener.class.getResourceAsStream( path );
        try {
            props.load( input );
        } catch ( IOException exc ) {
            log.warn( "Could not load server.properties.", exc );
        } finally {
            try {
                input.close();
            } catch ( IOException exc ) {
                log.warn( "Could not close server.properties.", exc );
            }
        }

        // some global variables
        if ( props.containsKey( "extractor.url" ) ) { //$NON-NLS-1$
            TupeloStore.getInstance().setExtractionServiceURL( props.getProperty( "extractor.url" ) ); //$NON-NLS-1$
        }
        
        //
        if(props.containsKey("extractor.contextUrl")) {
        	String contextUrl = props.getProperty("extractor.contextUrl");
        	String contextUser = props.getProperty("extractor.contextUser","admin");
        	String contextPassword = props.getProperty("extractor.contextPassword","admin");
        	HttpTupeloClient cc = new HttpTupeloClient();
        	cc.setTupeloUrl(contextUrl);
        	cc.setUsername(contextUser);
        	cc.setPassword(contextPassword);
        	TupeloStore.getInstance().setExtractorContext(cc);
        }
        
        // mail properties
        Properties mail = new Properties();
        for ( String key : props.stringPropertyNames() ) {
            if (key.startsWith( "mail." )) {
                mail.put( key, props.getProperty( key ) );
            }
        }
        Mail.setProperties(mail);

        // set up full-text search
        String indexFile = props.getProperty("search.index",null);
        setUpSearch(indexFile);
        
        // initialize system
        createAccounts( props );
        createUserFields( props );
        
        // FIXME MMDB-514 update context
        updateContext();
    }

    private void setUpSearch(String indexFile) {
        if(indexFile != null) {
        	log.info("Lucene search index directory = "+indexFile);
        	LuceneTextIndex<String> search = new LuceneTextIndex<String>(new File(indexFile));
        	search.setTextExtractor(new SearchableThingTextExtractor());
        	search.setIdGetter(new SearchableThingIdGetter());
        	TupeloStore.getInstance().setSearch(search);
        } else {
        	log.info("No Lucene search index directory specified, search will return dummy results");
        	Search s = new Search();
        	s.setTextExtractor(new TextExtractor<String>() {
				public String extractText(String object) {
					return object;
				}
        	});
        	s.setIdGetter(new IdGetter<String>() {
				public String getId(String object) {
					return object;
				}
        	});
        	TupeloStore.getInstance().setSearch(s);
        }
    }
    
    // FIXME MMDB-514 update context
    private void updateContext()
    {
        Resource image = Resource.literal( "edu.illinois.ncsa.mmdb.extractor.image.ImageExtractor" ); //$NON-NLS-1$
        Context c = TupeloStore.getInstance().getContext();
        TripleWriter tw = new TripleWriter();

        // replace HAS_PYRAMID with HAS_PREVIEW
        TripleMatcher tm = new TripleMatcher();
        tm.setPredicate( PreviewPyramidBeanUtil.HAS_PYRAMID );
        try {
            c.perform( tm );
        } catch ( OperatorException exc ) {
            log.warn( "Could not get wrong pyramid preview predicates.", exc );
        }
        for ( Triple t : tm.getResult() ) {
            tw.remove( t );
            tw.add( t.getSubject(), PreviewBeanUtil.HAS_PREVIEW, t.getObject() );
        }

        // Make sure every piece of metadata has executor, if none add it to image
        Unifier uf = new Unifier();
        uf.addPattern( "predicate", Rdf.TYPE, MMDB.METADATA_TYPE ); //$NON-NLS-1$
        uf.addPattern( "predicate", MMDB.METADATA_EXTRACTOR, "extractor", true ); //$NON-NLS-1$ //$NON-NLS-2$
        uf.setColumnNames( "predicate", "extractor" ); //$NON-NLS-1$ //$NON-NLS-2$
        try {
            c.perform( uf );
        } catch ( OperatorException exc ) {
            log.warn( "Could not get predicates.", exc );
        }
        for ( Tuple<Resource> row : uf.getResult() ) {
            if ( row.get( 1 ) == null ) {
                System.out.println(row.get(0));
                tw.add( row.get( 0 ), MMDB.METADATA_EXTRACTOR, image );
            }
        }


        // Make sure every preview has executor, if none add it to image
        uf = new Unifier();
        uf.addPattern( "dataset", PreviewBeanUtil.HAS_PREVIEW, "preview" ); //$NON-NLS-1$ //$NON-NLS-2$
        uf.addPattern( "preview", MMDB.METADATA_EXTRACTOR, "extractor", true ); //$NON-NLS-1$ //$NON-NLS-2$
        uf.setColumnNames( "preview", "extractor" ); //$NON-NLS-1$ //$NON-NLS-2$
        try {
            c.perform( uf );
        } catch ( OperatorException exc ) {
            log.warn( "Could not get previews.", exc );
        }
        for ( Tuple<Resource> row : uf.getResult() ) {
            if ( row.get( 1 ) == null ) {
                System.out.println(row.get(0));
                tw.add( row.get( 0 ), MMDB.METADATA_EXTRACTOR, image );
            }
        }

        // update context
        try {
            c.perform( tw );
        } catch ( OperatorException exc ) {
            log.warn( "Could not update context.", exc );
        }
    }

    private void createUserFields( Properties props )
    {
        Context context = TupeloStore.getInstance().getContext();
        TripleWriter tw = new TripleWriter();

        // FIXME remove the old code
        tw.remove( Resource.uriRef( "urn:strangeness" ), Rdf.TYPE, Cet.cet( "userMetadataField" ) ); //$NON-NLS-1$ //$NON-NLS-2$
        tw.remove( Resource.uriRef( "urn:charm" ), Rdf.TYPE, Cet.cet( "userMetadataField" ) ); //$NON-NLS-1$ //$NON-NLS-2$

        // add all the userfields
        for ( String key : props.stringPropertyNames() ) {
            if ( key.startsWith( "userfield." ) && key.endsWith( ".predicate" ) ) { //$NON-NLS-1$ //$NON-NLS-2$
                String pre = key.substring( 0, key.lastIndexOf( "." ) ); //$NON-NLS-1$
                if ( props.containsKey( pre + ".label" ) ) { //$NON-NLS-1$
                    Resource r = Resource.uriRef( props.getProperty( key ) );
                    tw.add( r, Rdf.TYPE, Cet.cet( "userMetadataField" ) ); //$NON-NLS-1$
                    tw.add( r, Rdfs.LABEL, props.getProperty( pre + ".label" ) ); //$NON-NLS-1$
                }
            }
        }

        try {
            context.perform( tw );
        } catch ( OperatorException exc ) {
            log.warn( "Could not add userfields.", exc );
        }
    }

    private void createAccounts( Properties props )
    {
        BeanSession beanSession = TupeloStore.getInstance().getBeanSession();
        PersonBeanUtil pbu = new PersonBeanUtil( beanSession );
        RBAC rbac = new RBAC( TupeloStore.getInstance().getContext() );

        for ( String key : props.stringPropertyNames() ) {
            if ( key.startsWith( "user." ) && key.endsWith( ".username" ) ) { //$NON-NLS-1$ //$NON-NLS-2$
                String pre = key.substring( 0, key.lastIndexOf( "." ) ); //$NON-NLS-1$
                String username = props.getProperty( key );

                // create account
                try {
                    PersonBean user = pbu.get( PersonBeanUtil.getPersonID( username ) );
                    Resource userid = Resource.uriRef( user.getUri() );
                    user.setName( props.getProperty( pre + ".fullname", username ) ); //$NON-NLS-1$
                    if ( props.containsKey( pre + ".email" ) ) { //$NON-NLS-1$
                        user.setEmail( props.getProperty( pre + ".email" ) ); //$NON-NLS-1$
                    }
                    beanSession.save( user );

                    // add password if none exists
                    TripleMatcher tripleMatcher = new TripleMatcher();
                    tripleMatcher.setSubject( userid );
                    tripleMatcher.setPredicate( MMDB.HAS_PASSWORD );
                    beanSession.getContext().perform( tripleMatcher );
                    if ( tripleMatcher.getResult().size() == 0 ) {
                        beanSession.getContext().addTriple( userid, MMDB.HAS_PASSWORD, PasswordDigest.digest( props.getProperty( pre + ".password", username ) ) ); //$NON-NLS-1$
                    }

                    for ( String role : props.getProperty( pre + ".roles", "VIEW_MEMBER_PAGES" ).split( "," ) ) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        if ( "VIEW_MEMBER_PAGES".equals( role ) ) { //$NON-NLS-1$
                            rbac.addPermission( userid, MMDB.VIEW_MEMBER_PAGES );
                        }
                        if ( "VIEW_ADMIN_PAGES".equals( role ) ) { //$NON-NLS-1$
                            rbac.addPermission( userid, MMDB.VIEW_ADMIN_PAGES );
                        }
                    }
                } catch ( Exception e ) {
                    log.warn( "Could not create user : " + username, e );
                }
            }
        }
    }
}
