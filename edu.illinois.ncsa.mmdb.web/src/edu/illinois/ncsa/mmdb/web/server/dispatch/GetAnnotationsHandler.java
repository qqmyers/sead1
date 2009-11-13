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
public class GetAnnotationsHandler implements ActionHandler<GetAnnotations, GetAnnotationsResult>{

	/** Tupelo bean session **/
	private static final BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

	/** Annotations DAO **/
	private static AnnotationBeanUtil abu = new AnnotationBeanUtil(beanSession);
	
	@Override
	public GetAnnotationsResult execute(GetAnnotations arg0,
			ExecutionContext arg1) throws ActionException {

		ArrayList<AnnotationBean> annotations = new ArrayList<AnnotationBean>();
		
		try {
			annotations = new ArrayList<AnnotationBean>(abu.getAssociationsFor(arg0.getId()));
		} catch (OperatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// sort annotations by date
		Collections.sort(annotations, new Comparator<AnnotationBean>() {

			@Override
			public int compare(AnnotationBean arg0, AnnotationBean arg1) {
				// TODO Auto-generated method stub
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
