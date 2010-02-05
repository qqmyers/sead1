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
	private boolean delete = false;
	
	public TagResource() {
		// TODO Auto-generated constructor stub
	}
	
	public TagResource(String id, Set<String> tags) {
		setUri(id);
		this.tags = tags;
	}
	
	public TagResource(String id, Set<String> tags, boolean delete) {
		setUri(id);
		this.tags = tags;
		setDelete(delete);
	}

	/** @deprecated use getUri */
	public String getId() {
		return getUri();
	}

	public Set<String> getTags() {
		return tags;
	}

	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}
}
