package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

@SuppressWarnings("serial")
public class SubjectAction<T extends Result> implements Action<T> {
	String uri;
	
	public SubjectAction() {
	}
	
	public SubjectAction(String uri) {
		setUri(uri);
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
	
	
}
