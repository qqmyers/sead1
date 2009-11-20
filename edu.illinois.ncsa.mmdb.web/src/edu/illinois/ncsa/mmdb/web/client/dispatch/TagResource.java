/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.Set;

import net.customware.gwt.dispatch.shared.Action;

/**
 * @author lmarini
 *
 */
public class TagResource implements Action<TagResourceResult>{

	private static final long serialVersionUID = 2375836350874802386L;
	private String id;
	private Set<String> tags;
	
	public TagResource() {
		// TODO Auto-generated constructor stub
	}
	
	public TagResource(String id, Set<String> tags) {
		this.id = id;
		this.tags = tags;
	}

	public String getId() {
		return id;
	}

	public Set<String> getTags() {
		return tags;
	}

}
