/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010, NCSA.  All rights reserved.
 *
 * Developed by:
 * Cyberenvironments and Technologies (CET)
 * http://cet.ncsa.illinois.edu/
 *
 * National Center for Supercomputing Applications (NCSA)
 * http://www.ncsa.illinois.edu/
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the names of CET, University of Illinois/NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *******************************************************************************/
package edu.illinois.ncsa.mmdb.web.client.ui;

//Added a few imports to be used in fetching JSON from VIVO - Ram
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SimpleHtmlSanitizer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.ParentJson;
import edu.illinois.ncsa.mmdb.web.client.Results;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ConfigurationResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetConfiguration;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetSection;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetSectionResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetSubclasses;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetSubclassesResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListUserMetadataFields;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListUserMetadataFieldsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.NamedThing;
import edu.illinois.ncsa.mmdb.web.client.dispatch.RemoveUserMetadata;
import edu.illinois.ncsa.mmdb.web.client.dispatch.Section;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetUserMetadata;
import edu.illinois.ncsa.mmdb.web.client.dispatch.UserMetadataField;
import edu.illinois.ncsa.mmdb.web.client.dispatch.UserMetadataValue;
import edu.illinois.ncsa.mmdb.web.client.event.BatchCompletedEvent;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;

public class AddMetadataWidget extends Composite {
    public String                          uri;
    public final Collection<String>        resources;
    private final DispatchAsync            dispatch;
    ListBox                                fieldChoice;
    //    TextBox                                valueText;
    VerticalPanel                          thePanel;
    Label                                  noFields;
    FlexTable                              fieldTable;
    Map<String, String>                    labels             = new HashMap<String, String>();
    Map<String, String>                    labels_description = new HashMap<String, String>();
    Map<String, Integer>                   indexLabel         = new HashMap<String, Integer>();
    Map<String, Integer>                   listLabel          = new HashMap<String, Integer>();
    private final SimplePanel              newFieldPanel;
    protected InputField                   inputField;
    protected KeyUpHandler                 pressEnter;
    protected SortedSet<UserMetadataField> availableFields;
    private final HandlerManager           eventBus;
    UserMetadataField                      userMetadataField;
    private String                         vivoQueryURL;
    private String                         vivoIDPrefixURL;

    public AddMetadataWidget(String uri, final DispatchAsync dispatch, HandlerManager events) {
        this(new HashSet<String>(), dispatch, events);
        this.resources.add(uri);
        this.uri = uri;
    }

