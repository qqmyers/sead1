package edu.illinois.ncsa.mmdb.web.client.view;

public class CreateCollectionDialogView extends TextDialogView {
    public CreateCollectionDialogView(String title) {
        super();
        setText(title);
    }

    public CreateCollectionDialogView() {
        setText("Create collection");
    }
}
