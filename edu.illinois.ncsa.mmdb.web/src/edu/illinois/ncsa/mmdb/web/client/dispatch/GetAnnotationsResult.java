/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.ArrayList;

import net.customware.gwt.dispatch.shared.Result;
import edu.uiuc.ncsa.cet.bean.AnnotationBean;

/**
 * Retrieve annotations for a particular resource.
 * 
 * @author Luigi Marini
 *
 */
public class GetAnnotationsResult implements Result {

	private static final long serialVersionUID = -1408043787452247309L;
	private ArrayList<AnnotationBean> annotations;

	/**
	 * For serialization only.
	 */
	public GetAnnotationsResult() {}
	
	public GetAnnotationsResult(ArrayList<AnnotationBean> annotations) {
		this.annotations = annotations;
		
	}

	public ArrayList<AnnotationBean> getAnnotations() {
		return annotations;
	}
}
