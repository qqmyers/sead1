/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.Set;

import net.customware.gwt.dispatch.shared.Result;

/**
 * @author lmarini
 *
 */
public class TagResourceResult implements Result {

	private static final long serialVersionUID = -3627101836501807107L;

	Set<String> tags;
	
	public TagResourceResult() {
		// TODO Auto-generated constructor stub
	}

	public TagResourceResult(Set<String> tags) {
		setTags(tags);
	}

	public Set<String> getTags() {
		return tags;
	}

	public void setTags(Set<String> tags) {
		this.tags = tags;
	}
	
	
}
