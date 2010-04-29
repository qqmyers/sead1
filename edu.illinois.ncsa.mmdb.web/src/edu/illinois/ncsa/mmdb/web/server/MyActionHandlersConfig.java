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
/**
 *
 */
package edu.illinois.ncsa.mmdb.web.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.illinois.ncsa.mmdb.web.server.dispatch.AddCollectionHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.AddToCollectionHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.AddUserHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.AnnotateResourceHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.AuthenticateHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.ChangeUserHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.ContextConvertHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.DeleteAnnotationHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.DeleteDatasetHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.EditRoleHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.ExtractionServiceHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetAllTagsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetAnnotationsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetCollectionHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetCollectionsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetDatasetHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetDatasetsByTagHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetDatasetsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetDerivedFromHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetGeoPointHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetLicenseHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetLikeDislikeHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetMetadataHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetPreviewsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetRecentActivityHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetRolesHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetTagsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetUploadDestinationHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetUserHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetUserMetadataFieldsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetUsersHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetViewCountHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.HasPermissionHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.IsPreviewPendingHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.ListDatasetsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.ListQueryDatasetsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.ReindexLuceneHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.RemoveFromCollectionHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.RequestNewPasswordHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.RunSparqlQueryHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.SearchHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.SetLicenseHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.SetPropertyHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.TagResourceHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.UserGroupMembershipHandler;

/**
 * Setup registry of action handlers when the servlet context is initialized.
 * 
 * @author Luigi Marini
 * 
 */
public class MyActionHandlersConfig implements ServletContextListener {
    public void contextInitialized(ServletContextEvent evt) {
        DispatchUtil.registerHandler(new GetDatasetsHandler());
        DispatchUtil.registerHandler(new GetDatasetHandler());
        DispatchUtil.registerHandler(new AnnotateResourceHandler());
        DispatchUtil.registerHandler(new TagResourceHandler());
        DispatchUtil.registerHandler(new GetTagsHandler());
        DispatchUtil.registerHandler(new GetAnnotationsHandler());
        DispatchUtil.registerHandler(new GetDatasetsByTagHandler());
        DispatchUtil.registerHandler(new ListDatasetsHandler());
        DispatchUtil.registerHandler(new AuthenticateHandler());
        DispatchUtil.registerHandler(new GetMetadataHandler());
        DispatchUtil.registerHandler(new GetGeoPointHandler());
        DispatchUtil.registerHandler(new GetCollectionsHandler());
        DispatchUtil.registerHandler(new AddCollectionHandler());
        DispatchUtil.registerHandler(new GetCollectionHandler());
        DispatchUtil.registerHandler(new AddToCollectionHandler());
        DispatchUtil.registerHandler(new GetPreviewsHandler());
        DispatchUtil.registerHandler(new GetUploadDestinationHandler());
        DispatchUtil.registerHandler(new DeleteDatasetHandler());
        DispatchUtil.registerHandler(new GetUsersHandler());
        DispatchUtil.registerHandler(new UserGroupMembershipHandler());
        DispatchUtil.registerHandler(new AddUserHandler());
        DispatchUtil.registerHandler(new HasPermissionHandler());
        DispatchUtil.registerHandler(new EditRoleHandler());
        DispatchUtil.registerHandler(new GetRolesHandler());
        DispatchUtil.registerHandler(new GetUserHandler());
        DispatchUtil.registerHandler(new RequestNewPasswordHandler());
        DispatchUtil.registerHandler(new ChangeUserHandler());
        DispatchUtil.registerHandler(new GetDerivedFromHandler());
        DispatchUtil.registerHandler(new RunSparqlQueryHandler());
        DispatchUtil.registerHandler(new GetUserMetadataFieldsHandler());
        DispatchUtil.registerHandler(new SetPropertyHandler());
        DispatchUtil.registerHandler(new ExtractionServiceHandler());
        DispatchUtil.registerHandler(new DeleteAnnotationHandler());
        DispatchUtil.registerHandler(new SearchHandler());
        DispatchUtil.registerHandler(new RemoveFromCollectionHandler());
        DispatchUtil.registerHandler(new GetAllTagsHandler());
        DispatchUtil.registerHandler(new GetRecentActivityHandler());
        DispatchUtil.registerHandler(new ReindexLuceneHandler());
        DispatchUtil.registerHandler(new IsPreviewPendingHandler());
        DispatchUtil.registerHandler(new ContextConvertHandler());
        DispatchUtil.registerHandler(new GetLicenseHandler());
        DispatchUtil.registerHandler(new SetLicenseHandler());
        DispatchUtil.registerHandler(new GetViewCountHandler());
        DispatchUtil.registerHandler(new GetLikeDislikeHandler());
        DispatchUtil.registerHandler(new ListQueryDatasetsHandler());
    }

    public void contextDestroyed(ServletContextEvent evt) {
    }
}
