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

import java.util.HashSet;
import java.util.Set;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Tags;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.TagResource;
import edu.illinois.ncsa.mmdb.web.client.dispatch.TagResourceResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.TagEventBeanUtil;

/**
 * Retrieve tags for a specific resource.
 * 
 * @author Luigi Mairini
 *
 */
public class TagResourceHandler implements ActionHandler<TagResource, TagResourceResult>{
	
	/** Commons logging **/
	private static Log log = LogFactory.getLog(TagResourceHandler.class);
	
	@Override
	public TagResourceResult execute(TagResource arg0, ExecutionContext arg1)
			throws ActionException {

		BeanSession beanSession = TupeloStore.getInstance().getBeanSession();
		
		TagEventBeanUtil tebu = new TagEventBeanUtil(beanSession);
		
		String uri = arg0.getUri();

		Set<String> tags = arg0.getTags();

		try {
			if(arg0.isDelete()) {
				tebu.removeTags(Resource.uriRef(uri), tags);
				for(String tag : tebu.getTags(arg0.getUri())) {
					if(tags.contains(tag)) {
						log.error("failed to delete tag "+tag);
					}
				}
				TupeloStore.getInstance().changed(uri);
				log.debug("removing tags "+tags+" from "+uri);
			} else {
				Set<String> normalizedTags = new HashSet<String>();
				for(String tag : tags) {
					// collapse multiple spaces and lowercase
					normalizedTags.add(tag.replaceAll("  +", " ").toLowerCase());
				}
				log.debug("normalized tags = "+normalizedTags);
				tebu.addTags(Resource.uriRef(uri), null, normalizedTags);
				debug(uri); // FIXME debug
				Set<String> allTags = tebu.getTags(uri);
				for(String tag : normalizedTags) {
					if(!allTags.contains(tag)) {
						log.error("failed to add tag "+tag);
					}
				}
				log.debug("Tagged " + uri + " with tags " + normalizedTags);
				TupeloStore.getInstance().changed(uri);
				return new TagResourceResult(normalizedTags);
			}
		} catch (OperatorException e) {
			log.error("Error tagging " + uri, e);
		}
		
		return new TagResourceResult();
	}

	void debug(String id) {
		try {
			Set<String> tags = new HashSet<String>();
			
			Unifier uf = new Unifier();
			uf.addPattern( Resource.uriRef(id), Tags.HAS_TAGGING_EVENT, "tevent" ); //$NON-NLS-1$
			uf.addPattern( "tevent", Tags.HAS_TAG_OBJECT, "tag" ); //$NON-NLS-1$ //$NON-NLS-2$
			uf.addPattern( "tag", Tags.HAS_TAG_TITLE, "title" ); //$NON-NLS-1$ //$NON-NLS-2$
			uf.addColumnName( "title" ); //$NON-NLS-1$
			uf.addColumnName("tag");
			TupeloStore.getInstance().getContext().perform( uf );
			
			for ( Tuple<Resource> row : uf.getResult() ) {
				System.out.println(row);
				if ( row.get( 0 ) != null ) {
					tags.add( row.get( 0 ).getString() );
				}
			}
		} catch(Exception x) {
			x.printStackTrace();
		}
	}
	@Override
	public Class<TagResource> getActionType() {
		return TagResource.class;
	}

	@Override
	public void rollback(TagResource arg0, TagResourceResult arg1,
			ExecutionContext arg2) throws ActionException {
		// TODO Auto-generated method stub
		
	}

}
