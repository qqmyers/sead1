package edu.illinois.ncsa.mmdb.web.client;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;

import edu.illinois.ncsa.mmdb.web.client.ui.LabeledListBox;
import edu.illinois.ncsa.mmdb.web.client.ui.PagingWidget;

/**
 * TODO Add comments
 * 
 * @author Joe Futrelle
 *
 * @param <T>
 */
public abstract class PagingDcThingView<T> extends PagingTableView<T> {
	protected LabeledListBox sortOptions;
	protected LabeledListBox viewOptions;
	LinkedList<LabeledListBox> sortControls;
	LinkedList<LabeledListBox> viewTypeControls;
	
	/**
	 * Parse the parameters in the history token after the '?'
	 * 
	 * @return
	 */
	Map<String, String> getParams(String token) {
		Map<String, String> params = new HashMap<String, String>();
		String paramString = token.substring(token.indexOf("?") + 1);
		if (!paramString.isEmpty()) {
			for (String paramEntry : paramString.split("&")) {
				String[] terms = paramEntry.split("=");
				if (terms.length == 2) {
					params.put(terms[0], terms[1]);
				}
			}
		}
		return params;
	}
	
	public abstract String getAction();
	
	public static String getDefaultSortKey() {
		return "date-desc";
	}
	
	// gets it from session preferences
	public static String getDefaultViewType() {
		return MMDB.getSessionPreference("viewType","list");
	}
	
	protected boolean descForSortKey() {
		return !sortKey.endsWith("-asc"); // default is descending
	}

	int page = 1;
	String sortKey = getDefaultSortKey();
	String viewType = getDefaultViewType();
	
	public String getViewType() {
		return viewType;
	}

	public void setViewType(String viewType) {
		this.viewType = viewType;
		// set this as the preferred view type in session preferences
		MMDB.setSessionPreference("viewType",viewType);
		// avoid retriggering a change event and just update the ifc
		for(LabeledListBox vtc : viewTypeControls) {
			vtc.setSelected(viewType != null ? viewType : getDefaultViewType());
		}
	}

	protected boolean isViewValid = false;
	protected boolean isPageValid = false;

	@Override
	protected void onAttach() {
		super.onAttach();
		invalidateView();
		displayAll();
	}

	protected void invalidatePage() {
		isPageValid = false;
	}
	public void invalidateView() {
		isViewValid = false;
		isPageValid = false;
	}
	
	protected Map<String,String> parseHistoryToken(String historyToken) {
		Map<String,String> params = getParams(historyToken);
		if(!isPageValid) {
			page = params.containsKey("page") ? Integer.parseInt(params.get("page")) : 1;
		}
		if(!isViewValid) {
			sortKey = params.containsKey("sort") ? params.get("sort") : getDefaultSortKey();
			if(params.containsKey("view")) {
				setViewType(params.get("view"));
			} else if(getViewType() == null) {
				setViewType(getDefaultViewType());
			}
		}
		return params;
	}

	protected String getHistoryToken() {
		return getAction()+"?page="+page+"&sort="+sortKey+"&view="+viewType;
	}
	
	protected abstract void displayView();
	
	protected abstract void displayPage();
	
	public void displayAll() {
		displayAll(History.getToken());
	}
	public void displayAll(String historyToken) {
		parseHistoryToken(historyToken);
		if(!isViewValid) {
			displayView();
			isViewValid = true;
		}
		if(!isPageValid) {
			displayPage();
			isPageValid = true;
		}
	}
	
	void setPage(int page) {
		this.page = page;
		for(PagingWidget w : pagingControls) {
			w.setPage(page, false); // avoid infinite loop
		}
	}
	
	public PagingDcThingView() {
		super();
		
		sortControls = new LinkedList<LabeledListBox>();
		viewTypeControls = new LinkedList<LabeledListBox>();
		
		parseHistoryToken(History.getToken());
		
		topPagingPanel.add(createPagingPanel(page, sortKey, getViewType()));
		bottomPagingPanel.add(createPagingPanel(page, sortKey, getViewType()));
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			public void onValueChange(ValueChangeEvent<String> event) {
				displayAll(event.getValue());
			}
		});
		
		addPageChangeHandler(new ValueChangeHandler<Integer>() {
			public void onValueChange(ValueChangeEvent<Integer> event) {
				setPage(event.getValue());
				invalidatePage();
				History.newItem(getHistoryToken());
			}
		});
		
		addViewTypeChangeHandler(new ValueChangeHandler<String>() {
			public void onValueChange(ValueChangeEvent<String> event) {
				setPage(1);
				setViewType(event.getValue());
				invalidateView();
				History.newItem(getHistoryToken());
			}
		});
		
		addSortKeyChangeHandler(new ValueChangeHandler<String>() {
			public void onValueChange(ValueChangeEvent<String> event) {
				sortKey = event.getValue();
				invalidateView();
				History.newItem(getHistoryToken());
			}
		});
	}
	
	protected void addSortControl(LabeledListBox control) {
		sortControls.add(control);
	}		
	
	protected void addViewTypeControl(LabeledListBox control) {
		viewTypeControls.add(control);
	}		
	
	protected HorizontalPanel createPagingPanel(int page, String sortKey, String viewType) {
		
		HorizontalPanel panel = createPagingPanel(page);
		panel.addStyleName("redBorder");
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		
		sortOptions = new LabeledListBox("Sort by: ");
		sortOptions.addStyleName("pagingLabel");
		sortOptions.addItem("Date: newest first", "date-desc");
		sortOptions.addItem("Date: oldest first", "date-asc");
		sortOptions.addItem("Title: A-Z", "title-asc");
		sortOptions.addItem("Title: Z-A", "title-desc");
		sortOptions.setSelected(sortKey);
		addSortControl(sortOptions);
		panel.add(sortOptions);
		
		viewOptions = new LabeledListBox("View:");
		viewOptions.addStyleName("pagingLabel");
		viewOptions.addItem("List", "list");
		viewOptions.addItem("Grid", "grid");
		viewOptions.addItem("Flow", "flow");
		viewOptions.setSelected(viewType);
		addViewTypeControl(viewOptions);
		panel.add(viewOptions);
		
		addViewTypeChangeHandler(new ValueChangeHandler<String>() {
			public void onValueChange(ValueChangeEvent<String> event) {
				viewOptions.setSelected(event.getValue());
			}
		});
		
		addSortKeyChangeHandler(new ValueChangeHandler<String>() {
			public void onValueChange(ValueChangeEvent<String> event) {
				sortOptions.setSelected(event.getValue());
			}
		});
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
