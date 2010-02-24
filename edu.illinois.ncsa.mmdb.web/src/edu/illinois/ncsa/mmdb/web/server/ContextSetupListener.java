/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleMatcher;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;

import edu.illinois.ncsa.bard.jaas.PasswordDigest;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
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
        
        // mail properties
        Properties mail = new Properties();
        for ( String key : props.stringPropertyNames() ) {
            if (key.startsWith( "mail." )) {
                mail.put( key, props.getProperty( key ) );
            }
        }
        Mail.setProperties(mail);

        // initialize system
        createAccounts( props );
        createUserFields( props );
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
