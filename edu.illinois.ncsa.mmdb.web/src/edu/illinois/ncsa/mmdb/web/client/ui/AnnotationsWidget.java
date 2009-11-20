/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetAnnotations;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetAnnotationsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.uiuc.ncsa.cet.bean.AnnotationBean;

/**
 * @author lmarini
 *
 */
public class AnnotationsWidget extends Composite {

	private CommentsView commentsView;

	public AnnotationsWidget(String id, MyDispatchAsync service) {
		commentsView = new CommentsView(id, service);
		initWidget(commentsView);
		
		service.execute(new GetAnnotations(id), new AsyncCallback<GetAnnotationsResult>() {

			@Override
			public void onFailure(Throwable caught) {
				GWT.log("Error retrieving annotations", null);
				
			}

			@Override
			public void onSuccess(GetAnnotationsResult result) {
				commentsView.show(result.getAnnotations());
				
			}
			
		});
		
		
	}

}
