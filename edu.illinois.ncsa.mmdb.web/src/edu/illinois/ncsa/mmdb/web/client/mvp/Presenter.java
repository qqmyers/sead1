/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.mvp;

/**
 * Presenter in MVP pattern.
 * 
 * @author Luigi Marini
 *
 */
public interface Presenter {

	/**
	 * Get the view associated with this presenter.
	 * @return the view
	 */
	View getView();
	
	/**
	 * Bind presenter to event bus.
	 */
	void bind();
	
	
}
