package edu.illinois.ncsa.mmdb.web.client;

import com.google.gwt.event.shared.HandlerManager;

import edu.illinois.ncsa.mmdb.web.client.mvp.BasePresenter;
import edu.illinois.ncsa.mmdb.web.client.mvp.View;

public class PagingTablePresenter<T> extends BasePresenter<PagingTablePresenter.Display<T>> {

    public PagingTablePresenter(Display<T> display, HandlerManager eventBus) {
        super(display, eventBus);
    }

    @Override
    public void bind() {
        super.bind();
    }

    public interface Display<T> extends View {
        void addItem(String uri, T item);

        void addItem(String uri, T item, int position);
    }
}
