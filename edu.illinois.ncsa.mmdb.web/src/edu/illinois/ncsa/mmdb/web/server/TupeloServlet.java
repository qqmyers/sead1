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
package edu.illinois.ncsa.mmdb.web.server;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.FilterContext;
import org.tupeloproject.kernel.Operator;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.SparqlOperator;
import org.tupeloproject.kernel.TripleReader;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.server.HttpTupeloServlet;
import org.tupeloproject.util.ListTable;
import org.tupeloproject.util.Table;
import org.tupeloproject.util.Tuple;
import org.tupeloproject.util.UnicodeTranscoder;

import edu.illinois.ncsa.mmdb.web.rest.AuthenticatedServlet;

/**
 * TupeloServlet
 */
public class TupeloServlet extends HttpTupeloServlet {
    Log log = LogFactory.getLog(TupeloServlet.class);

    class LiteralFilter extends FilterContext {
    	public LiteralFilter(Context c) {
    		super(c);
    	}
    	Resource postProcess(Resource literal) {
    		if(literal == null) {
    			return null;
    		} else if(!literal.isLiteral()) {
    			return literal;
    		} else {
    			String text = literal.getString();
    			if(!text.matches("[\\P{Cc}\\p{Space}]*")) {
    				log.warn("warning: bad literal \""+text+"\", escaping");
    				return Resource.literal(UnicodeTranscoder.encode(text));
    			} else {
    				return literal;
    			}
    		}
    	}
    	void postProcess(TripleReader tr) {
    		Set<Triple> newResult = new HashSet<Triple>();
    		for(Triple t : tr.getResult()) {
    			Triple newT = new Triple();
    			for(int i = 0; i < 3; i++) {
    				newT.set(i, postProcess(t.get(i)));
    			}
    			newResult.add(newT);
    		}
    		tr.setResult(newResult);
    	}
    	void postProcess(Unifier u) {
    		Table<Resource> uResult = u.getResult();
    		ListTable<Resource> newResult = new ListTable<Resource>();
    		newResult.setColumnNames(uResult.getColumnNames());
    		for(Tuple<Resource> row : u.getResult()) {
    			Tuple<Resource> newRow = new Tuple<Resource>(row.size());
    			for(int i = 0; i < row.size(); i++) {
    				newRow.set(i, postProcess(row.get(i)));
    			}
    			newResult.addRow(newRow);
    		}
    		u.setResult(newResult);
    	}
    	public void delegateOperation(Operator operator) throws OperatorException {
    		getContext().perform(operator);
    		if(operator instanceof TripleReader) {
    			postProcess((TripleReader)operator);
    		} else if(operator instanceof Unifier) {
    			postProcess((Unifier)operator);
    		} else if(operator instanceof SparqlOperator) {
    			log.warn("warning: literal post-processing of SPARQL operators (MMDB-315) not yet implemented!");
    		}
    	}
    }
    
    @Override
    public Context getContext() {
        Context context = new LiteralFilter(TupeloStore.getInstance().getContext());
    	return context;
        //log.info("Tupelo Servlet got context "+context);
        //return context;
    }

	@Override
	public void doDelete(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		if(!AuthenticatedServlet.doAuthenticate(arg0,arg1,getServletContext())) {
			return;
		}
		super.doDelete(arg0, arg1);
	}

	@Override
	public void doGet(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		if(!AuthenticatedServlet.doAuthenticate(arg0,arg1,getServletContext())) {
			return;
		}
		super.doGet(arg0, arg1);
	}

	@Override
	public void doPost(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		if(!AuthenticatedServlet.doAuthenticate(arg0,arg1,getServletContext())) {
			return;
		}
		super.doPost(arg0, arg1);
	}

	@Override
	public void doPut(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		if(!AuthenticatedServlet.doAuthenticate(arg0,arg1,getServletContext())) {
			return;
		}
		super.doPut(arg0, arg1);
	}
}