/**
 * This package contains an AuthFilter that:
 * 1) validates the users credentials against a remote Medici instance, and 
 * 2) stores the user's name/password in a MediciProxy object associated with the session.
 * 
 * If 'enableAnonymous' is true (in the properties file), the filter will attempt to login as anonymous whenever other valid credentials are not available
 * 
 *  The filter protects everything that doesn't start with OpenPath ("/login" by default) and captures calls to "/DoLogin" and "/DoLogout"
 *  It also allows for a subarea (WebAuthPath - null/not set by default) to return 403 (forbidden) errors rather than redirecting to the login page
 *  if good credentials are not available
 *  
 *  You must create a login.html page that has a login form with an AJAX call to DoLogin. That page must also handle errors (unauthorized and bad request).
 *  If that page uses images, css, or other resources, they can only be accessed from directories starting with "/login".
 *  A call to DoLogout will invalidate the current session, thereby make the stored credentials inaccessible, and send the user back to your LoginPage (defaults to login.html)
 *  All other calls to servlets or other resources will get redirected to login.html unless valid credentials have been created.  
 *  
 *  Note: The properties file is loaded at init time and is available to other classes.
 *    
 *  The filter is invoked by putting the following in web.xml:
 *  
 *  <filter>
 *   <filter-name>AuthFilter</filter-name>
 *   <filter-class>
 *      org.sead.acr.common.auth.AuthFilter
 *   </filter-class>
 *   <init-param>
 *      <param-name>PropertiesFileName</param-name>
 *      <param-value>/myparams.properties</param-value>
 *   </init-param>
 *   <init-param>
 *      <param-name>LoginPage</param-name>
 *      <param-value>/login.html</param-value>
 *   </init-param>
 *   <init-param>
 *      <param-name>OpenPath</param-name>
 *      <param-value>/login</param-value>
 *   </init-param>
 *   <init-param>
 *      <param-name>WebAuthPath</param-name>
 *      <param-value>/geoproxy</param-value>
 *   </init-param>
 *  </filter>
 *  <filter-mapping>
 *   <filter-name>AuthFilter</filter-name>
 *   <url-pattern>/*</url-pattern>
 *  </filter-mapping>
 *  
 *  The myparams.properties file should have a line in it:
 *  domain=<URL of medici rest endpoint you want to connect to>
 *  
 *  If 
 *  remoteAPIKey=<remoteAPIKey from ACR Instance at domain>
 *  is set, the app will end the api ley with each response
 *  If medici has a remoteAPIKey set, this one must match or authentication will fail
 *  
 *  If 
 *  enableAnonymous=true
 *  is set, the filter will attempt tp login as anonymous whenever other credentials are unavailable
 */
/**
 * @author Jim
 *
 */
package org.sead.acr.common.auth;