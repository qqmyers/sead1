/**
 * 
 */
package org.sead.acr.common.utilities;

import org.sead.acr.common.DataAccess;

/**
 * @author Jim
 *
 */
public class Queries {
	
	public static String ALL_PUBLISHED_COLLECTIONS = 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
			+ " "
			+ "PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/>"
			+ " "
			+ "Prefix tag: <http://www.holygoat.co.uk/owl/redwood/0.1/tags/>"
			+ " "
			+ "SELECT ?tagID ?title ?abstract ?keywords WHERE {"
			+ " "
			+ "?tagID <rdf:type> <cet:Collection> ."
			+ " "
			+ "?tagID <http://purl.org/dc/terms/issued> ?date ."
			+ " "
			+ "?tagID <dc:title> ?title ."
			+ " "
			+ "OPTIONAL { ?tagID <http://purl.org/dc/terms/abstract> ?abstract } ."
			+ "OPTIONAL { ?tagID <http://purl.org/dc/terms/isReplacedBy> ?deleted }" 
			+ "}" 
			+ "ORDER BY ?title";
			//+ "FILTER (!bound(?deleted)) }"; FILTER (!bound doesn't appear to be implemented in our sparql service
	
	public static String ALL_TOPLEVEL_COLLECTIONS = 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
			+ " "
			+ "PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/>"
			+ " "
			+ "Prefix tag: <http://www.holygoat.co.uk/owl/redwood/0.1/tags/>"
			+ " "
			+ "SELECT ?tagID ?title ?creator ?deleted ?parent WHERE {"
			+ " "
			+ "?tagID <rdf:type> <cet:Collection> ."
			+ " "
			+ "?tagID <dc:title> ?title ."
			+ " "
			+ "OPTIONAL { ?tagID <http://purl.org/dc/terms/isReplacedBy> ?deleted } . " // ?deleted is bound (as rdf nil)  if this collection has been deleted
			+ "OPTIONAL { ?parent <http://purl.org/dc/terms/hasPart> ?tagID } ."         // ?parent is bound if this is a subcollection 
			+ "OPTIONAL { ?tagID <http://purl.org/dc/terms/creator> ?creator }"       // Not all collections have creators (in practice - why they don't is not yet clear)
			+ "} ORDER BY ?title ";
	        //+ "FILTER (!bound(?deleted) !bound(?parent)) }"; FILTER (!bound doesn't appear to be implemented in our sparql service
	
	//Deprecated: Slow when there are lots of data sets - is this really what you want to do?
	public static String ALL_DATASETS = 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
			+ " "
			+ "PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/>"
			+ " "
			+ "Prefix tag: <http://www.holygoat.co.uk/owl/redwood/0.1/tags/>"
			+ " "
			+ "SELECT ?title ?deleted WHERE {"
			+ " "
			+ "?tagID <rdf:type> <cet:Dataset> ."
			+ " "
			+ "?tagID <dc:title> ?title . "
			+ " "
			+ "OPTIONAL { ?tagID <http://purl.org/dc/terms/isReplacedBy> ?deleted . }" // ?deleted is bound (as rdf nil)  if this collection has been deleted
			+ "}";
			//+ "FILTER (!bound(?deleted)) }"; FILTER (!bound doesn't appear to be implemented in our sparql service

	
			public static String RECENT_UPLOADS =
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
			+ " "
			+ "PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/>"
			+ " "
			+ "PREFIX dc: <http://purl.org/dc/elements/1.1/>"
			+ " "
			+ "PREFIX dcterms: <http://purl.org/dc/terms/>"
			+ " "
			+ "SELECT ?tagID ?title ?creator ?date ?deleted WHERE {"
			+ " "
			+ "?tagID <rdf:type> <cet:Dataset>  ."
			+ " "
			+ "?tagID <dc:title> ?title ."
			+ " "
			+ "?tagID <dc:creator> ?creator ."
			+ " "
			+ "?tagID <dc:date> ?date ."
			+ " "
			+ "OPTIONAL { ?tagID <dcterms:isReplacedBy> ?deleted } ."
			+"} "
			+"ORDER BY ASC(?replacedBy) DESC(?date) "
			+"LIMIT 15";
			//+ "FILTER (!bound(?deleted)) }"; FILTER (!bound doesn't appear to be implemented in our sparql service
			
			public static String TEAM_MEMBERS = 
			"PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
			+ "SELECT ?uri ?name  WHERE {"
			+ " "
			+ "?uri <foaf:name> ?name  ."
			+ " "
			+ "?uri <http://cet.ncsa.uiuc.edu/2007/foaf/context/password> ?pword ."
			+"} ORDER BY ?name ";

			public static String PROJECT_INFO = 
					"SELECT ?s ?p ?o  WHERE {"
					+ " ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://cet.ncsa.uiuc.edu/2007/mmdb/Configuration> ."
					+ "?s ?p ?o"
					+"} ";
			
	
	public static String getCollectionContents(String parentID) { 
			return ("PREFIX dcterms: <http://purl.org/dc/terms/>"
			+ " "
			+ "SELECT ?tagID ?title ?length ?abstract ?deleted WHERE { <"
			+ parentID
			+ "> <http://purl.org/dc/terms/hasPart> ?tagID ."
			+ " "
			+ "?tagID <http://purl.org/dc/elements/1.1/title> ?title ."
			+ " "
			+ "OPTIONAL { ?tagID <tag:tupeloproject.org,2006:/2.0/files/length> ?length .}"
			+ " " 
			+ "OPTIONAL { <" + parentID
			+ "> <http://purl.org/dc/terms/abstract> ?abstract . }"
			+ " " 
			+ "OPTIONAL { ?tagID <dcterms:isReplacedBy> ?deleted } ."
			+ "}" 
			+ "ORDER BY ?title");
	}
			public static String getItemCreators(String tagID) throws Exception {
				return ("SELECT ?creator WHERE { <" + tagID
						+ "> <http://purl.org/dc/terms/creator> ?creator . }");
			}
			
			public  static String getItemContacts(String tagID) throws Exception {
				return ("SELECT ?contact WHERE { <" + tagID
						+ "> <http://sead-data.net/terms/contact> ?contact . }");
				
			}
			
			public  static String getItemDescriptors(String tagID) throws Exception {
				return ("SELECT ?name ?descriptor WHERE { <" + tagID
						+ "> <http://purl.org/dc/terms/description> ?descriptor . }");
			}
			
			public  static String getItemKeywords(String tagID) throws Exception {
				return ("SELECT ?name ?keyword WHERE { <" + tagID
						+ "> <http://www.holygoat.co.uk/owl/redwood/0.1/tags/taggedWithTag> ?keyword . }");
			}
			
	
	
}