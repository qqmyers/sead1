/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.mvp;

import com.google.gwt.event.shared.HandlerManager;

/**
 * Base presenter. Should be extended specific presenters.
 * 
 * @author Luigi Marini
 *
 */
public class BasePresenter<D extends View> implements Presenter {

	protected D display;
	protected final HandlerManager eventBus;

	public BasePresenter(D display, HandlerManager eventBus) {
		this.display = display;
		this.eventBus = eventBus;
	}
	
	/* (non-Javadoc)
	 * @see edu.illinois.ncsa.mmdb.web.client.mvp.Presenter#bindDisplay(edu.illinois.ncsa.mmdb.web.client.mvp.Display)
	 */
	@Override
	public void bind() {
		// TODO Auto-generated method stub

	}

	@Override
	public View getView() {
		return display;
	}

}
