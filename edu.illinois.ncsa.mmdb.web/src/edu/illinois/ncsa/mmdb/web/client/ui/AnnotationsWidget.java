/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.Set;

import com.google.gwt.user.client.ui.Composite;

import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.uiuc.ncsa.cet.bean.AnnotationBean;

/**
 * @author lmarini
 *
 */
public class AnnotationsWidget extends Composite {

	private CommentsView commentsView;

	public AnnotationsWidget(String id, Set<AnnotationBean> annotations, MyDispatchAsync service) {
		commentsView = new CommentsView(id, service);
		initWidget(commentsView);
		commentsView.show(annotations);
	}

}
