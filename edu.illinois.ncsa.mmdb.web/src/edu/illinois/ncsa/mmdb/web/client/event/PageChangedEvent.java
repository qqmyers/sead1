package edu.illinois.ncsa.mmdb.web.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class PageChangedEvent extends GwtEvent<PageChangedHandler> {
	public static final GwtEvent.Type<PageChangedHandler> TYPE = new GwtEvent.Type<PageChangedHandler>();

	int page;
	
	protected void dispatch(PageChangedHandler handler) {
		handler.onPageChanged(this);
	}

	@Override
	public GwtEvent.Type<PageChangedHandler> getAssociatedType() {
		// TODO Auto-generated method stub
		return TYPE;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}
}
