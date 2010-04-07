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
import java.util.Collections;
import java.util.Comparator;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.OperatorException;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetAnnotations;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetAnnotationsResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.AnnotationBean;
import edu.uiuc.ncsa.cet.bean.tupelo.AnnotationBeanUtil;

/**
 * Get annotations attached to a specific resource sorted by date.
 * 
 * @author Luigi Marini
 * 
 */
public class GetAnnotationsHandler implements
		ActionHandler<GetAnnotations, GetAnnotationsResult> {

	/** Commons logging **/
	private static Log log = LogFactory.getLog(GetAnnotationsHandler.class);

	@Override
	public GetAnnotationsResult execute(GetAnnotations arg0,
			ExecutionContext arg1) throws ActionException {

		BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

		AnnotationBeanUtil abu = new AnnotationBeanUtil(beanSession);

		ArrayList<AnnotationBean> annotations = new ArrayList<AnnotationBean>();

		try {
			annotations = new ArrayList<AnnotationBean>(abu
					.getAssociationsFor(arg0.getUri()));
		} catch (OperatorException e) {
			log.error("Error getting associations for resource "
					+ arg0.getUri(), e);
			e.printStackTrace();
		}

		// sort annotations by date
		Collections.sort(annotations, new Comparator<AnnotationBean>() {

			@Override
			public int compare(AnnotationBean arg0, AnnotationBean arg1) {
				return arg0.getDate().compareTo(arg1.getDate());
			}
		});

		return new GetAnnotationsResult(annotations);
	}

	@Override
	public Class<GetAnnotations> getActionType() {
		return GetAnnotations.class;
	}

	@Override
	public void rollback(GetAnnotations arg0, GetAnnotationsResult arg1,
			ExecutionContext arg2) throws ActionException {
		// TODO Auto-generated method stub

	}

}
