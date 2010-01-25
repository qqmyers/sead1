/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.Set;

/**
 * @author lmarini
 *
 */
@SuppressWarnings("serial")
public class TagResource extends SubjectAction<TagResourceResult>{
	private Set<String> tags;
	
	public TagResource() {
		// TODO Auto-generated constructor stub
	}
	
	public TagResource(String id, Set<String> tags) {
		setUri(id);
		this.tags = tags;
	}

	/** @deprecated use getUri */
	public String getId() {
		return getUri();
	}

	public Set<String> getTags() {
		return tags;
	}

}
