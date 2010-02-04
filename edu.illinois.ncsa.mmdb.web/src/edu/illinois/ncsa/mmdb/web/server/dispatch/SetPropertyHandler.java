package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.Collection;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.ThingSession;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.mmdb.web.client.dispatch.SetProperty;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetPropertyResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

/**
 * TODO Add comments
 * 
 * @author Joe Futrelle
 *
 */
public class SetPropertyHandler implements ActionHandler<SetProperty, SetPropertyResult> {

	/** Commons logging **/
	private static Log log = LogFactory.getLog(SetPropertyHandler.class);

	@Override
	public SetPropertyResult execute(SetProperty arg0, ExecutionContext arg1)
			throws ActionException {
		try {
			Resource subject = Resource.uriRef(arg0.getUri());
			Resource predicate = Resource.uriRef(arg0.getPropertyUri());
			Collection<String> values = arg0.getValues();
			
			//
			ThingSession ts = new ThingSession(TupeloStore.getInstance().getContext());
			ts.setValues(subject, predicate, values);
			ts.save();
			ts.close();
			
			// attempt to refetch the bean
			try {
				TupeloStore.refetch(subject);
			} catch(Exception x) {
				x.printStackTrace();
			}
			
			return new SetPropertyResult();
		} catch(Exception x) {
			log.error("Error setting metadata on " + arg0.getUri(), x);
			throw new ActionException("failed", x);
		}
	}

	@Override
	public Class<SetProperty> getActionType() {
		return SetProperty.class;
	}

	@Override
	public void rollback(SetProperty arg0, SetPropertyResult arg1,
			ExecutionContext arg2) throws ActionException {
		// TODO Auto-generated method stub
		
	}

}
