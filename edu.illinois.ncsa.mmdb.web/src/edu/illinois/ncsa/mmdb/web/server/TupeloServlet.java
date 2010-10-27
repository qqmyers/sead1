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
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.FilterContext;
import org.tupeloproject.kernel.NotPermittedException;
import org.tupeloproject.kernel.Operator;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.SparqlOperator;
import org.tupeloproject.kernel.Transformer;
import org.tupeloproject.kernel.TripleReader;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.query.Pattern;
import org.tupeloproject.server.HttpTupeloServlet;
import org.tupeloproject.util.ListTable;
import org.tupeloproject.util.Table;
import org.tupeloproject.util.Tuple;
import org.tupeloproject.util.UnicodeTranscoder;
import org.tupeloproject.util.Variable;

import edu.illinois.ncsa.mmdb.web.rest.AuthenticatedServlet;
import edu.uiuc.ncsa.cet.bean.rbac.medici.Permission;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.medici.MediciRbac;

/**
 * TupeloServlet
 */
public class TupeloServlet extends HttpTupeloServlet {
    Log                log = LogFactory.getLog(TupeloServlet.class);
    static Set<String> predicatePrefixBlacklist;

    class MediciFilter extends FilterContext {

        boolean isBlacklisted(Resource predicate) {
            if (predicate == null) {
                return false;
            }
            if (predicatePrefixBlacklist == null) {
                predicatePrefixBlacklist = new HashSet<String>();
                predicatePrefixBlacklist.add("http://cet.ncsa.uiuc.edu/2007/foaf/context/");
            }
            for (String prefix : predicatePrefixBlacklist ) {
                if (predicate.getString().startsWith(prefix)) {
                    log.warn(predicate + " is blacklisted");
                    return true;
                }
            }
            return false;
        }

        boolean isBlacklisted(Triple t) {
            for (Resource term : t ) {
                if (isBlacklisted(term)) {
                    log.warn(t + " is blacklisted");
                    return true;
                }
            }
            return false;
        }

        public MediciFilter(Context c) {
            super(c);
        }

        Resource postProcess(Resource literal) {
            if (literal == null) {
                return null;
            } else if (!literal.isLiteral()) {
                return literal;
            } else {
                String text = literal.getString();
                if (!text.matches("[\\P{Cc}\\p{Space}]*")) {
                    log.warn("warning: bad literal \"" + text + "\", escaping");
                    return Resource.literal(UnicodeTranscoder.encode(text));
                } else {
                    return literal;
                }
            }
        }

        Triple postProcess(Triple t) {
            Triple newT = new Triple();
            for (int i = 0; i < 3; i++ ) {
                newT.set(i, postProcess(t.get(i)));
            }
            return newT;
        }

        void postProcess(TripleReader tr) {
            Set<Triple> newResult = new HashSet<Triple>();
            for (Triple t : tr.getResult() ) {
                if (!isBlacklisted(t)) {
                    newResult.add(postProcess(t));
                }
            }
            tr.setResult(newResult);
        }

        void postProcess(Unifier u) {
            // find unbound predicate columns; these may carry blacklisted predicates in the result set
            Set<String> unboundPredicateColumns = new HashSet<String>();
            for (Pattern p : u.getPatterns() ) {
                Variable<Resource> predicateTerm = p.get(1);
                if (!predicateTerm.isBound()) {
                    unboundPredicateColumns.add(predicateTerm.getName());
                }
            }
            Table<Resource> uResult = u.getResult();
            ListTable<Resource> newResult = new ListTable<Resource>();
            newResult.setColumnNames(uResult.getColumnNames());
            for (Tuple<Resource> row : u.getResult() ) {
                Tuple<Resource> newRow = new Tuple<Resource>(row.size());
                boolean rowBlacklisted = false;
                for (int i = 0; i < row.size(); i++ ) {
                    if (unboundPredicateColumns.contains(uResult.getColumnNames().get(i)) &&
                            isBlacklisted(row.get(i))) {
                        rowBlacklisted = true;
                        log.warn(row + " is blacklisted");
                    } else {
                        newRow.set(i, postProcess(row.get(i)));
                    }
                }
                if (!rowBlacklisted) {
                    newResult.addRow(newRow);
                }
            }
            u.setResult(newResult);
        }

        void preProcess(List<Pattern> patterns) throws NotPermittedException {
            for (Pattern p : patterns ) {
                Variable<Resource> predicateTerm = p.get(1);
                if (predicateTerm.isBound() &&
                        isBlacklisted(predicateTerm.getValue())) {
                    throw new NotPermittedException("query on blacklisted predicate not permitted");
                }
            }
        }

        public void delegateOperation(Operator operator) throws OperatorException {
            if (operator instanceof Unifier) {
                preProcess(((Unifier) operator).getPatterns());
            } else if (operator instanceof Transformer) {
                preProcess(((Transformer) operator).getInPatterns());
            }
            getContext().perform(operator);
            if (operator instanceof TripleReader) {
                postProcess((TripleReader) operator);
            } else if (operator instanceof Unifier) {
                postProcess((Unifier) operator);
            } else if (operator instanceof SparqlOperator) {
                log.warn("warning: literal post-processing of SPARQL operators (MMDB-315) not yet implemented!");
            }
        }
    }

    @Override
    public Context getContext() {
        Context context = new MediciFilter(TupeloStore.getInstance().getContext());
        return context;
        //log.info("Tupelo Servlet got context "+context);
        //return context;
    }

    boolean isAllowed(HttpServletRequest req, HttpServletResponse resp) {
        ServletContext sc = getServletContext();
        if (!AuthenticatedServlet.doAuthenticate(req, resp, sc)) {
            return false;
        }
        MediciRbac rbac = new MediciRbac(getContext());
        Resource userUri = Resource.uriRef(AuthenticatedServlet.getUserUri(req));
        Resource permissionUri = Resource.uriRef(Permission.USE_DESKTOP.getUri());
        try {
            if (!rbac.checkPermission(userUri, permissionUri)) {
                return false;
            }
        } catch (RBACException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void doDelete(HttpServletRequest arg0, HttpServletResponse arg1)
            throws ServletException, IOException {
        if (!isAllowed(arg0, arg1)) {
            return;
        }
        super.doDelete(arg0, arg1);
    }

    @Override
    public void doGet(HttpServletRequest arg0, HttpServletResponse arg1)
            throws ServletException, IOException {
        if (!isAllowed(arg0, arg1)) {
            return;
        }
        super.doGet(arg0, arg1);
    }

    @Override
    public void doPost(HttpServletRequest arg0, HttpServletResponse arg1)
            throws ServletException, IOException {
        if (!isAllowed(arg0, arg1)) {
            return;
        }
        super.doPost(arg0, arg1);
    }

    @Override
    public void doPut(HttpServletRequest arg0, HttpServletResponse arg1)
            throws ServletException, IOException {
        if (!isAllowed(arg0, arg1)) {
            return;
        }
        super.doPut(arg0, arg1);
    }
}