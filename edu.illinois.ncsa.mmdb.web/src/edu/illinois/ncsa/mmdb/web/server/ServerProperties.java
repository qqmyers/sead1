/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Singleton to load properties from server.properties.
 * 
 * @author Luigi Marini
 * 
 */
public class ServerProperties {

    /** Singleton instance **/
    private static ServerProperties instance;

    /** server.properties **/
    private final Properties        props;

    /** Commons logging **/
    private static Log              log = LogFactory.getLog(ServerProperties.class);

    /**
     * Return singleton instance.
     * 
     * @return singleton ServerProperties
     */
    public static ServerProperties getInstance() {
        if (instance == null) {
            instance = new ServerProperties();
        }
        return instance;
    }

    /**
     * Use getInstance() to retrieve singleton instance.
     */
    private ServerProperties() {
        Properties props = new Properties();
        String path = "/server.properties";
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
        this.props = props;
    }

    public Properties getProperties() {
        return props;
    }
}
