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
/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Tables;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.illinois.ncsa.mmdb.web.client.DatasetService;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;

/**
 * Server side implementation of <code>DatasetService</code>.
 * 
 * @author Luigi Marini
 *
 * @deprecated use gwt-dispatch
 */
public class DatasetServiceImpl extends RemoteServiceServlet implements
		DatasetService {

	private static final long serialVersionUID = 6537147697377708791L;

	/** Tupelo bean session **/
	private static final BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

	private static DatasetBeanUtil dbu = new DatasetBeanUtil(beanSession);
	
	@Override
	public HashSet<DatasetBean> getDatasets() {

		HashSet<DatasetBean> datasets = new HashSet<DatasetBean>();
		
		try {
			datasets = new HashSet<DatasetBean>(dbu.getAll());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return datasets;
	}

	@Override
	public HashSet<String> getDatasetIds() {
		
		HashSet<String> datasets = new HashSet<String>();
		
		try {
			datasets = new HashSet<String>(dbu.getIDs());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return datasets;
	}

	public List<String> listDatasetUris(String orderBy, boolean desc,
			int limit, int offset) {
		Unifier u = new Unifier();
		u.setColumnNames("s");
		u.addPattern("s",Rdf.TYPE,Cet.DATASET);
		u.addPattern("s",Resource.uriRef(orderBy),"o");
		u.setLimit(limit);
		u.setOffset(offset);
		if(!desc) { u.addOrderBy("o"); }
		else { u.addOrderByDesc("o"); }
		try {
			TupeloStore.getInstance().getContext().perform(u);
			List<String> result = new LinkedList<String>();
			for(Resource r : Tables.getColumn(u.getResult(),0)) {
				result.add(r.getString());
			}
			return result;
		} catch(OperatorException x) {
			return new LinkedList<String>();
		}
	}

	public List<DatasetBean> listDatasets(String orderBy, boolean desc,
			int limit, int offset) {
		try {
			return dbu.get(listDatasetUris(orderBy,desc,limit,offset));
		} catch(Exception x) {
			return new LinkedList<DatasetBean>();
		}
	}
}
