package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

public class DerivedDatasetsWidget extends Composite {
	FlexTable mainContainer;
	private final Label titleLabel;

	int n = 1;
	
	public DerivedDatasetsWidget() {
		mainContainer = new FlexTable();
		mainContainer.addStyleName("datasetRightColSection");
		initWidget(mainContainer);

		titleLabel = new Label("Derived from");
		titleLabel.addStyleName("datasetRightColHeading");
		mainContainer.setWidget(0, 0, titleLabel);
	}

	public void addDataset(DatasetBean ds) {
		String url = "dataset?id="+ds.getUri();
		PreviewWidget pw = new PreviewWidget(ds.getUri(), GetPreviews.SMALL, url);
		Hyperlink link = new Hyperlink(ds.getTitle(), url);
		mainContainer.setWidget(n++, 0, pw);
		mainContainer.setWidget(n++, 0, link);
	}
}
