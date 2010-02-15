package edu.illinois.ncsa.mmdb.web.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;

@Deprecated
public class DownloadButton extends Button implements ClickHandler {
    private String uri;

    public DownloadButton(String uri) {
        this();
        setText("Download full size");
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
        // this is sort of download-y, but we need to bounce off a service that sets content-disposition,
        // best candidate is REST service
        Window.open(getUri(), "_blank", "");
    }
}