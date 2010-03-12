package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.Arrays;
import java.util.Collection;

import net.customware.gwt.dispatch.shared.Action;

@SuppressWarnings("serial")
public class RemoveFromCollection implements Action<RemoveFromCollectionResult> {
	String collectionUri;
	Collection<String> resources;
	
	public RemoveFromCollection() { }
	
	public RemoveFromCollection(String collectionUri, String resource) {
		setCollectionUri(collectionUri);
		setResources(Arrays.asList(resource));
	}
	
	public RemoveFromCollection(String collectionUri, Collection<String> resources) {
		setCollectionUri(collectionUri);
		setResources(resources);
	}

	public String getCollectionUri() {
		return collectionUri;
	}

	public void setCollectionUri(String collectionUri) {
		this.collectionUri = collectionUri;
	}

	public Collection<String> getResources() {
		return resources;
	}

	public void setResources(Collection<String> resources) {
		this.resources = resources;
	}
}
