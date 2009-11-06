package edu.illinois.ncsa.mmdb.web.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;

public class DownloadButton extends Button implements ClickHandler {
    private String uri;

    public DownloadButton(String uri) {
        this();
        setText("Download");
        setUri(uri);
    }

    public DownloadButton() {
        super();
        addClickHandler(this);
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
    public String getUri() {
        return uri;
    }

    /**
     * Called when a native click event is fired.
     *
     * @param event the {@link com.google.gwt.event.dom.client.ClickEvent} that was fired
     */
    @Override
    public void onClick(ClickEvent event) {
        Window.alert("user wants to download "+getUri());
    }
}