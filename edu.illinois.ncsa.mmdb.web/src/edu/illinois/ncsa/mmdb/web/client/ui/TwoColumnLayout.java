/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;


import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A layout comprised of a wide column on the left and a narrow column on the right.
 * 
 * @author Luigi Marini
 *
 */
public class TwoColumnLayout extends Composite {

	private final Widget leftColumn;
	private final Widget righColumn;
	private FlowPanel mainLayoutPanel;

	public TwoColumnLayout(Widget leftColumn, Widget righColumn) {
		this.leftColumn = leftColumn;
		this.righColumn = righColumn;
		
		// main layout
		mainLayoutPanel = new FlowPanel();
		mainLayoutPanel.addStyleName("twoColumnLayout");
		initWidget(mainLayoutPanel);
		
		// left column
		mainLayoutPanel.add(leftColumn);
		leftColumn.addStyleName("twoColumnLayout-leftCol");
		
		// right column
		mainLayoutPanel.add(righColumn);
		righColumn.addStyleName("twoColumnLayout-rightCol");
		
		// clear bottom
		SimplePanel clearBothPanel = new SimplePanel();
		clearBothPanel.addStyleName("clearBoth");
		mainLayoutPanel.add(clearBothPanel);
	}
}
