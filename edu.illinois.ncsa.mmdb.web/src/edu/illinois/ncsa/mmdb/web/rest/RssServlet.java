/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010, NCSA.  All rights reserved.
 *
 * Developed by:
 * Cyberenvironments and Technologies (CET)
 * http://cet.ncsa.illinois.edu/
 *
 * National Center for Supercomputing Applications (NCSA)
 * http://www.ncsa.illinois.edu/
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the names of CET, University of Illinois/NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *******************************************************************************/
package edu.illinois.ncsa.mmdb.web.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.util.Tuple;
import org.w3c.dom.Document;

import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedOutput;

import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.UriCanonicalizer;

public class RssServlet extends HttpServlet {
	void die(int code, HttpServletResponse resp, String msg, Throwable exception) throws ServletException {
		resp.setStatus(code);
		try {
			PrintWriter pw = new PrintWriter(resp.getOutputStream());
			pw.println(code+": "+msg);
			pw.flush();
		} catch(IOException x) {
			throw new ServletException("can't write response!",x);
		}
	}
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
		Context c = TupeloStore.getInstance().getContext();
		Unifier u = new Unifier();
		u.setColumnNames("r","s","date","label","title","description");
		u.addPattern("s", Rdf.TYPE, Cet.DATASET);
		u.addPattern("s", Dc.DATE, "date");
		u.addPattern("s", Dc.TITLE, "title", true);
		u.addPattern("s", Rdfs.LABEL, "label", true);
		u.addPattern("s", Dc.DESCRIPTION, "description", true);
		u.addPattern("s", DcTerms.IS_REPLACED_BY, "r",true);
		u.addOrderBy("r"); // FIXME should be orderByDesc once TUP-479 is resolved
		u.addOrderByDesc("date");
		u.setLimit(20);
		try {
			c.perform(u);
		} catch(OperatorException x) {
			die(500,resp,"unable to list datasets",x);
			return;
		}
        // create a blank feed
        SyndFeed feed = new SyndFeedImpl();
        // set it up with our attributes
        feed.setTitle("Medici datasets");
        feed.setLink(req.getRequestURL().toString());
        feed.setDescription("Recent datasets from Medici");
        feed.setFeedType("rss_2.0");
        //
        List<SyndEntry> entries = new LinkedList<SyndEntry>();
        for(Tuple<Resource> row : u.getResult()) {
        	int i = 0;
        	if(row.get(i++) == null) { // i.e., non-deleted current version
        		String datasetUri = row.get(i++).getString();
        		UriCanonicalizer canon = TupeloStore.getInstance().getUriCanonicalizer(req);
        		String link = canon.canonicalize("dataset",datasetUri);
        		Date date = (Date) row.get(i++).asObject();
        		Resource t = row.get(i++);
        		Resource l = row.get(i++);
        		String label = "[no title]";
        		if(t != null) { label = t.getString(); }
        		else if(l != null) { label = l.getString(); }
        		String description = "<img src='"+canon.canonicalize(RestServlet.PREVIEW_SMALL,datasetUri)+"'>";
        		if(row.get(i) != null) {
        			description += "<p>" + row.get(i++).getString() + "</p>";
        		}
        		//
        		SyndEntry entry = new SyndEntryImpl();
        		entry.setLink(link);
        		entry.setPublishedDate(date);
        		entry.setTitle(label);
        		SyndContentImpl d = new SyndContentImpl();
        		d.setValue(description);
        		entry.setDescription(d);
        		entries.add(entry);
        	}
        }
        feed.setEntries(entries);
        // produce it in XML
        SyndFeedOutput out = new SyndFeedOutput();
        resp.setContentType("application/rss+xml");
        try {
            xmlOut(out.outputW3CDom(feed), resp.getOutputStream());
        } catch (Exception x) {
            throw new ServletException("error producing RSS feed", x);
        }        
	}

	static Transformer theTransformer;
	static Transformer getTheTransformer() throws TransformerConfigurationException {
		if(theTransformer == null) {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.setOutputProperty(OutputKeys.METHOD, "xml");
			t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			theTransformer = t;
		}
		return theTransformer;
	}
	// workaround for bug in o.t.util.Xml
	void xmlOut(Document doc, OutputStream os) throws TransformerConfigurationException, TransformerException {
		getTheTransformer().transform(new DOMSource(doc), new StreamResult(os));
	}
}
