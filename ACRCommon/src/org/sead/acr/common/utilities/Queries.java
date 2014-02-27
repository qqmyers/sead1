/**
 * 
 */
package org.sead.acr.common.utilities;


/**
 * @author Jim
 * 
 */
public class Queries {

	public static String ALL_PUBLISHED_COLLECTIONS = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
			+ " "
			+ "PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/>"
			+ " "
			+ "PREFIX tag: <http://www.holygoat.co.uk/owl/redwood/0.1/tags/>"
			+ " "
			+ "PREFIX dc: <http://purl.org/dc/elements/1.1/>"
			+ " "
			+ "SELECT ?tagID ?title ?abstract ?keywords ?deleted WHERE {"
			+ " "
			+ "?tagID <rdf:type> <cet:Collection> ."
			+ " "
			+ "?tagID <http://purl.org/dc/terms/issued> ?date ."
			+ " "
			+ "?tagID <dc:title> ?title ."
			+ " "
			+ "OPTIONAL { ?tagID <http://purl.org/dc/terms/abstract> ?abstract } ."
			+ "OPTIONAL { ?tagID <http://purl.org/dc/terms/isReplacedBy> ?deleted }"
			+ "}" + "ORDER BY ?title";
	// + "FILTER (!bound(?deleted)) }"; FILTER (!bound doesn't appear to be
	// implemented in our sparql service

	public static String ALL_TOPLEVEL_COLLECTIONS = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
			+ " "
			+ "PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/>"
			+ " "
			+ "PREFIX tag: <http://www.holygoat.co.uk/owl/redwood/0.1/tags/>"
			+ " "
			+ "PREFIX dc: <http://purl.org/dc/elements/1.1/>"
			+ " "
			+ "SELECT ?tagID ?title ?creator ?deleted ?parent WHERE {"
			+ " "
			+ "?tagID <rdf:type> <cet:Collection> ."
			+ " "
			+ "?tagID <dc:title> ?title ."
			+ " "
			+ "OPTIONAL { ?tagID <http://purl.org/dc/terms/isReplacedBy> ?deleted } . " // ?deleted
																						// is
																						// bound
																						// (as
																						// rdf
																						// nil)
																						// if
																						// this
																						// collection
																						// has
																						// been
																						// deleted
			+ "OPTIONAL { ?parent <http://purl.org/dc/terms/hasPart> ?tagID } ." // ?parent
																					// is
																					// bound
																					// if
																					// this
																					// is
																					// a
																					// subcollection
			+ "OPTIONAL { ?tagID <http://purl.org/dc/terms/creator> ?creator }" // Not
																				// all
																				// collections
																				// have
																				// creators
																				// (in
																				// practice
																				// -
																				// why
																				// they
																				// don't
																				// is
																				// not
																				// yet
																				// clear)
			+ "} ORDER BY ?title ";
	// + "FILTER (!bound(?deleted) !bound(?parent)) }"; FILTER (!bound doesn't
	// appear to be implemented in our sparql service

	// Deprecated: Slow when there are lots of data sets - is this really what
	// you want to do?
	public static String ALL_DATASETS = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
			+ " "
			+ "PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/>"
			+ " "
			+ "PREFIX tag: <http://www.holygoat.co.uk/owl/redwood/0.1/tags/>"
			+ " "
			+ "PREFIX dc: <http://purl.org/dc/elements/1.1/>"
			+ " "
			+ "SELECT ?title ?deleted WHERE {"
			+ " "
			+ "?tagID <rdf:type> <cet:Dataset> ."
			+ " "
			+ "?tagID <dc:title> ?title . "
			+ " "
			+ "OPTIONAL { ?tagID <http://purl.org/dc/terms/isReplacedBy> ?deleted . }" // ?deleted
																						// is
																						// bound
																						// (as
																						// rdf
																						// nil)
																						// if
																						// this
																						// collection
																						// has
																						// been
																						// deleted
			+ "}";
	// + "FILTER (!bound(?deleted)) }"; FILTER (!bound doesn't appear to be
	// implemented in our sparql service

