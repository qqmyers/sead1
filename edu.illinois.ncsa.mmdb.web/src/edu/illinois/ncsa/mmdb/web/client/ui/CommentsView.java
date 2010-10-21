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

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.PermissionUtil;
import edu.illinois.ncsa.mmdb.web.client.PermissionUtil.PermissionCallback;
import edu.illinois.ncsa.mmdb.web.client.PermissionUtil.PermissionsCallback;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AnnotateResource;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AnnotateResourceResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetAnnotations;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetAnnotationsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.HasPermissionResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.event.DeletedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DeletedHandler;
import edu.uiuc.ncsa.cet.bean.AnnotationBean;
import edu.uiuc.ncsa.cet.bean.rbac.medici.Permission;

/**
 * Shows a list of comments (annotations) for a particular resource. Allows the
 * user to add a new annotation to the list.
 * 
 * @author Luigi Marini <lmarini@ncsa.uiuc.edu>
 * 
 */
public class CommentsView extends Composite {

    private final SimplePanel     mainPanel = new SimplePanel();

    private final VerticalPanel   layoutPanel;

    private final String          resource;

    private final VerticalPanel   commentsPanel;

    private final MyDispatchAsync service;

    private final PermissionUtil  rbac;

    /**
     * Draws the main panel and the widget to input a new annotation. Calls the
     * refresh method to refresh the list and retrieve all the current
     * annotations.
     * 
     * @param resource
     */
    public CommentsView(final String resource, final MyDispatchAsync service) {

        this.resource = resource;

        this.service = service;

        rbac = new PermissionUtil(service);

        DisclosurePanel disclosurePanel = new DisclosurePanel("Comments");

        disclosurePanel.addStyleName("datasetDisclosurePanel");

        disclosurePanel.setOpen(true);

        disclosurePanel.setAnimationEnabled(true);

        initWidget(disclosurePanel);

        mainPanel.addStyleName("commentsView");

        layoutPanel = new VerticalPanel();

        mainPanel.add(layoutPanel);

        disclosurePanel.setContent(mainPanel);

        commentsPanel = new VerticalPanel();

        commentsPanel.setWidth("100%");

        layoutPanel.add(commentsPanel);

        rbac.doIfAllowed(Permission.ADD_COMMENT, new PermissionCallback() {
            @Override
            public void onAllowed() {
                final NewAnnotationView newAnnotationView = new NewAnnotationView();

                // add new annotation
                newAnnotationView.addClickHandler(new ClickHandler() {

                    public void onClick(ClickEvent arg0) {

                        service.execute(new AnnotateResource(resource,
                                newAnnotationView.getAnnotationBean(), MMDB.getUsername()),
                                new AsyncCallback<AnnotateResourceResult>() {

                                    @Override
                                    public void onFailure(Throwable caught) {
                                        GWT.log("Failed to annotate resource "
                                                + resource, caught);
                                    }

                                    @Override
                                    public void onSuccess(AnnotateResourceResult result) {
                                        newAnnotationView.clear();

                                        refresh();

                                    }
                                });

                    }

                });

                layoutPanel.add(newAnnotationView);
            }
        });

        refresh();
    }

    /**
     * Retrieves annotations and adds them to the panel.
     * 
     */
    private void refresh() {

        commentsPanel.clear();

        service.execute(new GetAnnotations(resource),
                new AsyncCallback<GetAnnotationsResult>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Error retrieving annotations", caught);
                    }

                    @Override
                    public void onSuccess(GetAnnotationsResult result) {

                        ArrayList<AnnotationBean> annotations = result
                                .getAnnotations();

                        show(annotations);
                    }
                });
    }

    /**
     * 
     * @param annotations
     */
    public void show(final ArrayList<AnnotationBean> annotations) {
        rbac.withPermissions(new PermissionsCallback() {
            @Override
            public void onPermissions(HasPermissionResult permissions) {
                commentsPanel.clear();

                commentsPanel.add(new Label(annotations.size() + " comment" + (annotations.size() != 1 ? "s" : "")));

                for (AnnotationBean annotation : annotations ) {
                    AnnotationView v = new AnnotationView(resource, annotation, permissions.isPermitted(Permission.EDIT_COMMENT));
                    if (permissions.isPermitted(Permission.EDIT_COMMENT)) {
                        v.addDeletedHandler(new DeletedHandler() {
                            public void onDeleted(DeletedEvent event) {
                                refresh();
                            }
                        });
                    }
                    commentsPanel.add(v);
                }
            }
        }, Permission.EDIT_COMMENT);
    }

}
