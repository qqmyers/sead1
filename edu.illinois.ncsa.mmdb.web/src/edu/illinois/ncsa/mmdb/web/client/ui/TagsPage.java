/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.user.client.ui.Label;

/**
 * A page listing all tags in the system.
 * 
 * @author Luigi Marini
 *
 */
public class TagsPage extends Page {

	public TagsPage(DispatchAsync dispatchAsync) {
		super("Tags", dispatchAsync);
		getTags();
	}

	private void getTags() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void layout() {
		mainLayoutPanel.add(new Label("Work in progress"));
	}
}
