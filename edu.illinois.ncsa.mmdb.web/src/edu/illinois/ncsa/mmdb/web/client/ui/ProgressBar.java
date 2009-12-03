package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ProgressBar extends Composite {
	HTML theBar;
	
	public ProgressBar() {
		VerticalPanel contentPanel = new VerticalPanel();
		theBar = new HTML();
		contentPanel.add(theBar);
		setProgress(0);
		initWidget(contentPanel);
	}

	public void setProgress(int i) {
		StringBuffer html = new StringBuffer();
		html.append("<table width='200px' border='0px'><tr>");
		html.append("<td width='"+i+"%' style='background-color:00ff00'>&nbsp;</td>");
		html.append("<td width='"+(100-i)+"%'>&nbsp;</td>");
		html.append("</tr></table>");
		theBar.setHTML(html.toString());
	}
}
