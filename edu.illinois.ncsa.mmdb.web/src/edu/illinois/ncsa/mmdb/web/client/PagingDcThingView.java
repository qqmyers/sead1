package edu.illinois.ncsa.mmdb.web.client;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;

import edu.illinois.ncsa.mmdb.web.client.ui.LabeledListBox;

public abstract class PagingDcThingView<T> extends PagingTableView<T> {
	LabeledListBox sortOptions;
	LabeledListBox viewOptions;
	List<HasValueChangeHandlers<String>> sortControls;
	List<HasValueChangeHandlers<String>> viewTypeControls;
	
	public PagingDcThingView(int page, String sortKey, String viewType) {
		super();
		
		sortControls = new LinkedList<HasValueChangeHandlers<String>>();
		viewTypeControls = new LinkedList<HasValueChangeHandlers<String>>();
		
		topPagingPanel.add(createPagingPanel(page, sortKey, viewType));
		bottomPagingPanel.add(createPagingPanel(page, sortKey, viewType));
	}
	
	protected void addSortControl(HasValueChangeHandlers<String> control) {
		sortControls.add(control);
	}		
	
	protected void addViewTypeControl(HasValueChangeHandlers<String> control) {
		viewTypeControls.add(control);
	}		
	
	protected HorizontalPanel createPagingPanel(int page, String sortKey, String viewType) {
		HorizontalPanel panel = createPagingPanel(page);
	
		sortOptions = new LabeledListBox("sort by: ");
		sortOptions.addStyleName("pagingLabel");
		sortOptions.addItem("Date: newest first", "date-desc");
		sortOptions.addItem("Date: oldest first", "date-asc");
		sortOptions.addItem("Title: A-Z", "title-asc");
		sortOptions.addItem("Title: Z-A", "title-desc");
		sortOptions.setSelected(sortKey);
		addSortControl(sortOptions);
		panel.add(sortOptions);
		
		viewOptions = new LabeledListBox("view:");
		viewOptions.addStyleName("pagingLabel");
		viewOptions.addItem("list", "list");
		viewOptions.addItem("grid", "grid");
		viewOptions.setSelected(viewType);
		addViewTypeControl(viewOptions);
		panel.add(viewOptions);
		
		return panel;
	}
	
	public void addViewTypeChangeHandler(ValueChangeHandler<String> handler) {
		for(HasValueChangeHandlers<String> control : viewTypeControls) {
			control.addValueChangeHandler(handler);
		}
	}

	public void addSortKeyChangeHandler(ValueChangeHandler<String> handler) {
		for(HasValueChangeHandlers<String> control : sortControls) {
			control.addValueChangeHandler(handler);
		}
	}
}
