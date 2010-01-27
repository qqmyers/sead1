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
	private String sessionId;

	public AnnotateResource() {}
	
	/**
	 * 
	 * @param id the resource to which to attach the annotation
	 * @param annotation the new annotation
	 * @param sessionId 
	 */
	public AnnotateResource(String id, AnnotationBean annotation, String sessionId) {
		setUri(id);
		this.annotation = annotation;
		this.sessionId = sessionId;
	}

	public AnnotationBean getAnnotation() {
		return annotation;
	}

	/** @deprecated use getUri */
	public String getId() {
		return getUri();
	}

	public String getSessionId() {
		return sessionId;
	}
}
