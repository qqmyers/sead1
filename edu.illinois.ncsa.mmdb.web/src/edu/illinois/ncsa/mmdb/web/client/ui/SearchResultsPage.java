/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.PagingDatasetTablePresenter;
import edu.illinois.ncsa.mmdb.web.client.PagingSearchResultsTableView;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.dispatch.Search;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SearchResult;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetEvent;
import edu.illinois.ncsa.mmdb.web.client.place.PlaceService;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * Page to handle search results.
 * 
 * @author Luigi Marini
 * 
 */
public class SearchResultsPage extends Page {

	private static final String TITLE = "Search Results";
	private PagingSearchResultsTableView datasetTableView;
	private final HandlerManager eventbus;

	public SearchResultsPage(MyDispatchAsync dispatchasync, HandlerManager eventbus) {
		super(TITLE, dispatchasync);
		this.eventbus = eventbus;
		String query = PlaceService.getParams().get("q");
		if (query != null) {
			mainLayoutPanel.add(new HTML("Your search for <b>" + query
					+ "</b> returned the following results:"));
			queryServer(query);
		}
		// paged table of datasets
		datasetTableView = new PagingSearchResultsTableView();
		datasetTableView.addStyleName("datasetTable");
		
		PagingDatasetTablePresenter datasetTablePresenter =
			new PagingDatasetTablePresenter(datasetTableView, eventbus);
		datasetTablePresenter.bind();
		
		mainLayoutPanel.add(datasetTableView);
	}

	private void queryServer(String query) {
		dispatchAsync.execute(new Search(query), new AsyncCallback<SearchResult>() {

			@Override
			public void onFailure(Throwable caught) {
				GWT.log("Error executing search", caught);
			}

			@Override
			public void onSuccess(SearchResult result) {
				showResults(result);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.illinois.ncsa.mmdb.web.client.ui.Page#layout()
	 */
	@Override
	public void layout() {

	}

	protected void showResults(SearchResult result) {
		List<String> hits = result.getHits();
		for (String hit : hits) {
			MMDB.dispatchAsync.execute(new GetDataset(hit),
					new AsyncCallback<GetDatasetResult>() {

						@Override
						public void onFailure(Throwable caught) {
							GWT.log("Error getting dataset", caught);
						}

						@Override
						public void onSuccess(GetDatasetResult result) {
							DatasetBean dataset = result.getDataset();
							GWT.log("Sending event add dataset " + dataset.getTitle(), null);
							AddNewDatasetEvent event = new AddNewDatasetEvent();
							event.setDataset(dataset);
							MMDB.eventBus.fireEvent(event);				
						}
			});
		}
	}
}
