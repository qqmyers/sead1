/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Action;

/**
 * Request recent activity for a particular user.
 * 
 * @author Luigi Marini
 * 
 */
@SuppressWarnings("serial")
public class GetRecentActivity implements Action<GetRecentActivityResult> {

    private String user;
    private int    maxNum;

    public GetRecentActivity() {
    }

    public GetRecentActivity(String user, int maxNum) {
        this.user = user;
        this.maxNum = maxNum;
    }

    public String getUser() {
        return user;
    }

    public int getMaxNum() {
        return maxNum;
    }
}
