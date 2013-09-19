package org.sead.acr.discovery;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.ws.http.HTTPException;

import org.sead.acr.common.MediciProxy;
import org.sead.acr.common.SparqlQueryServlet;
import org.sead.acr.common.utilities.Queries;

public class CollectionContents extends SparqlQueryServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5202731150389612465L;

	
	protected String getQuery(String tagID) {
		return Queries.getCollectionContents(tagID);
	}
}
