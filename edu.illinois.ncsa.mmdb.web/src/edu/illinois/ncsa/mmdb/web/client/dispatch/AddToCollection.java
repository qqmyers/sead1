/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.Collection;

import net.customware.gwt.dispatch.shared.Action;

/**
 * @author lmarini
 *
 */
@SuppressWarnings("serial")
public class AddToCollection implements Action<AddToCollectionResult> {
	
	private Collection<String> resources;
	private String collectionUri;

	public AddToCollection() {}
	
	public AddToCollection(String collectionUri, Collection<String> resources) {
		this.collectionUri = collectionUri;
		this.resources = resources;
	}
	
	/**
	 * @return the collection
	 */
	public String getCollectionUri() {
		return collectionUri;
	}

	/**
	 * @return the resources
	 */
	public Collection<String> getResources() {
		return resources;
	}

}
