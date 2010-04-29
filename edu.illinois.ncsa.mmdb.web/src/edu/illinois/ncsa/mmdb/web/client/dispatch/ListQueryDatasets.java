/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * @author lmarini
 * 
 */
@SuppressWarnings("serial")
public class ListQueryDatasets extends ListQuery<DatasetBean> {

    private String inCollection;

    public void setInCollection(String inCollection) {
        this.inCollection = inCollection;
    }

    public String getInCollection() {
        return inCollection;
    }

}