	public static String RECENT_UPLOADS = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
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
			+ "} "
			+ "ORDER BY ASC(?replacedBy) DESC(?date) " + "LIMIT 15";
	// + "FILTER (!bound(?deleted)) }"; FILTER (!bound doesn't appear to be
	// implemented in our sparql service

	public static String TEAM_MEMBERS = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
			+ "SELECT ?uri ?name  WHERE {"
			+ " "
			+ "?uri <foaf:name> ?name  ."
			+ " "
			+ "?uri <http://cet.ncsa.uiuc.edu/2007/role/hasRole> ?role ."
			+ "} ORDER BY ?name ";

	public static String PROJECT_INFO = "SELECT ?s ?p ?o  WHERE {"
			+ " ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://cet.ncsa.uiuc.edu/2007/mmdb/Configuration> ."
			+ "?s ?p ?o" + "} ";

	/*
	 * Query string to retrieve the layer names and extent of the map dataset_id
	 * (uri), WmsLayerName (layername), WmsLayerUrl (layerurl), dataset title(title)
	 */
	public static String ALL_WMS_LAYERS_INFO = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
			+ "PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/> "
			+ "SELECT ?uri ?layername ?layerurl ?title ?deleted "
			+ "WHERE { "
			+ "?uri <rdf:type> <cet:Dataset> . "
			+ "?uri <cet:metadata/Extractor/WmsLayerName> ?layername . "
			+ "?uri <cet:metadata/Extractor/WmsLayerUrl> ?layerurl . "
			+ "?uri <http://purl.org/dc/elements/1.1/title> ?title . "
			+ "OPTIONAL { ?uri <http://purl.org/dc/terms/isReplacedBy> ?deleted . } "
			+ "}";
	
	public static String getCollectionContents(String parentID) {
		return ("PREFIX dcterms: <http://purl.org/dc/terms/>"
				+ " "
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "  "
				+ "SELECT ?tagID ?title ?length ?abstract ?deleted ?type WHERE { <"
				+ parentID
				+ "> <http://purl.org/dc/terms/hasPart> ?tagID ."
				+ " "
				+ "?tagID <http://purl.org/dc/elements/1.1/title> ?title ."
				+ " "
				+ "?tagID <rdf:type> ?type ."
				+ " "
				+ "OPTIONAL { ?tagID <tag:tupeloproject.org,2006:/2.0/files/length> ?length .}"
				+ " " + "OPTIONAL { <" + parentID
				+ "> <http://purl.org/dc/terms/abstract> ?abstract . }" + " "
				+ "OPTIONAL { ?tagID <dcterms:isReplacedBy> ?deleted } ." + "}" + "ORDER BY ?title");
		// FILTER to get only type for DataSet or Collection
	}

	public static String getItemBibliographicInfo(String tagID) {
		return ("PREFIX dcterms: <http://purl.org/dc/terms/>"
				+ " "
				+ "SELECT ?title ?creator ?contact ?descriptor ?keyword ?location ?abstract"
				+ " " + "WHERE { " + " " + "<"
				+ tagID
				+ "> <http://purl.org/dc/elements/1.1/title> ?title . "
				+ " "
				+ "OPTIONAL {<"
				+ tagID
				+ "> <dcterms:creator> ?creator } ."
				+ " "
				+ "OPTIONAL {<"
				+ tagID
				+ "> <http://sead-data.net/terms/contact> ?contact } . "
				+ " "
				+ "OPTIONAL {<"
				+ tagID
				+ "> <dcterms:description> ?descriptor } . "
				+ " "
				+ "OPTIONAL {<"
				+ tagID
				+ "> <http://www.holygoat.co.uk/owl/redwood/0.1/tags/taggedWithTag> ?keyword } . "
				+ " "
				+ "OPTIONAL {<"
				+ tagID
				+ "> <http://sead-data.net/terms/generatedAt> ?location } . "
				+ " "
				+ "OPTIONAL { <"
				+ tagID
				+ "> <dcterms:abstract> ?abstract } ." + " " + "}");
	}

}