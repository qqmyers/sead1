package edu.illinois.ncsa.mmdb.web.client;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.PagingTablePresenter.Display;
import edu.illinois.ncsa.mmdb.web.client.ui.PagingWidget;

public abstract class PagingTableView<T> extends Composite implements Display<T> {
	VerticalPanel mainPanel;
	
	List<PagingWidget> pagingControls;
	
	protected HorizontalPanel topPagingPanel;
	protected VerticalPanel middlePanel;
	protected HorizontalPanel bottomPagingPanel;
	
	protected PagingTableView() {
		//
		mainPanel = new VerticalPanel(); 
		topPagingPanel = new HorizontalPanel();
		middlePanel = new VerticalPanel();
		bottomPagingPanel = new HorizontalPanel();
		mainPanel.add(topPagingPanel);
		mainPanel.add(middlePanel);
		mainPanel.add(bottomPagingPanel);
		//
		pagingControls = new LinkedList<PagingWidget>();
		
		initWidget(mainPanel);
	}
	
	public PagingTableView(int page) {
		this();
		topPagingPanel.add(createPagingPanel(page));
		bottomPagingPanel.add(createPagingPanel(page));
	}

	protected void addPagingControl(PagingWidget control) {
		pagingControls.add(control);
	}
	
	protected HorizontalPanel createPagingPanel(int page) {
		HorizontalPanel panel = new HorizontalPanel();
		panel.addStyleName("datasetsPager");
		panel.addStyleName("centered"); // special IE-friendly centering style

		PagingWidget pagingWidget = new PagingWidget();
		pagingWidget.setPage(page);
		pagingControls.add(pagingWidget);
		panel.add(pagingWidget);
		return panel;
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}

	public void addPageChangeHandler(ValueChangeHandler<Integer> handler) {
		for(HasValueChangeHandlers<Integer> control : pagingControls) {
			control.addValueChangeHandler(handler);
		}
	}
	
	public void setNumberOfPages(int p) {
		for(PagingWidget control : pagingControls) {
			control.setNumberOfPages(p);
		}
	}
}
