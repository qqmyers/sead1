/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.AnnotateResource;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AnnotateResourceResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.AnnotationBean;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.workflow.Cyberintegrator;

/**
 * Annotate any resource in repository.
 * 
 * @author Luigi Marini
 *
 */
public class AnnotateResourceHandler implements ActionHandler<AnnotateResource, AnnotateResourceResult>{

	/** Tupelo bean session **/
	private static final BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

	/** Tupelo context **/
	private static final Context tupeloContext = TupeloStore.getInstance().getContext();
	
	/**
	 * TODO move to using DAO
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
			beanSession.save();
		} catch (OperatorException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		// add link between resource and annotation
		TripleWriter tw = new TripleWriter();

		tw.add(Triple.create(Resource.uriRef(resource),
				Cyberintegrator.HAS_ANNOTATION, Resource.uriRef(annotation
						.getUri())));

		try {

			tupeloContext.perform(tw);

		} catch (OperatorException e) {

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
