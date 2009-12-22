package edu.illinois.ncsa.mmdb.web.client.ui;


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class PagingWidget extends Composite implements ClickHandler, HasValueChangeHandlers<Integer> {
	Image firstButton;
	Image previousButton;
	Image nextButton;
	Label pageLabel;

	// paging model
	int page = 1;
	int nPages = -1;
	
	public void setPage(int p) {
		page = p >= 1 ? p : 1;
		setPageLabel(page,nPages);
		ValueChangeEvent.fire(this, page);
	}
	public void setNumberOfPages(int p) {
		nPages = p;
		setPageLabel(page,nPages);
	}
	public int getNumberOfPages() {
		return nPages;
	}
	void setPageLabel(int p, int np) {
		pageLabel.setText("page "+p+(np > 0 ? " of " + np : ""));
	}
	public int getPage() { return page; }
	
	public PagingWidget() {
		this(1);
	}
	public PagingWidget(int p) {
		page = p;
		HorizontalPanel thePanel = new HorizontalPanel();
		thePanel.addStyleName("pagingWidget");
		firstButton = new Image("images/go-first.png");
		previousButton = new Image("images/go-previous.png");
		nextButton = new Image("images/go-next.png");
		pageLabel = new Label();
		pageLabel.addStyleName("pagingLabel");
		setPageLabel(page,nPages);
		for(Widget element : new Widget[] { firstButton, previousButton, pageLabel, nextButton }) {
			thePanel.add(element);
			if(element instanceof Image) {
				((Image)element).addClickHandler(this);
			}
		}
		initWidget(thePanel);
	}
	
	public void onClick(ClickEvent event) {
		Widget w = (Widget) event.getSource();
		if(w == firstButton) {
			setPage(1);
		} else if(w == previousButton) {
			if(getPage() > 1) {
				setPage(getPage()-1);
			}
		} else if(w == nextButton) {
			if(getNumberOfPages() == -1 || getPage() < getNumberOfPages()) {
				setPage(getPage()+1);
			}
		}
	}

	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Integer> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}
}

