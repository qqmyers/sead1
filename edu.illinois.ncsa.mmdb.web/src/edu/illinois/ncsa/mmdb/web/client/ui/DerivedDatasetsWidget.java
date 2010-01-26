package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;

import edu.uiuc.ncsa.cet.bean.DatasetBean;

public class DerivedDatasetsWidget extends Composite {
	FlowPanel mainContainer;
	private final Label titleLabel;

	public DerivedDatasetsWidget() {
		mainContainer = new FlowPanel();
		mainContainer.addStyleName("datasetRightColSection");
		initWidget(mainContainer);

		titleLabel = new Label("Derived from");
		titleLabel.addStyleName("datasetRightColHeading");
		mainContainer.add(titleLabel);
	}

	public void addDataset(DatasetBean ds) {
		Hyperlink link = new Hyperlink(ds.getTitle(), "dataset?id="+ds.getUri());
		mainContainer.add(link);
	}
}
