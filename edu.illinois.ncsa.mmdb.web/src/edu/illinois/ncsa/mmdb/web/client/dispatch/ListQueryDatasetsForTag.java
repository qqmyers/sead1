package edu.illinois.ncsa.mmdb.web.client.dispatch;

import edu.uiuc.ncsa.cet.bean.DatasetBean;

@SuppressWarnings("serial")
public class ListQueryDatasetsForTag extends ListQuery<DatasetBean> {

    private String tagName;

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTagName() {
        return tagName;
    }

}
