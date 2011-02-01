package edu.illinois.ncsa.mmdb.web.common;

/**
 * This class will contain the rest endpoints defined by Medici. This will allow
 * easy access to these endpoints from both the server and client side.
 * 
 * @author Rob Kooper
 * 
 */
public class RestEndpoints {
    /** endpoints returning dataset */
    public static final String BLOB_URL      = "api/image/";          //$NON-NLS-1$
    public static final String EXTENSION_URL = "api/dataset/";

    /** endpoint returning image pyramd */
    public static final String PYRAMID_URL   = "rest/secure/pyramid/";

}
