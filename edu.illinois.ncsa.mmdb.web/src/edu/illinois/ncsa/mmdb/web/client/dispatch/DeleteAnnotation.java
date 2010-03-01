package edu.illinois.ncsa.mmdb.web.client.dispatch;

/**
 * For this event: "uri" = the annotated thing and "annotationUri" is the uri of the annotation
 * from which it is to be removed
 * @author futrelle
 *
 */
@SuppressWarnings("serial")
public class DeleteAnnotation extends SubjectAction<DeleteAnnotationResult> {
	String annotationUri;

	public DeleteAnnotation() {
	}
	
	public DeleteAnnotation(String uri, String annotationUri) {
		setUri(uri);
		setAnnotationUri(annotationUri);
	}

	public String getAnnotationUri() {
		return annotationUri;
	}

	public void setAnnotationUri(String annotationUri) {
		this.annotationUri = annotationUri;
	}

	
}
