/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;


/**
 * Get all collections.
 * 
 * @author Luigi Marini
 *
 */
@SuppressWarnings("serial")
public class GetCollections extends SubjectAction<GetCollectionsResult> {
	String sortKey;
	Boolean desc;
	int limit;
	int offset;
	
	public GetCollections() {}

	public GetCollections(String uri) {
		setUri(uri);
	}
	
	/**
	 * @return the uri
	 */
	public String getMemberUri() {
		return getUri();
	}

	public String getSortKey() {
		return sortKey;
	}

	public void setSortKey(String sortKey) {
		this.sortKey = sortKey;
	}

	public boolean isDesc() {
		return desc;
	}

	public void setDesc(boolean desc) {
		this.desc = desc;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	
}
