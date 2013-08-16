/**
 * This package contains an AuthFilter that:
 * 1) validates the users credentials against a remote Medici instance, and 
 * 2) stores the user's name/password in a MediciProxy object associated with the session.
 * 
 *  The filter protects everything that doesn't start with "/login" and captures calls to "/DoLogin" and "/DoLogout"
 *  You must create a login.html page that has a login form with an AJAX call to DoLogin. That page must also handle errors (unauthorized and bad request).
 *  If that page uses images, css, or other resources, they can only be accessed from directories starting with "/login".
 *  A call to DoLogout will invalidate the current session, thereby make the stored credentials inaccessible, and send the user back to login.html
 *  All other calls to servlets or other resources will get redirected to login.html unless valid credentials have been created.  
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
 *  </filter>
 *  <filter-mapping>
 *   <filter-name>AuthFilter</filter-name>
 *   <url-pattern>/*</url-pattern>
 *  </filter-mapping>
 *  
 *  The myparams.properties file should have a line in it:
 *  domain=<URL of medici rest endpoint you want to connect to>
 */
/**
 * @author Jim
 *
 */
package org.sead.acr.common.auth;