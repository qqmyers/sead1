package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Action;

public class ListDatasets implements Action<ListDatasetsResult> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2922437353231426177L;

	public ListDatasets() { }
	
	private String orderBy;
	private boolean desc;
	private int limit;
	private int offset;
	
	public ListDatasets(String orderBy, boolean desc, int limit, int offset) {
		this.orderBy = orderBy;
		this.desc = desc;
		this.limit = limit;
		this.offset = offset;
	}
	
	public void setOrderBy(String s) { orderBy=s; }
	public String getOrderBy() { return orderBy; }
	public void setDesc(boolean b) { desc = b; }
	public boolean getDesc() { return desc; }
	public void setLimit(int i) { limit=i; }
	public int getLimit() { return limit; }
	public void setOffset(int i) { offset=i; }
	public int getOffset() { return offset; }
}
