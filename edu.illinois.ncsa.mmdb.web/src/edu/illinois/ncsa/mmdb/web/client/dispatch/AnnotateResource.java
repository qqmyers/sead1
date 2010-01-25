/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import edu.uiuc.ncsa.cet.bean.AnnotationBean;

/**
 * Requests the user to be authenticated on the server side.
 * 
 * @author Luigi Marini
 *
 */
@SuppressWarnings("serial")
public class AnnotateResource extends SubjectAction<AnnotateResourceResult>{
	private AnnotationBean annotation;

	public AnnotateResource() {}
	
	public AnnotateResource(String id, AnnotationBean annotation) {
		setUri(id);
		this.annotation = annotation;
	}

	public AnnotationBean getAnnotation() {
		return annotation;
	}

	/** @deprecated use getUri */
	public String getId() {
		return getUri();
	}
}