    public AddMetadataWidget(Collection<String> batch, final DispatchAsync dispatch, HandlerManager eventBus) {
        dispatch.execute(new GetConfiguration(null, ConfigurationKey.VIVOQUERYURL, ConfigurationKey.VIVOIDENTIFIERURL), new AsyncCallback<ConfigurationResult>() {

            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(ConfigurationResult result) {
                for (Entry<ConfigurationKey, String> entry : result.getConfiguration().entrySet() ) {
                    switch (entry.getKey()) {
                        case VIVOQUERYURL:
                            vivoQueryURL = entry.getValue();
                            break;
                        case VIVOIDENTIFIERURL:
                            vivoIDPrefixURL = entry.getValue();
                            break;

                        default:
                    }
                }
            }
        });

        this.resources = batch;
        this.dispatch = dispatch;
        this.eventBus = eventBus;

        thePanel = new VerticalPanel();

        // add new field
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.addStyleName("addMetadata");
        verticalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        verticalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        thePanel.add(verticalPanel);

        // list box
        fieldChoice = new ListBox();
        fieldChoice.addStyleName("addMetadataListBox");
        verticalPanel.add(fieldChoice);

        // new field panel
        newFieldPanel = new SimplePanel();
        verticalPanel.add(newFieldPanel);

        // selection handler
        fieldChoice.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                changeHandler();
            }
        });

        initWidget(thePanel);
    }

    /**
     * Called by selection change handler or by clicking Edit
     */
    private void changeHandler() {
        newFieldPanel.clear();
        int index = fieldChoice.getSelectedIndex();
        if (index != 0) {

            ClickHandler addHandler = new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    addValue(true);
                }
            };

            ClickHandler clearHandler = new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    newFieldPanel.clear();
                    fieldChoice.setSelectedIndex(0);
                }
            };
            pressEnter = new KeyUpHandler() {

                @Override
                public void onKeyUp(KeyUpEvent event) {
                    if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                        addValue(true);
                    }

                }
            };
            String query = "";
            // add input widget based on type
            userMetadataField = availableFields.toArray(new UserMetadataField[0])[index - 1];
            switch (userMetadataField.getType()) {
                case UserMetadataField.PLAIN:
                    inputField = new PlainField(userMetadataField, addHandler, clearHandler);
                    newFieldPanel.add(inputField);
                    break;
                case UserMetadataField.DATATYPE:
                    inputField = new PlainField(userMetadataField, addHandler, clearHandler);
                    newFieldPanel.add(inputField);
                    break;
                case UserMetadataField.ENUMERATED:
                    inputField = new ListField(userMetadataField, addHandler, clearHandler);
                    newFieldPanel.add(inputField);
                    break;
                case UserMetadataField.CLASS:
                    inputField = new TreeField(dispatch, userMetadataField, addHandler, clearHandler);
                    newFieldPanel.add(inputField);
                    break;

                //Set handlers to be used by the inner classes for fetching JSON from VIVO
                case UserMetadataField.VIVO_CREATOR:
                case UserMetadataField.VIVO_CONTACT:
                    inputField = new CreatorField(userMetadataField, addHandler, clearHandler);
                    newFieldPanel.add(inputField);
                    //Initialize the connection to VIVO

                    //FIXME : Change this hard coded URL using some ORM-like implementation available for SPARQL in Jena
                    query = "PREFIX+foaf%3A+<http%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F>%0D%0APREFIX+rdf%3A+<http%3A%2F%2Fwww.w3.org%2F1999%2F02%2F22-rdf-syntax-ns%23>%0D%0APREFIX+rdfs%3A+<http%3A%2F%2Fwww.w3.org%2F2000%2F01%2Frdf-schema%23>%0D%0ASELECT+distinct+%3FURL+%3FLabel%0D%0AWHERE%7B%0D%0A%3FURL+rdf%3Atype+foaf%3APerson+.%0D%0A%3FURL+rdfs%3Alabel+%3FLabel+.%0D%0A%7D%0D%0A&output=json"
                            + "&callback=";
                    InitializeVIVOConnection(query);
                    break;
                case UserMetadataField.VIVO_PART_OF:
                    inputField = new PartOfField(userMetadataField, addHandler, clearHandler);
                    newFieldPanel.add(inputField);
                    //Initialize the connection to VIVO
                    query = "PREFIX+bibo%3A+<http%3A%2F%2Fpurl.org%2Fontology%2Fbibo%2F>%0D%0APREFIX+rdf%3A+<http%3A%2F%2Fwww.w3.org%2F1999%2F02%2F22-rdf-syntax-ns%23>%0D%0APREFIX+rdfs%3A+<http%3A%2F%2Fwww.w3.org%2F2000%2F01%2Frdf-schema%23>%0D%0A%0D%0A%0D%0ASELECT+distinct+%3FURL+%3FLabel%0D%0AWHERE{%0D%0A%3FURL+rdf%3Atype+bibo%3AAcademicArticle+.%0D%0A%3FURL+rdfs%3Alabel+%3FLabel+.%0D%0A}%0D%0A&output=json"
                            + "&callback=";
                    InitializeVIVOConnection(query);
                    break;

                case UserMetadataField.MULTILINE_TEXT:
                    inputField = new MultiField(userMetadataField, addHandler, clearHandler);
                    newFieldPanel.add(inputField);
                    break;
                default:
                    inputField = new PlainField(userMetadataField, addHandler, clearHandler);
                    newFieldPanel.add(inputField);
                    break;
            }

        }

    }

    //START - ADDED BY RAM
    private static final String    SERVER_ERROR = "Couldn't retrieve required details. "
                                                        + "This may be as a result of a faulty internet connection or the server "
                                                        + "is experiencing a down time. Please check your network connection and try again.";
    Results                        results;

    private MultiWordSuggestOracle oracle       = null;

    /**
     * Make call to remote server to get the required JSON object.
     */
    public native static void getJson(int requestId, String url, AddMetadataWidget handler) /*-{
		var callback = "callback" + requestId;

		// [1] Create a script element.
		var script = document.createElement("script");
		script.setAttribute("src", url + callback);
		script.setAttribute("type", "text/javascript");

		// [2] Define the callback function on the window object.
		window[callback] = function(jsonObj) {
			// [3]
			handler.@edu.illinois.ncsa.mmdb.web.client.ui.AddMetadataWidget::handleJsonResponse(Lcom/google/gwt/core/client/JavaScriptObject;)(jsonObj);
			window[callback + "done"] = true;
		}

		// [4] JSON download has 3-second timeout.
		setTimeout(
				function() {
					if (!window[callback + "done"]) {
						handler.@edu.illinois.ncsa.mmdb.web.client.ui.AddMetadataWidget::handleJsonResponse(Lcom/google/gwt/core/client/JavaScriptObject;)(null);
					}

					// [5] Cleanup. Remove script and callback elements.
					document.body.removeChild(script);
					delete window[callback];
					delete window[callback + "done"];
				}, 3000);

		// [6] Attach the script element to the document body.
		document.body.appendChild(script);

    }-*/;

    //Called when the getJson method hits the server and a response is obtained
    public void handleJsonResponse(JavaScriptObject jso) {

        if (jso == null) {

            displayMessage(SERVER_ERROR);
            refresh();
            return;
        }

        try {

            ParentJson pJsons = getParentJson(jso);
            results = pJsons.getResults();
            if (results != null) {

                oracle = (MultiWordSuggestOracle) ((SuggestBox) inputField.inputWidget).getSuggestOracle();
            }
            for (int i = 0; i < results.getBindings().length(); i++ ) {
                String name = results.getBindings().get(i).getLabel().getValue();
                String userVivoURL = results.getBindings().get(i).getURL().getValue();

                oracle.add(name + " : " + userVivoURL);
            }
        } catch (Exception e) {

        }
    }

    private void displayMessage(String errorMessage) {
        // TODO Auto-generated method stub
        Window.alert(errorMessage);
    }

    private final native ParentJson getParentJson(JavaScriptObject jso) /*-{
		return jso;
    }-*/;

    private void InitializeVIVOConnection(String query) {
        // Send request to server to get the json object.
        if (!vivoQueryURL.equals("")) {
            getJson(1, vivoQueryURL + query, this);
        }
    }

    //END - ADDED BY RAM

    public void showFields(final boolean canEdit) {
        // FIXME single get to get fields and values
        dispatch.execute(new ListUserMetadataFields(), new AsyncCallback<ListUserMetadataFieldsResult>() {
            public void onFailure(Throwable caught) {
                GWT.log("Error retrieving available list of User Specified Metadata fields", caught);
            }

            public void onSuccess(ListUserMetadataFieldsResult result) {
                availableFields = result.getFieldsSortedByName();
                GWT.log("available fields: " + availableFields);
                if (availableFields.size() > 0) {
                    if (canEdit) {
                        populateTypes(availableFields);
                    }
                }
            }
        });
    }

    /**
     * Widget to create new entry in table.
     * 
     * @param availableFields
     */
    private void populateTypes(SortedSet<UserMetadataField> availableFields) {
        fieldChoice.clear();
        fieldChoice.addItem("Add Field", "");
        int count = 1;
        for (UserMetadataField field : availableFields ) {
            String label = field.getLabel();
            String description = field.getDescription();
            String field_label = label;
            if (!("".equals(description))) {
                field_label += ": " + description;
            }
            String predicate = field.getUri();
            fieldChoice.addItem(field_label, predicate);
            labels.put(predicate, label);
            labels_description.put(predicate, description);
            indexLabel.put(predicate, count);
            count++;
        }
    }

    /**
     * Refresh and redraw table.
     */
    protected void refresh() {
        newFieldPanel.clear();
        fieldChoice.setSelectedIndex(0);
        inputField = null;
    }

    /**
     * RPC call to add a new entry.
     */
    protected void addValue(final boolean refresh) {
        String theValue = inputField.getValue();
        // Only MultiField inputs will display as HTML right now (in the MEdiciinterface to the data), 
        // but should avoid storing problematic values that may be displayed as HTML in other javascript apps 
        final String text = SimpleHtmlSanitizer.sanitizeHtml(theValue).asString();

        if (text.isEmpty() || text.equals("Select...")) {
            PopupPanel popupPanel = new PopupPanel(true);
            popupPanel.add(new Label("Please enter a value"));
            popupPanel.showRelativeTo(inputField);
            return;
        }

        if (!theValue.equals(text)) {
            PopupPanel popupPanel = new PopupPanel(true);
            popupPanel.add(new Label("Note: Your text has been modified to properly display special characters\r and/or disable unallowed HTML constructs."));
            popupPanel.showRelativeTo(inputField);
        }

        final String metadataUri = inputField.getUri();
        final String property = fieldChoice.getValue(fieldChoice.getSelectedIndex());
        // TODO pass section value if selected to backend
        //final String section = inputField.getSectionUri(); // null if section not specified

        final BatchCompletedEvent done = new BatchCompletedEvent(resources.size(), "modified");
        for (final String uris : resources ) {

            final GetSection gs = new GetSection();

            gs.setUri(uris); // dataset uri
            gs.setMarker(inputField.getSectionMarker());
            GWT.log(gs.getMarker() + " / " + inputField.getSectionMarker());
            // null if no section specified; the getsection handler will respond with a single section uri
            // equal to the dataseturi

            dispatch.execute(gs, new AsyncCallback<GetSectionResult>() {
                @Override
                public void onSuccess(GetSectionResult result) {
                    SetUserMetadata prop;

                    for (Section s : result.getSections() ) {
                        String sectionUri = s.getUri();
                        if (metadataUri == null) {
                            GWT.log("Adding new metadata: " + sectionUri + " | " + property + " | " + text);
                            prop = new SetUserMetadata(uris, property, text);
                        } else {
                            GWT.log("Adding new metadata: " + sectionUri + " | " + property + " | " + metadataUri);
                            prop = new SetUserMetadata(uris, property, metadataUri, true);
                        }
                        prop.setSectionUri(sectionUri);

                        dispatch.execute(prop, new AsyncCallback<EmptyResult>() {
                            public void onFailure(Throwable caught) {
                                GWT.log("Failed adding a new entry to the list", caught);

                                done.setFailure(uris, caught);
                                if (done.readyToFire() && eventBus != null) {
                                    eventBus.fireEvent(done);
                                }
                            }

                            public void onSuccess(EmptyResult result) {
                                GWT.log("User metadata field was successfully set");
                                done.addSuccess(uris);
                                if (done.readyToFire() && eventBus != null) {
                                    eventBus.fireEvent(done);
                                }

                                if (refresh) {
                                    refresh();
                                }
                                // FIXME this will refresh once per section!
                                //Since this is an add and we're sending in one section name (if it isn't the main value),
                                //won't this always be one section, i.e. no fix required even if there are annotations on 
                                //multiple sections?
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                    // the section marker doesn't match any sections
                    ConfirmDialog okay = new ConfirmDialog("Error", "Unrecognized section: " + gs.getMarker(), false);
                    okay.getOkText().setText("OK");
                }
            });

        }
    }

    /**
     * RPC call to remove an entry.
     * 
     * @param property
     * @param isEdit
     *            - in an edit, the removal of the old property should trigger
     *            the add of the new property before refresh occurs.
     */
    public void removeValue(final String property, final UserMetadataValue value, final boolean isEdit) {

        final GetSection gs = new GetSection();
        gs.setUri(uri);
        gs.setMarker(value.getSectionValue());

        dispatch.execute(gs, new AsyncCallback<GetSectionResult>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(GetSectionResult result) {
                for (Section s : result.getSections() ) {
                    String sectionUri = s.getUri();
                    RemoveUserMetadata remove;
                    if (value.getUri() == null) {
                        GWT.log("removing literal value '" + value.getName() + "'");
                        remove = new RemoveUserMetadata(uri, property, value.getName());
                    } else {
                        GWT.log("removing URI value '" + value.getUri() + "'");
                        String valUri = value.getUri();
                        //FixMe : VIVO ID special case - storing name and Uri in one triple until we have the list of vivo ids cached on the server
                        if (valUri.startsWith(vivoIDPrefixURL)) {
                            valUri = value.getName() + " : " + valUri;
                            remove = new RemoveUserMetadata(uri, property, valUri);
                        } else {
                            valUri = stripPrefix(valUri);
                            remove = new RemoveUserMetadata(uri, property, valUri, true);
                        }

                    }

                    GWT.log("section URI = " + value.getSectionUri());
                    remove.setSectionUri(sectionUri);

                    dispatch.execute(remove, new AsyncCallback<EmptyResult>() {
                        public void onFailure(Throwable caught) {
                            GWT.log("Error removing value", caught);
                        }

                        public void onSuccess(EmptyResult result) {
                            if (isEdit == true) {
                                addValue(true);
                            } else {
                                refresh();
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * Edit value of the property
     * 
     * @param property
     */
    public void editValue(final String property, final UserMetadataValue oldValue) {

        fieldChoice.setSelectedIndex(indexLabel.get(property));
        changeHandler();

        String currentVal = oldValue.getUri();
        if (currentVal == null) {
            currentVal = oldValue.getName();
        } else if (currentVal.startsWith(vivoIDPrefixURL)) {
            currentVal = oldValue.getName() + " : " + currentVal;
        } else {
            currentVal = stripPrefix(currentVal);
        }

        ClickHandler editHandler = new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                doEditEvent(property, oldValue);
            }
        };

        KeyUpHandler editPressEnter = new KeyUpHandler() {

            @Override
            public void onKeyUp(KeyUpEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    doEditEvent(property, oldValue);
                }

            }
        };

        inputField.setValue(currentVal);

        inputField.addAnchor.setText("Update");
        inputField.setEnterHandler(editPressEnter);
        inputField.addClickHandler.removeHandler();
        inputField.addClickHandler = inputField.addAnchor.addClickHandler(editHandler);

    }

    protected void doEditEvent(final String property, final UserMetadataValue oldValue) {
        String text = inputField.getUri();
        if (text == null) {
            text = inputField.getValue();
        }
        text = text.trim();
        if ((text == null) || text.equals("")) {
            //Don't bother to write, just remove and refresh
            removeValue(property, oldValue, false);
        } else {
            //Fixme? Unless I'm missing something, to determine whether we can avoid having to remove and 
            // then write just to keep the same value, we have to check whether the uris match 
            // (if present), the values (if present), or whether the section and section value match,
            // ... complex enough to skip since remove and then write the new value should always work, 
            // just be less efficient.
            //
            //add the new value after removing the old and before refreshing
            removeValue(property, oldValue, true);
        }
    }

    protected String stripPrefix(String val) {
        //remove added prefix used with datasets and collections
        if (val.startsWith("dataset?id=")) {
            val = val.substring(11); // "dataset?id=".length();
        }
        if (val.startsWith("collection?uri=")) {
            val = val.substring(15); // "collection?uri=".length();
        }
        return val;
    }

    abstract class InputField extends Composite implements HasValue<String> {

        protected final UserMetadataField userMetadataField;
        private final TextBox             sectionTextBox;
        private final RadioButton         sectionButton;
        private final RadioButton         documentButton;
        public Anchor                     addAnchor;
        public Widget                     inputWidget;
        FlowPanel                         appliedToPanel;
        protected HandlerRegistration     addClickHandler;
        protected HandlerRegistration     pressEnterHandler = null;

        public InputField(UserMetadataField userMetadataField, ClickHandler addHandler, ClickHandler clearHandler) {
            this.userMetadataField = userMetadataField;
            // TODO switch to divs
            // first div
            FlexTable layout = new FlexTable();
            layout.addStyleName("metadataPlainField");
            inputWidget = createInputWidget();

            layout.setWidget(0, 0, inputWidget);

            addAnchor = new Anchor("Add");
            addClickHandler = addAnchor.addClickHandler(addHandler);
            layout.setWidget(0, 1, addAnchor);
            Anchor clearAnchor = new Anchor("Cancel");
            clearAnchor.addClickHandler(clearHandler);
            layout.setWidget(0, 2, clearAnchor);

            // second row
            appliedToPanel = new FlowPanel();
            appliedToPanel.addStyleName("metadataAppliedPanel");
            appliedToPanel.add(new Label("Will be applied to"));
            documentButton = new RadioButton("appliedTo", "document");
            documentButton.setValue(true);
            appliedToPanel.add(documentButton);
            sectionButton = new RadioButton("appliedTo", "section");
            appliedToPanel.add(sectionButton);
            sectionTextBox = new TextBox();
            sectionTextBox.setWidth("100px");
            sectionTextBox.addFocusHandler(new FocusHandler() {

                @Override
                public void onFocus(FocusEvent event) {
                    sectionButton.setValue(true);
                }
            });
            appliedToPanel.add(sectionTextBox);
            layout.setWidget(1, 0, appliedToPanel);

            layout.getFlexCellFormatter().setColSpan(1, 0, 3);

            initWidget(layout);
        }

        abstract Widget createInputWidget();

        abstract String getUri();

        public void setEnterHandler(KeyUpHandler pressEnter) {
        }

        public void isAppliedToSectionVisible(boolean isVisible) {
            appliedToPanel.setVisible(isVisible);
        }

        public String getSectionMarker() {
            if (sectionButton.getValue()) {
                return sectionTextBox.getValue();
            } else {
                return null;
            }
        }

        @Override
        public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getValue() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setValue(String value) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setValue(String value, boolean fireEvents) {
            // TODO Auto-generated method stub

        }
    }

    class PlainField extends InputField {

        private TextBox textBox;

        public PlainField(UserMetadataField userMetadataField, ClickHandler addHandler, ClickHandler clearHandler) {
            super(userMetadataField, addHandler, clearHandler);
        }

        @Override
        public String getValue() {
            return textBox.getValue();
        }

        @Override
        public void setValue(String value) {
            textBox.setValue(value);
            textBox.setFocus(true);
        }

        @Override
        Widget createInputWidget() {
            textBox = new TextBox();
            pressEnterHandler = textBox.addKeyUpHandler(pressEnter);
            textBox.setWidth("500px");
            return textBox;
        }

        @Override
        String getUri() {
            String val = getValue();
            boolean isUri = false;
            if (val.startsWith("tag:") || val.startsWith("http:") || val.startsWith("https:") || val.startsWith("ftp:")) {
                return val;
            } else {
                return null;
            }
        }

        @Override
        public void setEnterHandler(KeyUpHandler pressEnter) {
            pressEnterHandler.removeHandler();
            pressEnterHandler = textBox.addKeyUpHandler(pressEnter);
        }
    }

    class MultiField extends InputField {

        private TextArea textArea;

        public MultiField(UserMetadataField userMetadataField, ClickHandler addHandler, ClickHandler clearHandler) {
            super(userMetadataField, addHandler, clearHandler);
        }

        @Override
        public String getValue() {
            return textArea.getValue();
        }

        @Override
        public void setValue(String value) {
            textArea.setValue(value);
            textArea.setFocus(true);
        }

        @Override
        Widget createInputWidget() {
            textArea = new TextArea();
            textArea.setWidth("500px");
            return textArea;
        }

        @Override
        String getUri() {
            // TODO Auto-generated method stub
            return null;
        }
    }

    //START - ADDED BY RAM
    //Inherit input field class to build a creator field - contains a suggest box instead of a text box
    class CreatorField extends InputField {

        //The suggest box contains the list of VIVO persons retrieved from the VIVO server 
        private SuggestBox vivoCreatorSuggestBox;

        public CreatorField(UserMetadataField userMetadataField, ClickHandler addHandler, ClickHandler clearHandler) {
            super(userMetadataField, addHandler, clearHandler);
            isAppliedToSectionVisible(false);
        }

        @Override
        public String getValue() {
            return vivoCreatorSuggestBox.getValue();
        }

        @Override
        public void setValue(String value) {
            vivoCreatorSuggestBox.setText(value);
            vivoCreatorSuggestBox.setFocus(true);
        }

        @Override
        Widget createInputWidget() {

            vivoCreatorSuggestBox = new SuggestBox();

            // HACK : https://code.google.com/p/google-web-toolkit/issues/detail?id=3533
            pressEnterHandler = vivoCreatorSuggestBox.getTextBox().addKeyUpHandler(pressEnter);
            vivoCreatorSuggestBox.setWidth("500px");
            return vivoCreatorSuggestBox;
        }

        @Override
        String getUri() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setEnterHandler(KeyUpHandler pressEnter) {
            pressEnterHandler.removeHandler();
            pressEnterHandler = vivoCreatorSuggestBox.getTextBox().addKeyUpHandler(pressEnter);
        }
    }

    class PartOfField extends InputField {

        //The suggest box contains the list of VIVO persons retrieved from the VIVO server 
        private SuggestBox partOfSuggestBox;

        public PartOfField(UserMetadataField userMetadataField, ClickHandler addHandler, ClickHandler clearHandler) {
            super(userMetadataField, addHandler, clearHandler);
            isAppliedToSectionVisible(false);
        }

        @Override
        public String getValue() {
            return partOfSuggestBox.getValue();
        }

        @Override
        public void setValue(String value) {
            partOfSuggestBox.setValue(value);
            partOfSuggestBox.setFocus(true);
        }

        @Override
        Widget createInputWidget() {

            partOfSuggestBox = new SuggestBox();

            pressEnterHandler = partOfSuggestBox.getTextBox().addKeyUpHandler(pressEnter);
            partOfSuggestBox.setWidth("500px");
            return partOfSuggestBox;
        }

        @Override
        String getUri() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setEnterHandler(KeyUpHandler pressEnter) {
            pressEnterHandler.removeHandler();
            pressEnterHandler = partOfSuggestBox.getTextBox().addKeyUpHandler(pressEnter);
        }
    }

    //END - ADDED BY RAM

    class ListField extends InputField {

        private ListBox listBox;

        public ListField(UserMetadataField userMetadataField, ClickHandler addHandler, ClickHandler clearHandler) {
            super(userMetadataField, addHandler, clearHandler);
        }

        @Override
        public String getValue() {
            return listBox.getItemText(listBox.getSelectedIndex());
        }

        @Override
        public void setValue(String value) {
            listBox.setSelectedIndex(listLabel.get(value));
            listBox.setFocus(true);
        }

        @Override
        Widget createInputWidget() {
            listBox = new ListBox();
            listBox.setWidth("500px");
            listBox.addItem("Select...", "");
            pressEnterHandler = listBox.addKeyUpHandler(pressEnter);
            int count = 1;
            List<NamedThing> range = new ArrayList<NamedThing>();
            range.addAll(NamedThing.orderByName(userMetadataField.getRange()));
            for (NamedThing namedThing : range ) {
                listBox.addItem(namedThing.getName(), namedThing.getUri());
                listLabel.put(namedThing.getName(), count);
                count++;
            }
            return listBox;
        }

        @Override
        String getUri() {
            // TODO Auto-generated method stub
            return listBox.getValue(listBox.getSelectedIndex());
        }

        @Override
        public void setEnterHandler(KeyUpHandler pressEnter) {
            pressEnterHandler.removeHandler();
            pressEnterHandler = listBox.addKeyUpHandler(pressEnter);
        }
    }

    class TreeField extends InputField {

        private Tree                tree;
        private final DispatchAsync dispatch;
        private TaxonomyTreeItem    root;

        public TreeField(DispatchAsync dispatch, UserMetadataField userMetadataField, ClickHandler addHandler, ClickHandler clearHandler) {
            super(userMetadataField, addHandler, clearHandler);
            this.dispatch = dispatch;
            populateTree();
        }

        private void populateTree() {

            Iterator<NamedThing> iterator = userMetadataField.getRange().iterator();
            while (iterator.hasNext()) {
                NamedThing thing = iterator.next();
                root = new TaxonomyTreeItem(thing.getName(), thing.getUri());
            }
            populateChildren(root);
            tree.addItem(root);
        }

        /**
         * Recursevely populate subtree starting at node.
         * 
         * @param node
         */
        private void populateChildren(final TaxonomyTreeItem node) {

            dispatch.execute(new GetSubclasses(node.getUri()), new AsyncCallback<GetSubclassesResult>() {

                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Error getting subclasses of " + userMetadataField.getUri(), caught);
                }

                @Override
                public void onSuccess(GetSubclassesResult result) {
                    List<NamedThing> children = result.getSubclasses();
                    if (children.size() != 0) {
                        for (NamedThing namedThing : children ) {
                            GWT.log("Adding " + namedThing.getName() + " to " + node.getLabel());
                            final TaxonomyTreeItem newNode = new TaxonomyTreeItem(namedThing.getName(), namedThing.getUri());
                            node.addItem(newNode);
                            populateChildren(newNode);
                        }
                    }
                }
            });
        }

        @Override
        Widget createInputWidget() {
            ScrollPanel scrollPanel = new ScrollPanel();
            scrollPanel.addStyleName("metadataTreeScrollPanel");
            tree = new Tree();
            tree.setAnimationEnabled(true);
            scrollPanel.add(tree);
            return scrollPanel;
        }

        @Override
        public String getValue() {
            return tree.getSelectedItem().getText();
        }

        @Override
        public void setValue(String value) {
            tree.setFocus(true);

        }

        @Override
        public String getUri() {
            return ((TaxonomyTreeItem) tree.getSelectedItem()).getUri();
        }
    }

    class TaxonomyTreeItem extends TreeItem {

        private String uri;
        private String label;

        public TaxonomyTreeItem() {
            super();
        }

        public TaxonomyTreeItem(String label, String uri) {
            super();
            this.label = label;
            this.uri = uri;
            setText(label);
        }

        public String getUri() {
            return uri;
        }

        public String getLabel() {
            return label;
        }
    }

}
