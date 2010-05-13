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
    private String withTag;

    public void setInCollection(String inCollection) {
        this.inCollection = inCollection;
    }

    public String getInCollection() {
        return inCollection;
    }

    public void setWithTag(String withTag) {
        this.withTag = withTag;
    }

    public String getWithTag() {
        return withTag;
    }

}
