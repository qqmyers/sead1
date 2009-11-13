/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.OperatorException;

import edu.illinois.ncsa.mmdb.web.client.dispatch.AnnotateResource;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AnnotateResourceResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.AnnotationBean;
import edu.uiuc.ncsa.cet.bean.tupelo.AnnotationBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;

/**
 * Annotate any resource in repository.
 * 
 * @author Luigi Marini
 *
 */
public class AnnotateResourceHandler implements ActionHandler<AnnotateResource, AnnotateResourceResult>{

	/** Tupelo bean session **/
	private static final BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

	/** Annotations DAO **/
	private static AnnotationBeanUtil abu = new AnnotationBeanUtil(beanSession);
	
	/**
	 *
	 */
	@Override
	public AnnotateResourceResult execute(AnnotateResource arg0,
			ExecutionContext arg1) throws ActionException {

		AnnotationBean annotation = arg0.getAnnotation();
		
		String resource = arg0.getId();
		
		if (annotation.getCreator() == null) {
			annotation.setCreator(PersonBeanUtil.getAnonymous());
		}

		// store annotation
		try {
			beanSession.register(annotation);
			beanSession.save(annotation);
			abu.addAssociationTo(resource, annotation);
		} catch (OperatorException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new AnnotateResourceResult();
	}

	@Override
	public Class<AnnotateResource> getActionType() {
		return AnnotateResource.class;
	}

	@Override
	public void rollback(AnnotateResource arg0, AnnotateResourceResult arg1,
			ExecutionContext arg2) throws ActionException {
		// TODO Auto-generated method stub
		
	}

}
