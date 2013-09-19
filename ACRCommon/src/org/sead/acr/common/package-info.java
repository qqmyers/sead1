/**
 * This package collects common code for interacting with the Medici 1.0 component of the ACR 
 * (primarily via SPARQL queries) and creating JSON outputs for common tasks, e.g.
 * listing collections and datasets, retrieving metadata for a given item, etc. 
 * 
 * The MediciProxy class manages credentials and formats the output while DataAccess performs the query calls.
 * Common queries are stored in the utilities.Queries class
 * 
 *  SparqlQueryServlet gathers the common logic requred to call Medici's sparql endpoint> Derived classes 
 *  specify the specific query and can overide the default handling of success and failures.
 */
/**
 * @author Jim Myers, Ram Prassana, Saurabh Malviya, Rob Kooper
 *
 */
package org.sead.acr.common;