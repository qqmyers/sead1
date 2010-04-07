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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.OperatorException;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetAllTags;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetTagsResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.TagBean;
import edu.uiuc.ncsa.cet.bean.TagEventBean;
import edu.uiuc.ncsa.cet.bean.tupelo.TagEventBeanUtil;

/**
 * Return all tags in the system.
 * 
 * @author Luigi Marini
 *
 */
public class GetAllTagsHandler implements ActionHandler<GetAllTags, GetTagsResult> {

	/** Commons logging **/
	private static Log log = LogFactory.getLog(GetAllTags.class);
	
	@Override
	public GetTagsResult execute(GetAllTags arg0, ExecutionContext arg1)
			throws ActionException {
		BeanSession beanSession = TupeloStore.getInstance().getBeanSession();
		
		TagEventBeanUtil tebu = new TagEventBeanUtil(beanSession);
		
		LinkedHashMap<String, Integer> tags = new LinkedHashMap<String, Integer>();

		Collection<TagEventBean> allTags = new HashSet<TagEventBean>();
		
		try {
			allTags = tebu.getAll();
		} catch (OperatorException e) {
			log.error("Error getting tags", e);
		} catch (Exception e) {
			log.error("Error getting tags", e);
		}
		
		Iterator<TagEventBean> iterator = allTags.iterator();
		while (iterator.hasNext()) {
			TagEventBean next = iterator.next();
			Set<TagBean> tags2 = next.getTags();
			Iterator<TagBean> iterator2 = tags2.iterator();
			while (iterator2.hasNext()) {
				TagBean next2 = iterator2.next();
				String tagString = next2.getTagString();
				if (tags.keySet().contains(tagString)) {
					Integer newValue = tags.get(tagString) + 1;
					tags.put(tagString, newValue);
				} else {
					tags.put(tagString, 1);
				}
			}
		}

		return new GetTagsResult(tags);
	}

	@Override
	public Class<GetAllTags> getActionType() {
		return GetAllTags.class;
	}

	@Override
	public void rollback(GetAllTags arg0, GetTagsResult arg1,
			ExecutionContext arg2) throws ActionException {
		// TODO Auto-generated method stub
		
	}

}
