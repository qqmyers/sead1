/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.DefaultActionHandlerRegistry;
import net.customware.gwt.dispatch.server.DefaultDispatch;
import net.customware.gwt.dispatch.server.Dispatch;

/**
 * Registry of action handlers.
 * 
 * @author Luigi Marini
 * 
 */
public class DispatchUtil {

	private static final DefaultActionHandlerRegistry REGISTRY = new DefaultActionHandlerRegistry();

	private static final Dispatch DISPATCH = new DefaultDispatch(REGISTRY);

	public static void registerHandler(ActionHandler<?, ?> handler) {
		REGISTRY.addHandler(handler);
	}

	public static Dispatch getDispatch() {
		return DISPATCH;
	}

}
