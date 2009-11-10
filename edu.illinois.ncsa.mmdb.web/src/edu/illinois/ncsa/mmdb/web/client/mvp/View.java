/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.mvp;

import com.google.gwt.user.client.ui.Widget;

/**
 * View for MVP pattern.
 * 
 * @author Luigi Marini
 *
 */
public interface View {

	/**
	 * Get view as GWT Widget.
	 * 
	 * @return this as widget
	 */
	Widget asWidget();
}
