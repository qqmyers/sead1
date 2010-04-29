/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.List;

import net.customware.gwt.dispatch.shared.Result;

/**
 * @author lmarini
 * 
 */
@SuppressWarnings("serial")
public class ListQueryResult<T> implements Result {
    private List<T> results;
    private int     totalCount;

    public ListQueryResult() {
    }

    public ListQueryResult(List<T> result) {
        setResults(result);
    }

    public void setResults(List<T> results) {
        this.results = results;
    }

    public List<T> getResults() {
        return results;
    }

    public void setTotalCount(int count) {
        this.totalCount = count;
    }

    public int getTotalCount() {
        return totalCount;
    }

}
