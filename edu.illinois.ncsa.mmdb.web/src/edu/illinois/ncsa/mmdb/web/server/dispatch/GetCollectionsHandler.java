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
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.ObjectResourceMapping;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Table;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollections;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollectionsResult;
import edu.illinois.ncsa.mmdb.web.server.Memoized;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;

/**
 * Retrieve collections.
 * 
 * @author Luigi Marini
 * 
 */
public class GetCollectionsHandler implements
		ActionHandler<GetCollections, GetCollectionsResult> {

	/** Commons logging **/
	private static Log log = LogFactory.getLog(GetCollectionsHandler.class);

	static Memoized<Integer> collectionCount;
	static int getCollectionCount() {
		if(collectionCount == null) {
			collectionCount = new Memoized<Integer>() {
				public Integer computeValue() {
					Unifier u = new Unifier();
					u.setColumnNames("c");
					u.addPattern("c",Rdf.TYPE,Cet.cet("Collection"));
					try {
						long then = System.currentTimeMillis();
						Table<Resource> result = TupeloStore.getInstance().unifyExcludeDeleted(u,"c");
						int count = 0;
						for(Tuple<Resource> row : result) {
							count++;
						}
						long ms = System.currentTimeMillis() - then;
						log.debug("counted "+count+" collection(s) in "+ms+"ms");
						return count;
					} catch (OperatorException e) {
						e.printStackTrace();
						return 0;
					}
				}
			};
			collectionCount.setTtl(30000);
		}
		return collectionCount.getValue();
	}
	
	public static GetCollectionsResult getCollections(GetCollections query) {
		
		BeanSession beanSession = TupeloStore.getInstance().getBeanSession();
		
		CollectionBeanUtil cbu = new CollectionBeanUtil(beanSession);
		PersonBeanUtil pbu = new PersonBeanUtil(beanSession);
		
		int limit = query.getLimit();
		int offset = query.getOffset();
		ArrayList<CollectionBean> collections = new ArrayList<CollectionBean>();
		List<Resource> seen = new LinkedList<Resource>(); 
		try {
			int dups = 1;
			while(dups > 0) {
				int news = 0;
				dups = 0;

				Unifier uf = createUnifier(query, limit, offset);

				Table<Resource> result = TupeloStore.getInstance().unifyExcludeDeleted(uf, "collection");
				
				for (Tuple<Resource> row : result) {
					int r = 0;
					Resource subject = row.get(r++);
					Resource creator = row.get(r++);
					Resource title = row.get(r++);
					Resource description = row.get(r++);
					Resource dateCreated = row.get(r++);
					Resource dateModified = row.get(r++);
					if (subject != null) {
						try {
							if (!seen.contains(subject)) { // FIXME: because of this logic, we may return fewer than the limit!
								CollectionBean colBean = new CollectionBean();
								// FIXME use Rob's BeanFactory instead of this hardcoded way
								colBean.setUri(subject.getString());
								if(creator != null) {
									colBean.setCreator(pbu.get(creator));
								}
								if(title != null) {
									colBean.setTitle(title.getString());
								}
								if(description != null) {
									colBean.setDescription(description.getString());
								}
								if(dateCreated != null) {
									colBean.setCreationDate((Date)ObjectResourceMapping.object(dateCreated));
								}
								if(dateModified != null) {
									colBean.setLastModifiedDate((Date)ObjectResourceMapping.object(dateModified));
								}
								collections.add(colBean);
								seen.add(subject);
								news++;
							} else {
								dups++;
							}
						} catch(OperatorException x) {
							log.error("Unable to fetch collection " + subject,x);
						}
					}
				}
				if(limit > 0 && dups > 0) {
					limit = dups;
					offset += news;  // FIXME: wow, this is a hack
				}
			}
		} catch (OperatorException e1) {
			e1.printStackTrace();
		}
		GetCollectionsResult result = new GetCollectionsResult(collections);
		result.setCount(getCollectionCount());
		return result; 
	}
	
	@Override
	public GetCollectionsResult execute(GetCollections query,
			ExecutionContext arg1) throws ActionException {
		return getCollections(query);
	}
	
	/**
	 * 
	 * @param query
	 * @param limit
	 * @param offset
	 * @return
	 */
	private static Unifier createUnifier(GetCollections query, int limit, int offset) {
		Unifier uf = new Unifier();
		uf.addPattern("collection", Rdf.TYPE,
				CollectionBeanUtil.COLLECTION_TYPE);
		if(query.getMemberUri() != null) {
			uf.addPattern("collection", CollectionBeanUtil.DCTERMS_HAS_PART,
					Resource.uriRef(query.getMemberUri()));
		}
		Resource sortKey = DcTerms.DATE_CREATED;
		if(query.getSortKey() != null) {
			sortKey = Resource.uriRef(query.getSortKey());
		}
		uf.addPattern("collection", sortKey, "o", true);
		// now add columns to fetch the info we need to construct minimal CollectionBeans
		//
		uf.addPattern("collection", Dc.CREATOR, "creator", true);
		uf.addPattern("collection", Dc.TITLE, "title", true);
		uf.addPattern("collection", Dc.DESCRIPTION, "description", true);
		uf.addPattern("collection", DcTerms.DATE_CREATED, "dateCreated", true);
		uf.addPattern("collection", DcTerms.DATE_MODIFIED, "dateModified", true);
		uf.setColumnNames("collection", "creator", "title", "description", "dateCreated", "dateModified", "o");
		if(limit > 0) { uf.setLimit(limit); }
		if(offset > 0) { uf.setOffset(offset); }
		if(!query.isDesc()) {
			uf.addOrderBy("o");
		} else {
			uf.addOrderByDesc("o");
		}
		//System.out.println(SparqlQueryFactory.toSparql(uf));
		return uf;
	}

	@Override
	public Class<GetCollections> getActionType() {
		return GetCollections.class;
	}

	@Override
	public void rollback(GetCollections arg0, GetCollectionsResult arg1,
			ExecutionContext arg2) throws ActionException {
		// TODO Auto-generated method stub

	}

}
