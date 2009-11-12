/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import edu.uiuc.ncsa.cet.bean.AnnotationBean;
import net.customware.gwt.dispatch.shared.Action;

/**
 * @author lmarini
 *
 */
public class AnnotateResource implements Action<AnnotateResourceResult>{

	private static final long serialVersionUID = -4746438030647742921L;
	private String id;
	private AnnotationBean annotation;

	public AnnotateResource() {}
	
	public AnnotateResource(String id, AnnotationBean annotation) {
		this.id = id;
		this.annotation = annotation;
	}

	public AnnotationBean getAnnotation() {
		return annotation;
	}

	public String getId() {
		return id;
	}
}
