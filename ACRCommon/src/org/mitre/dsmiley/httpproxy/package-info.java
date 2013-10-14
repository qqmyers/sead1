/*
 * These two classes implement a proxy we use for geoserver calls. The proxy is normally protected by the Auth filter 
 * (thus only accessible to valid users with remoteAPI permissions). When accessed, it uses a separate set of 
 * credentials to forward calls to the specified server. The credentials are defined in the usual propeties file set 
 * up by the app:
 * 
 * geoserver=<baseURL for remote service, e.g.: http://sead.ncsa.illinois.edu/geoserver>
 * geouser=
 * geopassword=
 * 
 * It's invoked by adding the following to the web.xml file:
 * 
 * 	<servlet>
 *    <servlet-name>geoproxy</servlet-name>
 *    <servlet-class>org.mitre.dsmiley.httpproxy.GeoProxyServlet</servlet-class>
 *    <init-param>
 *       <param-name>log</param-name>
 *       <param-value>true</param-value>
 *    </init-param>
 *  </servlet>
 *  <servlet-mapping>
 *     <servlet-name>geoproxy</servlet-name>
 *     <url-pattern>/geoproxy/*</url-pattern>
 *  </servlet-mapping>
 *  
 *  It's recommended that it be placed in the WebAuthPath of the AuthFilter so that authentication errors return
 *  a 403 status rather than redirecting to the app login page:
 *  
 *  <init-param>
 *     <param-name>WebAuthPath</param-name>
 *     <param-value>/geoproxy</param-value>
 *  </init-param>
 *  
 *  The ProxyServlet class is unmodified from the orginal mitre code except for the init method - changed so we
 *  could override how the URL was set and add credentials for the proxied calls.
 *  
 */