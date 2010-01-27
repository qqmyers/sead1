/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Result;
import edu.uiuc.ncsa.cet.bean.PersonBean;

/**
 * @author Luigi Marini
 *
 */
@SuppressWarnings("serial")
public class GetUserResult implements Result {

	private PersonBean personBean;

	public GetUserResult() {}
	
	public GetUserResult(PersonBean personBean) {
		this.personBean = personBean;
	}

	/**
	 * @return the personBean
	 */
	public PersonBean getPersonBean() {
		return personBean;
	}
}
