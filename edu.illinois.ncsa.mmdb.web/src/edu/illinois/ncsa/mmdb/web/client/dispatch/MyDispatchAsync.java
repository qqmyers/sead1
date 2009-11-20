/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.dispatch.client.service.DispatchService;
import net.customware.gwt.dispatch.client.service.DispatchServiceAsync;
import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Default dispatcher.
 * 
 * @author Luigi Marini
 */
public class MyDispatchAsync implements DispatchAsync {

	private static final DispatchServiceAsync realService = GWT
			.create(DispatchService.class);

	public MyDispatchAsync() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public <A extends Action<R>, R extends Result> void execute(final A action,
			final AsyncCallback<R> callback) {

		realService.execute(action, new AsyncCallback<Result>() {

			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}

			public void onSuccess(Result result) {
				callback.onSuccess((R) result);
				GWT.log("Command " + action.getClass().getName()
						+ " successfully executed.", null);
			}

		});

	}

}
