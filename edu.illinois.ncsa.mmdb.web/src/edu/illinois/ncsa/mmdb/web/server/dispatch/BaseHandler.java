/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.ActionException;
import net.customware.gwt.dispatch.shared.Result;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;

import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

/**
 * @author Luigi Marini
 *
 */
public class BaseHandler<A extends Action<R>, R extends Result> implements ActionHandler<A, R> {

	/** Tupelo bean session **/
	private static final BeanSession beanSession = TupeloStore.getInstance()
			.getBeanSession();
	
	/** Commons logging **/
	private static Log log = LogFactory.getLog(BaseHandler.class);
	
	@Override
	public R execute(A arg0, ExecutionContext arg1) throws ActionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<A> getActionType() {
		return null;
	}

	@Override
	public void rollback(A arg0, R arg1, ExecutionContext arg2)
			throws ActionException {
		// TODO Auto-generated method stub
		
	}


}
