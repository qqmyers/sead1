package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteAnnotation;
import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteAnnotationResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.AnnotationBeanUtil;

/**
 * Delete annotation. Unlinks it from the thing being annotated, but otherwise leaves it untouched.
 * 
 */
public class DeleteAnnotationHandler implements
		ActionHandler<DeleteAnnotation, DeleteAnnotationResult> {

	/** Commons logging **/
	private static Log log = LogFactory.getLog(DeleteAnnotationHandler.class);
	
	@Override
	public DeleteAnnotationResult execute(DeleteAnnotation arg0, ExecutionContext arg1)
			throws ActionException {
		String thingUri = arg0.getUri();
		String annotationUri = arg0.getAnnotationUri();
		try {
			AnnotationBeanUtil abu = new AnnotationBeanUtil(null);
			Resource ap = abu.getAssociationPredicate();
			TupeloStore.getInstance().getContext().removeTriple(Resource.uriRef(thingUri), ap, Resource.uriRef(annotationUri));
			TupeloStore.getInstance().changed(thingUri);
			return new DeleteAnnotationResult(true);
		} catch (OperatorException e) {
			log.error("Error deleting annotation "+annotationUri+" from "+thingUri);
			return new DeleteAnnotationResult(false);
		}
	}

	@Override
	public Class<DeleteAnnotation> getActionType() {
		return DeleteAnnotation.class;
	}

	@Override
	public void rollback(DeleteAnnotation arg0, DeleteAnnotationResult arg1,
			ExecutionContext arg2) throws ActionException {
		String thingUri = arg0.getUri();
		String annotationUri = arg0.getAnnotationUri();
		try {
			AnnotationBeanUtil abu = new AnnotationBeanUtil(null);
			Resource ap = abu.getAssociationPredicate();
			TupeloStore.getInstance().getContext().addTriple(Resource.uriRef(thingUri), ap, Resource.uriRef(annotationUri));
		} catch (OperatorException e) {
			throw new ActionException("unable to undelete annotation "+annotationUri+" from "+thingUri);
		}
	}
}
