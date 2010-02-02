package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class SetProperty extends SubjectAction<SetPropertyResult> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6257131114264143785L;

	String propertyUri;
	
	public SetProperty() { }
	
	private Collection<String> values;
	
	public SetProperty(String uri, String propertyUri, Collection<String> values) {
		this.uri = uri;
		setPropertyUri(propertyUri);	
		this.setValues(values);
	}

	public SetProperty(String uri, String propertyUri, String value) {
		this.uri = uri;
		setPropertyUri(propertyUri);	
		Set<String> values = new HashSet<String>();
		values.add(value);
		this.setValues(values);
	}

	public void setValues(Collection<String> values) {
		this.values = values;
	}

	public Collection<String> getValues() {
		return values;
	}

	public String getPropertyUri() {
		return propertyUri;
	}

	public void setPropertyUri(String propertyUri) {
		this.propertyUri = propertyUri;
	}
}
