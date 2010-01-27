/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleMatcher;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.bard.jaas.PasswordDigest;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBAC;

/**
 * Create admin user and guest user if not in the system.
 * 
 * FIXME: add roles to admin and guest account and not specific permissions
 * 
 * @author Luigi Marini
 *
 */
public class ContextSetupListener implements ServletContextListener {

	private static final String ADMIN_ID = "admin";
	private static final String ADMIN_NAME = "admin";
	private static final String ADMIN_PASSWORD = "admin";
	private static final String GUEST_ID = "guest";
	private static final String GUEST_NAME = "guest";
	private static final String GUEST_PASSWORD = "guest";

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		
		BeanSession beanSession = TupeloStore.getInstance().getBeanSession();
		
		PersonBeanUtil pbu = new PersonBeanUtil(beanSession);
		
		RBAC rbac = new RBAC(TupeloStore.getInstance().getContext());

			try {
				// admin account
				PersonBean admin = pbu.get(PersonBeanUtil.getPersonID(ADMIN_ID));
				admin.setName(ADMIN_NAME);
				beanSession.save(admin);
				checkAdminPassword(admin, ADMIN_PASSWORD, false);
				rbac.addPermission(Resource.uriRef(admin.getUri()), MMDB.VIEW_ADMIN_PAGES);
				rbac.addPermission(Resource.uriRef(admin.getUri()), MMDB.VIEW_MEMBER_PAGES);
				
				// guest account
				PersonBean guest = pbu.get(PersonBeanUtil.getPersonID(GUEST_ID));
				guest.setName(GUEST_NAME);
				beanSession.save(guest);
				checkAdminPassword(guest, GUEST_PASSWORD, true);
				rbac.addPermission(Resource.uriRef(guest.getUri()), MMDB.VIEW_MEMBER_PAGES);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	/**
	 * If admin user doesn't have a password, set password to default.
	 * 
	 * @param account
	 */
	private void checkAdminPassword(PersonBean account, String defaultPassword, boolean overwrite) {
		Resource user = Resource.uriRef(account.getUri());
		Resource predicate = MMDB.HAS_PASSWORD;
		String passwordDigest = PasswordDigest.digest(defaultPassword);
		Context context = TupeloStore.getInstance().getContext();
		
		// remove old password digests
		TripleMatcher tripleMatcher = new TripleMatcher();
		tripleMatcher.setSubject(user);
		tripleMatcher.setPredicate(predicate);
		try {
			context.perform(tripleMatcher);
			if (tripleMatcher.getResult().size() == 0 || overwrite) {
				context.removeTriples(tripleMatcher.getResult());
				context.addTriple(user, predicate, passwordDigest);
			}
		} catch (OperatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
