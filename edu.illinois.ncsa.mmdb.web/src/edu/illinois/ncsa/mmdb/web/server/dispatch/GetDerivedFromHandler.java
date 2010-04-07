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
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import static org.tupeloproject.rdf.terms.Cet.cet;

import java.util.LinkedList;
import java.util.List;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDerivedFrom;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDerivedFromResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * Get datasets from which a particular dataset was derived using a
 * Cyberintegrator tool.
 * 
 * @author Joe Futrelle
 * 
 */
public class GetDerivedFromHandler implements
		ActionHandler<GetDerivedFrom, GetDerivedFromResult> {

	/** Commons logging **/
	private static Log log = LogFactory.getLog(GetDerivedFromHandler.class);

	@Override
	public GetDerivedFromResult execute(GetDerivedFrom arg0,
			ExecutionContext arg1) throws ActionException {
		// run the query
		try {
			Resource subject = Resource.uriRef(arg0.getUri());
			Unifier u = new Unifier();
			u.setColumnNames("input");
			u.addPattern("o3", "o3s", subject); // sq
			u.addPattern("o2", cet("workflow/datalist/hasData"), "o3");
			u.addPattern("o1", "o1s", "o2"); // seq
			u.addPattern("step", cet("workflow/step/hasOutput"), "o1");
			u.addPattern("step", cet("workflow/step/hasInput"), "i1");
			u.addPattern("i1", "i1s", "i2");
			u.addPattern("i2", cet("workflow/datalist/hasData"), "i3");
			u.addPattern("i3", "i3s", "input");
			u.addPattern("input", Rdf.TYPE, Cet.DATASET);
			TupeloStore.getInstance().getContext().perform(u);
			List<DatasetBean> df = new LinkedList<DatasetBean>();
			for (Tuple<Resource> row : u.getResult()) {
				df.add(TupeloStore.fetchDataset(row.get(0))); // dbu's only take strings
			}
			return new GetDerivedFromResult(df);
		} catch (Exception x) {
			log.error("Error getting derived datasets for " + arg0.getUri(), x);
			throw new ActionException("unable to find datasets "
					+ arg0.getUri() + " was derived from");
		}
	}

	@Override
	public Class<GetDerivedFrom> getActionType() {
		return GetDerivedFrom.class;
	}

	@Override
	public void rollback(GetDerivedFrom arg0, GetDerivedFromResult arg1,
			ExecutionContext arg2) throws ActionException {
		// TODO Auto-generated method stub

	}

}
